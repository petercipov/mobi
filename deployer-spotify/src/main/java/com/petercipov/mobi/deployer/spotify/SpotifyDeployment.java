package com.petercipov.mobi.deployer.spotify;

import com.google.common.collect.Lists;
import com.petercipov.mobi.Image;
import com.petercipov.mobi.deployer.Deployment;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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
    public Deployment<I, ContainerConfig> addVolume(String hostPath, String guestPath) {
        return addVolumes(Arrays.asList(hostPath+":"+guestPath));
    }

	@Override
	public Deployment<I, ContainerConfig> addVolumes(Iterable<String> volumeBindings) {
		List<String> volumes = Lists.newLinkedList(volumeBindings);
		if (this.hostConfig.binds() != null) {
            volumes.addAll(this.hostConfig.binds());
        }

        this.hostConfig.binds(volumes);
		return this;
	}

    @Override
    public Deployment<I, ContainerConfig> addEnv(String variable) {
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
	public Deployment<I, ContainerConfig> setPublishAllPorts(boolean enabled) {
		this.hostConfig.publishAllPorts(enabled);
		return this;
	}

    @Override
    public Deployment<I, ContainerConfig> addPort(String port, int customPort) {
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

	@Override
	public Deployment<I, ContainerConfig> setWorkDir(String workDir) {
		this.containerConfig.workingDir(workDir);
		return this;
	}

	@Override
	public Deployment<I, ContainerConfig> setUser(String user) {
		this.containerConfig.user(user);
		return this;
	}

	@Override
	public Deployment<I, ContainerConfig> setCmd(String ... cmd) {
		this.containerConfig.cmd(cmd);
		return this;
	}

	@Override
	public Deployment<I, ContainerConfig> setCpuQuota(long quota) {
		this.containerConfig.cpuQuota(quota);
		return this;
	}

	@Override
	public Deployment<I, ContainerConfig> setCpuShares(long shares) {
		this.containerConfig.cpuShares(shares);
		return this;
	}

	@Override
	public Deployment<I, ContainerConfig> setDomainName(String name) {
		this.containerConfig.domainname(name);
		return this;
	}

	@Override
	public Deployment<I, ContainerConfig> setEntryPoint(String... entry) {
		this.containerConfig.entrypoint(entry);
		return this;
	}

	@Override
	public Deployment<I, ContainerConfig> addExposedPort(String port) {
		HashSet<String> ports = new HashSet<>();
		ports.add(port);
		
		if (this.containerConfig.exposedPorts() != null) {
			ports.addAll(this.containerConfig.exposedPorts());
        }

		this.containerConfig.exposedPorts(ports);
        return this;
	}

	@Override
	public Deployment<I, ContainerConfig> setHostName(String hostName) {
		this.containerConfig.hostname(hostName);
		return this;
	}

	@Override
	public Deployment<I, ContainerConfig> addLabel(String key, String value) {
		HashMap<String, String> labels = new HashMap<>();
		labels.put(key, value);
		if (this.containerConfig.labels() != null) {
			labels.putAll(this.containerConfig.labels());
		}
		this.containerConfig.labels(labels);
		return this;
	}

	@Override
	public Deployment<I, ContainerConfig> setMacAdress(String mac) {
		this.containerConfig.macAddress(mac);
		return this;
	}

	@Override
	public Deployment<I, ContainerConfig> setMemory(long memory) {
		this.containerConfig.memory(memory);
		return this;
	}
	
	@Override
	public Deployment<I, ContainerConfig> setNetworkDisabled(boolean disabled) {
		this.containerConfig.networkDisabled(disabled);
		return this;
	}

	@Override
	public Deployment<I, ContainerConfig> setOpenStdIn(boolean open) {
		this.containerConfig.openStdin(open);
		return this;
	}

	@Override
	public Deployment<I, ContainerConfig> setStdInOnce(boolean once) {
		this.containerConfig.stdinOnce(once);
		return this;
	}

	@Override
	public Deployment<I, ContainerConfig> setTty(boolean enabled) {
		this.containerConfig.tty(enabled);
		return this;
	}

	@Override
	public Deployment<I, ContainerConfig> addEnv(String name, String value) {
		return addEnv(name.trim()+"="+value.trim());
	}

	@Override
	public Deployment<I, ContainerConfig> setMemory(long memory, long swap) {
		this.containerConfig.memory(memory);
		if (swap < 0) {
			this.containerConfig.memorySwap(-1l);
		} else {
			this.containerConfig.memorySwap(memory+swap);
		}
		return this;
	}

	@Override
	public Deployment<I, ContainerConfig> setCgroupParent(String parent) {
		this.hostConfig.cgroupParent(parent);
		return this;
	}

	@Override
	public Deployment<I, ContainerConfig> addDns(String... dns) {
		LinkedList<String> list = new LinkedList<>();
		list.addAll(Arrays.asList(dns));
		if (this.hostConfig.dns() != null) {
			list.addAll(this.hostConfig.dns());
		}
		this.hostConfig.dns(list);
		return this;
	}

	@Override
	public Deployment<I, ContainerConfig> addDnsSearch(String... dns) {
		LinkedList<String> list = new LinkedList<>();
		list.addAll(Arrays.asList(dns));
		if (this.hostConfig.dnsSearch() != null) {
			list.addAll(this.hostConfig.dnsSearch());
		}
		this.hostConfig.dnsSearch(list);
		return this;
	}

	@Override
	public Deployment<I, ContainerConfig> addExtraHosts(String... hosts) {
		LinkedList<String> list = new LinkedList<>();
		list.addAll(Arrays.asList(hosts));
		if (this.hostConfig.extraHosts() != null) {
			list.addAll(this.hostConfig.extraHosts());
		}
		this.hostConfig.extraHosts(list);
		return this;
	}

	@Override
	public Deployment<I, ContainerConfig> addLinks(String... links) {
		LinkedList<String> list = new LinkedList<>();
		list.addAll(Arrays.asList(links));
		if (this.hostConfig.links() != null) {
			list.addAll(this.hostConfig.links());
		}
		this.hostConfig.links(list);
		return this;
	}

	@Override
	public Deployment<I, ContainerConfig> addLxcParameter(String key, String value) {
		LinkedList<HostConfig.LxcConfParameter> list = new LinkedList<>();
		list.add(new HostConfig.LxcConfParameter(key, value));
		
		if (this.hostConfig.lxcConf()!= null) {
			list.addAll(this.hostConfig.lxcConf());
		}
		this.hostConfig.lxcConf(list);
		return this;
	}

	@Override
	public Deployment<I, ContainerConfig> setNetworkMode(String mode) {
		this.hostConfig.networkMode(mode);
		return this;
	}

	@Override
	public Deployment<I, ContainerConfig> setPrivileged(boolean privileged) {
		this.hostConfig.privileged(privileged);
		return this;
	}

	@Override
	public Deployment<I, ContainerConfig> addSecurityOpt(String... opts) {
		LinkedList<String> list = new LinkedList<>();
		list.addAll(Arrays.asList(opts));
		if (this.hostConfig.securityOpt()!= null) {
			list.addAll(this.hostConfig.securityOpt());
		}
		this.hostConfig.securityOpt(list);
		return this;
	}

	@Override
	public Deployment<I, ContainerConfig> addVolumeFrom(String... volumes) {
		LinkedList<String> list = new LinkedList<>();
		list.addAll(Arrays.asList(volumes));
		if (this.hostConfig.volumesFrom()!= null) {
			list.addAll(this.hostConfig.volumesFrom());
		}
		this.hostConfig.volumesFrom(list);
		return this;
	}
}