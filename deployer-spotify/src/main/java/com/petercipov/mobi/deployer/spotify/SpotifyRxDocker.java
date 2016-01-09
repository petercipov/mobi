package com.petercipov.mobi.deployer.spotify;

import com.petercipov.mobi.Image;
import com.petercipov.mobi.deployer.PortBinding;
import com.petercipov.traces.api.Level;
import com.petercipov.traces.api.Trace;
import com.petercipov.traces.api.Trace.Event;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.ProgressMessage;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import com.petercipov.mobi.deployer.RxDocker;

/**
 *
 * @author Peter Cipov
 */
public class SpotifyRxDocker implements RxDocker<ContainerConfig, SpotifyDeployment<? extends Image>>{
    
    private final DefaultDockerClient.Builder clientBuilder;
	private final Scheduler scheduler;
	private final Map<Long, DockerClient> clients;

	public SpotifyRxDocker(DefaultDockerClient.Builder builder, Scheduler scheduler) {
		this.clientBuilder = builder;
		this.scheduler = scheduler;
		this.clients = Collections.synchronizedMap(new HashMap<>());
	}

    @Override
    public Observable<Image> pull(Trace trace, Image image) {
        return Observable.create((Subscriber<? super Image> subscriber) -> {
			Trace.Event pulling = trace.start("RxDocker: pull(image)", image);
			final DockerClient client;
			try {
				client = obtainDockerClient();
				client.pull(image.toString(), message -> logPullProgress(trace, message));
			} catch(Exception ex) {
				trace.event(Level.ERROR, "RxDocker: pulling failed", ex);
				pulling.end();
				
				if (subscriber.isUnsubscribed()) return;
				subscriber.onError(ex);
				return;
			}
			pulling.end();
			
			if (subscriber.isUnsubscribed()) return;
			subscriber.onNext(image);
			subscriber.onCompleted();
		}).subscribeOn(scheduler);
    }
    
    public Observable<List<com.spotify.docker.client.messages.Image>> listImages(Trace trace) {
		return Observable.create((Subscriber<? super List<com.spotify.docker.client.messages.Image>> subscriber) -> {
			Trace.Event listing = trace.start("RxDocker: listing images");
			final DockerClient client;
			final List<com.spotify.docker.client.messages.Image> list;
			try {
				client = obtainDockerClient();
				list = client.listImages();
			} catch(Exception ex) {
				trace.event(Level.ERROR, "RxDocker: listing failed", ex);
				listing.end();
				
				if (subscriber.isUnsubscribed()) return;
				subscriber.onError(ex);
				return;
			}
			listing.end();
			
			if (subscriber.isUnsubscribed()) return;
			subscriber.onNext(list);
			subscriber.onCompleted();
		}).subscribeOn(scheduler);
	}
	
	public Observable<com.spotify.docker.client.messages.Container> listContainers(Trace trace, boolean all) {
		return Observable.create((Subscriber<? super com.spotify.docker.client.messages.Container> subscriber) -> {
			Trace.Event listing = trace.start("RxDocker: listing containers");
			final DockerClient client;
			final List<com.spotify.docker.client.messages.Container> list;
			try {
				client = obtainDockerClient();
				list = client.listContainers(DockerClient.ListContainersParam.allContainers(all));
			} catch(Exception ex) {
				trace.event("RxDocker: listing failed", ex);
				listing.end();
				
				if (subscriber.isUnsubscribed()) return;
				subscriber.onError(ex);
				return;
			}
			listing.end();
			
			if (subscriber.isUnsubscribed()) return;
			for(com.spotify.docker.client.messages.Container container : list) {
				subscriber.onNext(container);
			}
			subscriber.onCompleted();
		}).subscribeOn(scheduler);
	}

    @Override
    public Observable<Boolean> isPresent(Trace trace, Image image) {
        return Observable.defer(() -> {
			final String id = image.toString();
			trace.event("RxDocker: checking if image is present", image);
			return listImages(trace)
				.map(list -> {
					for (com.spotify.docker.client.messages.Image entry : list) {
						for (String tag: entry.repoTags()) {
							if (tag.equals(id)) {
								return true;
							}
						}
					}
					return false;
				});
		});
    }

    @Override
    public Observable<String> createContainer(Trace trace, SpotifyDeployment builder) {
        return Observable.create((Subscriber<? super String> subscriber) -> {
            Optional<String> name = builder.name();
            ContainerConfig containerConfig = builder.build();
			Event creating = trace.start("RxDocker: creating container from image ", containerConfig.image());
			final DockerClient client;
			final String containerId;
			try {
				client = obtainDockerClient();
				containerId = name.isPresent() 
					? client.createContainer(containerConfig, name.get()).id() 
					: client.createContainer(containerConfig).id();
			} catch(Exception ex) {
				trace.event(Level.ERROR, "RxDocker: creation failed", ex);
				creating.end();
				
				if (subscriber.isUnsubscribed()) return;
				subscriber.onError(ex);
				return;
			}
			creating.end();
			
			if (subscriber.isUnsubscribed()) return;
			subscriber.onNext(containerId);
			subscriber.onCompleted();
		}).subscribeOn(scheduler);
    }

    @Override
    public Observable<String> startContainer(Trace trace, String containerId) {
        return Observable.create((Subscriber<? super String> subscriber) -> {
			Event starting = trace.start("RxDocker: starting container (containerId): ", containerId);
			final DockerClient client;
			try {
				client = obtainDockerClient();
				client.startContainer(containerId);
			} catch(Exception ex) {
				trace.event(Level.ERROR, "RxDocker: starting failed", ex);
				starting.end();
				
				if (subscriber.isUnsubscribed()) return;
				subscriber.onError(ex);
				return;
			}
			starting.end();
			
			if (subscriber.isUnsubscribed()) return;
			subscriber.onNext(containerId);
			subscriber.onCompleted();
		}).subscribeOn(scheduler);
    }

    @Override
    public Observable<String> killContainer(Trace trace, String containerId) {
        return Observable.create((Subscriber<? super String> subscriber) -> {
			Event killing = trace.start("RxDocker: killing container: ", containerId);
			final DockerClient client;
			try {
				client = obtainDockerClient();
				client.killContainer(containerId);
			} catch(Exception ex) {
				trace.event("RxDocker: killing failed", ex);
				killing.end();
				
				if (subscriber.isUnsubscribed()) return;
				subscriber.onError(ex);
				return;
			}
			killing.end();
			
			if (subscriber.isUnsubscribed()) return;
			subscriber.onNext(containerId);
			subscriber.onCompleted();
		}).subscribeOn(scheduler);
    }

    @Override
    public Observable<String> stopContainer(Trace trace, String containerId, int secondsBeforeFail) {
        return Observable.create((Subscriber<? super String> subscriber) -> {
			Event stopping = trace.start("RxDocker: stopping container: ", containerId);
			final DockerClient client;
			try {
				client = obtainDockerClient();
				client.stopContainer(containerId, secondsBeforeFail);
			} catch(Exception ex) {
				trace.event("RxDocker: stopping failed", ex);
				stopping.end();
				
				if (subscriber.isUnsubscribed()) return;
				subscriber.onError(ex);
				return;
			}
			stopping.end();
			
			if (subscriber.isUnsubscribed()) return;
			subscriber.onNext(containerId);
			subscriber.onCompleted();
		}).subscribeOn(scheduler);
    }

    @Override
    public Observable<String> removeContainer(Trace trace, String containerId) {
        return Observable.create((Subscriber<? super String> subscriber) -> {
			Event removing = trace.start("RxDocker: removing container: ", containerId);
			final DockerClient client;
			try {
				client = obtainDockerClient();
				client.removeContainer(containerId, true);
			} catch(Exception ex) {
				trace.event("RxDocker: removing failed", ex);
				removing.end();
				
				if (subscriber.isUnsubscribed()) return;
				subscriber.onError(ex);
				return;
			}
			removing.end();
			
			if (subscriber.isUnsubscribed()) return;
			subscriber.onNext(containerId);
			subscriber.onCompleted();
		}).subscribeOn(scheduler);
    }

    public Observable<ContainerInfo> inspectContainer(Trace trace, String containerId) {
        return Observable.create((Subscriber<? super ContainerInfo> subscriber) -> {
			Event inspecting = trace.start("RxDocker: inspecting container (containerId): ", containerId);
			final DockerClient client;
			final ContainerInfo info;
			try {
				client = obtainDockerClient();
				info = client.inspectContainer(containerId);
			} catch(Exception ex) {
				trace.event("RxDocker: inspecting failed", ex);
				inspecting.end();
				
				if (subscriber.isUnsubscribed()) return;
				subscriber.onError(ex);
				return;
			}
			inspecting.end();
			
			if (subscriber.isUnsubscribed()) return;
			subscriber.onNext(info);
			subscriber.onCompleted();
		}).subscribeOn(scheduler);
    }

    @Override
    public Observable<Boolean> isContainerRunning(Trace trace, String containerId) {
        return inspectContainer(trace, containerId)
            .map(containerInfo -> containerInfo.state().running());
    }

    @Override
    public Observable<Map<String, List<PortBinding>>> containerPorts(Trace trace, String containerId) {
        return inspectContainer(trace, containerId)
			.map(containerInfo -> {
				Map<String, List<com.spotify.docker.client.messages.PortBinding>> containerPorts = containerInfo.networkSettings().ports();
				HashMap<String, List<PortBinding>> converted = new HashMap<>(containerPorts.size());
				for (Map.Entry<String, List<com.spotify.docker.client.messages.PortBinding>> entry: containerPorts.entrySet()) {
					List<PortBinding> p = new LinkedList<>();
					for (com.spotify.docker.client.messages.PortBinding binding : entry.getValue()) {
						p.add(new PortBinding(binding.hostIp(), Integer.parseInt(binding.hostIp())));
					}
					converted.put(entry.getKey(), p);
				}
				return converted;
			});
    }
	
    public void close(Trace trace) {
        closeAllClients(trace);
        trace.event("RxDocker: closed all containers");
    }
    
    protected void closeAllClients(Trace trace) {
		synchronized(clients) {
			for (DockerClient client : clients.values()) {
				client.close();
			}
		}
	}
    
    protected void logPullProgress(Trace trace, ProgressMessage progress) {
		if (trace.isDebugEnabled()) {
			if ("downloading".equalsIgnoreCase(progress.status())) {
				//ommit donloading messages
				return;
			}
			trace.event(Level.DEBUG, "pull progress (id, status, progress, error)", progress.id(), progress.status(), progress.progress(), progress.error());
		}
	}
    
    protected DockerClient obtainDockerClient() {
		return clients.computeIfAbsent(
			Thread.currentThread().getId(), 
			(Long threadId) -> clientBuilder.build()
		);
	}
}