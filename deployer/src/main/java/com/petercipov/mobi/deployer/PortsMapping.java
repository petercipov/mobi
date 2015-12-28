package com.petercipov.mobi.deployer;

import com.spotify.docker.client.messages.PortBinding;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author pcipov
 */
public class PortsMapping {
	private final Map<String, List<PortBinding>> bindings;

	public PortsMapping() {
		this(new HashMap<>());
	}

	private PortsMapping(Map<String, List<PortBinding>> bindings) {
		this.bindings = Collections.unmodifiableMap(bindings);
	}
	
	public PortsMapping map(String containerPort, int dockerHostPort) {
		Map<String, List<PortBinding>> newBindings = new HashMap<>(bindings);
		newBindings.put(containerPort, Arrays.asList(PortBinding.of(null, dockerHostPort)));
		return new PortsMapping(newBindings);
	}

	Map<String, List<PortBinding>> getPortBindings() {
		return bindings;
	}
	
	Collection<String> getExposedPorts() {
		return bindings.keySet();
	}
}
