package com.petercipov.mobi.deployer;

import com.petercipov.mobi.Image;
import com.petercipov.mobi.deployer.RxDocker.PortBinding;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 * @author pcipov
 */
public class Container<T extends Image> {
	private final String containerId;
	private final T image;
	private final Map<String, List<PortBinding>> ports;

	public Container(String containerId, T image, Map<String, List<PortBinding>> ports) {
		this.containerId = containerId;
		this.image = image;
		this.ports = Collections.unmodifiableMap(ports);
	}

	public String getContainerId() {
		return containerId;
	}

	public Map<String, List<PortBinding>> getPorts() {
		return ports;
	}

	public T getImage() {
		return image;
	}
	
	@Override
	public String toString() {
		return "container id: "+containerId+", image:"+image+ ", exposed ports: "+ports;
	}
}
