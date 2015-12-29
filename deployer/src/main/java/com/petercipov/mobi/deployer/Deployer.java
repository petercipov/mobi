package com.petercipov.mobi.deployer;

import com.petercipov.mobi.Image;
import com.petercipov.mobi.ApiHost;
import com.petercipov.traces.api.Level;
import com.petercipov.traces.api.NoopTrace;
import com.petercipov.traces.api.Trace;
import com.petercipov.traces.api.Trace.Event;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.ContainerState;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;
import com.spotify.docker.client.messages.ProgressMessage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import rx.Observable;

/**
 *
 * @author pcipov
 */
public class Deployer {
	
	private final ApiHost api;
	private final List<Container<? extends Image>> deployedContainers;
	private final RxDocker rxdocker;
	
	public Deployer(ApiHost api, RxDocker rxdocker) {
		this.api = api;
		this.rxdocker = rxdocker;
		this.deployedContainers = Collections.synchronizedList(new LinkedList<>());
	}
	
	public static class Builder<T extends Image> {
		private Trace trace;
		private final T image;
		private Optional<String> name;
		private final ContainerConfig.Builder containerConfig;
		private final HostConfig.Builder hostConfig;

		public Builder(T image) {
			this.image = image;
			this.trace = NoopTrace.INSTANCE;
			this.name = Optional.empty();
			this.containerConfig = ContainerConfig.builder();
			this.hostConfig = HostConfig.builder();
		}
		
		public Builder<T> trace(Trace trace) {
			this.trace = trace;
			return this;
		}
		
		public Builder<T> name(String name) {
			this.name = Optional.of(name);
			return this;
		}
		
		public Builder<T> volume(String hostPath, String guestPath) {
			String path = hostPath+":"+guestPath;
			List<String> volumes = new LinkedList<>();
			
			if (this.hostConfig.binds() == null) {
				volumes.add(path);
			} else {
				volumes.addAll(this.hostConfig.binds());
				volumes.add(path);
			}
			
			this.hostConfig.binds(volumes);
			return this;
		}
		
		public Builder<T> env(String variable) {
			List<String> env = new LinkedList<>();
			
			if (this.containerConfig.env() == null) {
				env.add(variable);
			} else {
				env.addAll(this.containerConfig.env());
				env.add(variable);
			}
			this.containerConfig.env(env);
			
			return this;
		}
		
		public Builder<T> port(String port, int customPort) {
			if (this.hostConfig.portBindings() == null) {
				HashMap<String, List<PortBinding>> ports = new HashMap<>();
				ports.put(port, Arrays.asList(PortBinding.of(null, customPort)));
				this.hostConfig.portBindings(ports);
			} else {
				this.hostConfig.portBindings()
				.put(port, Arrays.asList(PortBinding.of(null, customPort)));
			}
			
			return this;
		}
		
		public Builder<T> withContainer(Consumer<ContainerConfig.Builder> config) {
			config.accept(this.containerConfig);
			return this;
		}

		public ContainerConfig.Builder containerConfig() {
			return this.containerConfig;
		}
		
		public Builder<T> withHost(Consumer<HostConfig.Builder> config) {
			config.accept(this.hostConfig);
			return this;
		}
		
		public HostConfig.Builder hostConfig() {
			return this.hostConfig;
		}

		public Trace trace() {
			return trace;
		}

		public T image() {
			return image;
		}

		public Optional<String> name() {
			return name;
		}
		
		private void withDockerHostIpVariable( String host) {
			List<String> env = this.containerConfig.env();
			LinkedList<String> list = new LinkedList<>();
			if (env != null) {
				list.addAll(env);
			}
			list.add("DOCKER_HOST_IP="+host);
			this.containerConfig.env(list);
		}
		
		private HostConfig withPortsAndVolumes(Trace trace, ApiHost api) {
			List<String> bindings = new LinkedList<>(api.getDefaultVolumeBindings().orElse(Collections.emptyList()));

			if (this.hostConfig.binds() != null) {
				bindings.addAll(this.hostConfig.binds());
			}
			
			if (! bindings.isEmpty()) {
				this.hostConfig.binds(bindings);
			}

			if (this.hostConfig.portBindings() == null) {
				this.hostConfig.publishAllPorts(true);
				trace.event("Deployer: setup container (volumes, enabling all exposed ports)", bindings, this.hostConfig.publishAllPorts());
			} else {
				trace.event("Deployer: setup container (volumes, ports)", bindings, this.hostConfig.portBindings());
			}
			return this.hostConfig.build();
		}
		
		ContainerConfig build(ApiHost api) {
			withDockerHostIpVariable(api.getHost());
			withPortsAndVolumes(trace, api);
			return this.containerConfig
				.image(image.toString())
				.hostConfig(this.hostConfig.build())
				.build();
		}
	}
	
	public <T extends Image> Observable<Container<T>> deploy(Builder<T> deployment) {
		return Observable.defer(() -> {
			Trace trace = deployment.trace();
			T image = deployment.image();
			ContainerConfig config = deployment.build(api);
			Event deployEvent = trace.start("Deployer: deploying image", image);
			return rxdocker.isImagePresent(trace, image)
				.flatMap(present -> {
					if (present) {
						trace.event("Deployer: Image already present in the docker", image);
						return  Observable.just(image);
					} else {
						trace.event("Deployer: Image not present in the docker, pulling image from registry", image);
						return rxdocker.pull(trace, image, progress -> logProgress(trace, progress)); 
					}
				})
				.flatMap((xxx) -> rxdocker.createContainer(trace, deployment.name(), config))
				.flatMap(containerId -> {
					return rxdocker.startContainer(trace, containerId)
						.flatMap(xxx -> rxdocker.inspectContainer(trace, containerId))
						.flatMap(containerInfo -> checkIfRunning(containerInfo, containerId))
						.flatMap(containerInfo -> remapPorts(containerInfo, image, config.hostConfig().portBindings()))
						.onErrorResumeNext(ex ->  {
							return containerId == null
								? Observable.error(ex)
								: rxdocker.killContainer(trace, containerId)
									.flatMap(id -> rxdocker.removeContainer(trace, id))
									.flatMap(id -> Observable.error(ex));
						})
						.map(ports -> {
							Container<T> container = new Container<>(containerId, image, api.getHost(), ports, Deployer.this);
							trace.event("Deployer: image was deployed, (container)", container);
							deployedContainers.add(container);
							return container;
						})
					;
				})
				.doOnError((th) -> trace.event(Level.ERROR, "Deployer: could not deploy", th))
				.doOnTerminate(() -> deployEvent.end());
		});
	}
	
	public <T extends Image> Observable<ContainerInfo> inspectContainer(Trace trace, Container<T> container) {
		return rxdocker.inspectContainer(trace, container);
	}
	
	public <T extends Image> Observable<Container<T>> killContainer(Trace trace, Container<T> container) {
		trace.event("Deployer: killing container, (container id, image)", container.getContainerId(), container.getImage());
		return rxdocker.killContainer(trace, container.getContainerId())
			.flatMap(id -> rxdocker.removeContainer(trace, id))
			.map(id -> {
				trace.event("Deployer: container was killed, (container id)", id);
				deployedContainers.remove(container);
				return container;
			});
	}
	
	public <T extends Image> Observable<Container<T>> stopContainer(Trace trace, Container<T> container, int secondsBefaoreKill) {
		trace.event("Deployer: stopping container (container id, image)", container.getContainerId(), container.getImage());
		return rxdocker.stopContainer(trace, container.getContainerId(), secondsBefaoreKill)
			.flatMap(id -> rxdocker.removeContainer(trace, id))
			.map(id -> {
				trace.event("Deployer: container was stopped, (container id)", id);
				deployedContainers.remove(container);
				return container;
			});		
	}
	
	public Observable<ContainerState> watchContainerIfRunning(Trace trace, Container<?> container, long retryTime, TimeUnit retryTimeUnit) {
		return rxdocker.inspectContainer(trace, container)
			.map(inspect -> inspect.state())
			.take(1)
			.flatMap(state -> {
				return state.running()
					? watchContainerIfRunning(trace, container, retryTime, retryTimeUnit)
						.delaySubscription(retryTime, retryTimeUnit)
					: Observable.just(state);
			}
		);
	}

	private Observable<Map<String, Integer>> remapPorts(ContainerInfo info, Image image, Map<String, List<PortBinding>> portBindings) {
		Map<String, List<PortBinding>> ports = info.networkSettings().ports();
		Map<String, Integer> remapping = new HashMap<>(1);
		
		Iterable<String> exposedPorts = Optional.ofNullable(portBindings).map(bindings -> (Iterable<String>)bindings.keySet()).orElse(image.getExposedPorts());
		
		int port;
		for (String exposedPort : exposedPorts) {
			if (ports == null) {
				return Observable.error(new IllegalStateException("Expecting ports mapping in the container, but docker retuned none, containerId="+info.id()+", image="+image));
			}
			List<PortBinding> bindings = ports.get(exposedPort);
			if (bindings == null) {
				return Observable.error(new IllegalStateException("mising port binding for "+exposedPort +" of "+image +" in exposed ports map"+ports));
			} else {
				try {
					port  = Integer.parseInt(bindings.get(0).hostPort());
				} catch(Exception ex) {
					return Observable.error(ex);
				}
				remapping.put(exposedPort, port);
			}
		}
		return Observable.just(remapping);
	}

	public void close(Trace trace) throws IOException {
		killAllPendingContainers(trace);
	}

	private void killAllPendingContainers(Trace trace) {
		trace.event("Deployer: killing all pending containers");
		
		List<Container<? extends Image>> list;
		synchronized(deployedContainers) {
			list = new ArrayList<>(deployedContainers);
		}
		
		Observable.from(list)
			.flatMap((c) -> this.killContainer(trace, c))
			.toList().toBlocking().single()
		;
	}
	
	private void logProgress(Trace trace, ProgressMessage progress) {
		if (trace.isDebugEnabled()) {
			if ("downloading".equalsIgnoreCase(progress.status())) {
				//ommit donloading messages
				return;
			}

			trace.event(Level.DEBUG, "pull progress (id, status, progress, error)", progress.id(), progress.status(), progress.progress(), progress.error());
		}
	}

	private Observable<ContainerInfo> checkIfRunning(ContainerInfo containerInfo, String containerId) {		
		if (containerInfo.state().running()) {
			return Observable.just(containerInfo);
		} else {
			return Observable.error(new IllegalStateException("Expecting container to run. State="+containerInfo.state()+", containerId="+containerId));
		}
	}
}