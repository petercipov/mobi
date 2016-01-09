package com.petercipov.mobi.deployer.spotify;

import com.petercipov.mobi.Image;
import com.petercipov.mobi.deployer.Deployment;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


/**
 *
 * @author Peter Cipov
 */
public class SpotifyDeployment <I extends Image> extends Deployment<I, ContainerConfig> {
    
    private final ContainerConfig.Builder containerConfig;
	private final HostConfig.Builder hostConfig;

    public SpotifyDeployment(I image) {
        super(image);
        this.containerConfig = ContainerConfig.builder();
		this.hostConfig = HostConfig.builder();
    }

    @Override
    public Deployment<I, ContainerConfig> volume(String hostPath, String guestPath) {
        return volume(hostPath+":"+guestPath);
    }

	@Override
	public Deployment<I, ContainerConfig> volume(String... volumeBindings) {
		return volume(Arrays.asList(volumeBindings));
	}

	@Override
	public Deployment<I, ContainerConfig> volume(Collection<String> volumeBindings) {
		List<String> volumes = new LinkedList<>(volumeBindings);
		if (this.hostConfig.binds() != null) {
            volumes.addAll(this.hostConfig.binds());
        }

        this.hostConfig.binds(volumes);
		return this;
	}

    @Override
    public Deployment<I, ContainerConfig> env(String variable) {
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
	
	
	@Override
	public Deployment<I, ContainerConfig> allPortsPublished(boolean enabled) {
		this.hostConfig.publishAllPorts(enabled);
		return this;
	}

    @Override
    public Deployment<I, ContainerConfig> port(String port, int customPort) {
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
	
	@Override
	public boolean portsSpecified() {
		return this.hostConfig.portBindings() == null 
			|| this.hostConfig.portBindings().isEmpty();
	}
		
    @Override
	public ContainerConfig build() {
        return this.containerConfig
            .image(image.toString())
            .hostConfig(this.hostConfig.build())
            .build();
    }
}
