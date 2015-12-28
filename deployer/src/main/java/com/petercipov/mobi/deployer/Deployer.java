package com.petercipov.mobi.deployer;

import com.petercipov.mobi.Image;
import com.petercipov.mobi.ApiHost;
import com.petercipov.traces.api.Level;
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import rx.Observable;

/**
 *
 * @author pcipov
 */
public class Deployer {
	private static final List<String> DEFAULT_VOLUME_BINDINGS = Arrays.asList(
		"/var/log:/var/log",
		"/var/run/dockerhost/:/etc/dockerhost",
		"/var/run:/var/dockerhost/run"
	);
	
	private final ApiHost api;
	private final List<Container<? extends Image>> deployedContainers;
	private final RxDocker rxdocker;
	
	public Deployer(ApiHost api, RxDocker rxdocker) {
		this.api = api;
		this.rxdocker = rxdocker;
		this.deployedContainers = Collections.synchronizedList(new LinkedList<>());
	}
	
	public <T extends Image> Observable<Container<T>> deploy(Trace trace, T image) {
		return deploy(trace, image, Optional.empty());
	}
	
	public <T extends Image> Observable<Container<T>> deploy(Trace trace, T image, Optional<PortsMapping> portMapping) {
		return deploy(trace, image, portMapping, ContainerConfig.builder());
	}
	
	public <T extends Image> Observable<Container<T>> deploy(Trace trace, T image, Optional<PortsMapping> portMapping, ContainerConfig.Builder builder) {
		return deploy(trace, Optional.empty(), image, portMapping, builder);
	}
	
	public <T extends Image> Observable<Container<T>> deploy(Trace trace, Optional<String> name, T image) {
		return deploy(trace, name, image, Optional.empty());
	}
	
	public <T extends Image> Observable<Container<T>> deploy(Trace trace, Optional<String> name, T image, Optional<PortsMapping> portMapping) {
		return deploy(trace, name, image, portMapping, ContainerConfig.builder());
	}
	
	public <T extends Image> Observable<Container<T>> deploy(Trace trace, Optional<String> name, T image, Optional<PortsMapping> portMapping, ContainerConfig.Builder builder) {
		return Observable.defer(() -> {
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
				.map(imageToDeploy -> createContainerConfig(builder, imageToDeploy, createHostConfig(trace, portMapping)))
				.flatMap((config) -> rxdocker.createContainer(trace, name, config))
				.flatMap(containerId -> {
					return rxdocker.startContainer(trace, containerId)
						.flatMap(xxx -> rxdocker.inspectContainer(trace, containerId))
						.flatMap(containerInfo -> checkIfRunning(containerInfo, containerId))
						.flatMap(containerInfo -> remapPorts(containerInfo, image, portMapping))
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

	private Observable<Map<String, Integer>> remapPorts(ContainerInfo info, Image image, Optional<PortsMapping> portMapping) {
		Map<String, List<PortBinding>> ports = info.networkSettings().ports();
		Map<String, Integer> remapping = new HashMap<>(1);
		
		Collection<String> exposedPorts = portMapping.map(
			(mapping) -> mapping.getExposedPorts()
		).orElse(image.getExposedPorts());
		
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
	
	private void withDockerHostIpVariable(ContainerConfig.Builder configBuilder) {
		List<String> env = configBuilder.env();
		ArrayList<String> list = new ArrayList<>();
		if (env != null) {
			list.addAll(env);
		}
		list.add("DOCKER_HOST_IP="+api.getHost());
		configBuilder.env(list);
	}

	protected ContainerConfig createContainerConfig(ContainerConfig.Builder builder, Image image, HostConfig hostConfig) {
		withDockerHostIpVariable(builder);
		return builder
			.image(image.toString())
			.hostConfig(hostConfig)
			.build();
	}

	protected HostConfig createHostConfig(Trace trace, Optional<PortsMapping> portsMapping) {
		List<String> bindings = api.getDefaultVolumeBindings().orElse(DEFAULT_VOLUME_BINDINGS);
		
		HostConfig.Builder b = HostConfig.builder()
			.binds(bindings);
			
		if (portsMapping.isPresent()) {
			b = b.portBindings(portsMapping.get().getPortBindings());
			trace.event("Deployer: setup container (volumes, ports)", bindings, b.portBindings());
		} else {
			b = b.publishAllPorts(true);
			trace.event("Deployer: setup container (volumes, enabling all exposed ports)", bindings, b.publishAllPorts());
		}
		return b.build();
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