package com.petercipov.mobi.deployer;

import com.petercipov.mobi.Image;
import com.petercipov.traces.api.Trace;
import com.spotify.docker.client.messages.ContainerInfo;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import rx.Notification;
import rx.Observable;

/**
 *
 * @author pcipov
 */
public class Container<T extends Image> {
	private final String containerId;
	private final T image;
	private final String host;
	private final Map<String, Integer> ports;
	private final Deployer deployer;

	public Container(String containerId, T image, String host, Map<String, Integer> ports, Deployer deployer) {
		this.containerId = containerId;
		this.image = image;
		this.host = host;
		this.ports = Collections.unmodifiableMap(ports);
		this.deployer = deployer;
	}

	public String getContainerId() {
		return containerId;
	}

	public Map<String, Integer> getPorts() {
		return ports;
	}

	public String getHost() {
		return host;
	}

	public T getImage() {
		return image;
	}
	
	public void close(Trace trace) throws IOException {
		
		List<Notification<Container<T>>> results = stop(trace, 15)
			.materialize().toList().toBlocking().single();
		
		if (results.get(0).isOnError()) {
			throw new IOException("could not close container "+containerId, results.get(0).getThrowable());
		}
	}
	
	public Observable<ContainerInfo> inspect(Trace trace) {
		return deployer.inspectContainer(trace, this);
	}
	
	public Observable<Container<T>> kill(Trace trace) {
		return deployer.killContainer(trace, this);
	}
	
	public Observable<Container<T>> stop(Trace trace, int secondsBefaoreKill) {
		return deployer.stopContainer(trace, this, secondsBefaoreKill);
	}

	@Override
	public String toString() {
		return "container id: "+containerId+", image:"+image+ ", host: "+host+", exposed ports: "+ports;
	}
}
