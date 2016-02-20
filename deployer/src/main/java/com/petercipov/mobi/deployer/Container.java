package com.petercipov.mobi.deployer;

import com.petercipov.mobi.Instance;
import com.petercipov.mobi.deployer.RxDocker.PortBinding;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 * @author pcipov
 */
public class Container {
	private final String containerId;
	private final Instance image;
	private final Map<String, List<PortBinding>> ports;

	public Container(String containerId, Instance image, Map<String, List<PortBinding>> ports) {
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
	
	public int getPort(String portDef) {
		return getPorts().getOrDefault(portDef, Collections.emptyList()).get(0).hostPort();
	}

	public Instance getImage() {
		return image;
	}
	
	@Override
	public String toString() {
		return "container id: "+containerId+", image:"+image+ ", exposed ports: "+ports;
	}
}
