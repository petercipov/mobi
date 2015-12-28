package com.petercipov.mobi.deployer;

import com.petercipov.mobi.Image;
import com.petercipov.traces.api.Level;
import com.petercipov.traces.api.Trace;
import com.petercipov.traces.api.Trace.Event;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.ProgressHandler;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerInfo;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;

/**
 *
 * @author pcipov
 */
public class RxDocker {
	
	private final DefaultDockerClient.Builder clientBuilder;
	private final Scheduler scheduler;
	private final Map<Long, DockerClient> clients;

	public RxDocker(DefaultDockerClient.Builder builder, Scheduler scheduler) {
		this.clientBuilder = builder;
		this.scheduler = scheduler;
		this.clients = Collections.synchronizedMap(new HashMap<>());
	}

	public Observable<Image> pull(Trace trace, Image image, ProgressHandler handler) {
		return Observable.create((Subscriber<? super Image> subscriber) -> {
			Event pulling = trace.start("RxDocker: pulling image, (image)", image);
			final DockerClient client;
			try {
				client = obtainDockerClient();
				if (handler == null) {
					client.pull(image.toString());
				} else {
					client.pull(image.toString(), handler);
				}
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
	
	public Observable<Boolean> isImagePresent(Trace trace, Image image) {
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
	
	public Observable<List<com.spotify.docker.client.messages.Image>> listImages(Trace trace) {
		return Observable.create((Subscriber<? super List<com.spotify.docker.client.messages.Image>> subscriber) -> {
			Event listing = trace.start("RxDocker: listing images");
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
			Event listing = trace.start("RxDocker: listing containers");
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
	
	public Observable<String> createContainer(Trace trace, ContainerConfig containerConfig) {
		return createContainer(trace, Optional.empty(), containerConfig);
	}
	
	public Observable<String> createContainer(Trace trace, Optional<String> name, ContainerConfig containerConfig) {
		return Observable.create((Subscriber<? super String> subscriber) -> {
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
	
	public Observable<ContainerInfo> inspectContainer(Trace trace, Container<? extends Image> container) {
		return inspectContainer(trace, container.getContainerId());
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
	
	protected DockerClient obtainDockerClient() {
		return clients.computeIfAbsent(
			Thread.currentThread().getId(), 
			(Long threadId) -> clientBuilder.build()
		);
	}
	
	private void closeAllClients(Trace trace) {
		synchronized(clients) {
			for (DockerClient client : clients.values()) {
				client.close();
			}
		}
		trace.event("Deployer: closed all containers");
	}
	
	public void close(Trace trace) {
		closeAllClients(trace);
	}
}