package com.petercipov.mobi.deployer.spotify;

import com.petercipov.mobi.deployer.RxDocker;
import com.spotify.docker.client.messages.ContainerInfo;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 * @author petercipov
 */
public class SpotifyInspectWrapper implements RxDocker.ContainerInfo {

	private final ContainerInfo container;
	private final NetworkSettings networkSettings;
	private final ContainerState containerState;
	private final HostConfig hostConfig;
	private final ContainerConfig containerConfig;

	public SpotifyInspectWrapper(ContainerInfo info) {
		this.container = info;
		this.networkSettings = new NetworkSettings(this.container.networkSettings());
		this.containerState = new ContainerState(this.container.state());
		this.hostConfig = new HostConfig(this.container.hostConfig());
		this.containerConfig = new ContainerConfig(this.container.config());
	}

	@Override
	public String id() {
		return container.id();
	}

	@Override
	public Date created() {
		return container.created();
	}

	@Override
	public String path() {
		return container.path();
	}

	@Override
	public List<String> args() {
		return this.container.args();
	}

	@Override
	public String image() {
		return container.image();
	}

	@Override
	public String resolvConfPath() {
		return container.resolvConfPath();
	}

	@Override
	public String hostnamePath() {
		return container.hostnamePath();
	}

	@Override
	public String hostsPath() {
		return container.hostsPath();
	}

	@Override
	public String name() {
		return container.hostsPath();
	}

	@Override
	public String driver() {
		return container.driver();
	}

	@Override
	public String execDriver() {
		return container.execDriver();
	}

	@Override
	public String processLabel() {
		return container.processLabel();
	}

	@Override
	public String mountLabel() {
		return container.mountLabel();
	}

	@Override
	public Map<String, String> volumes() {
		return container.volumes();
	}

	@Override
	public Map<String, Boolean> volumesRW() {
		return container.volumesRW();
	}

	@Override
	public RxDocker.ContainerConfig config() {
		return this.containerConfig;
	}

	@Override
	public RxDocker.HostConfig hostConfig() {
		return this.hostConfig;
	}

	@Override
	public RxDocker.ContainerState state() {
		return this.containerState;
	}

	@Override
	public RxDocker.NetworkSettings networkSettings() {
		return this.networkSettings;
	}
	
	private static class ContainerConfig implements RxDocker.ContainerConfig {
		
		private final com.spotify.docker.client.messages.ContainerConfig containerConfig;

		public ContainerConfig(com.spotify.docker.client.messages.ContainerConfig containerConfig) {
			this.containerConfig = containerConfig;
		}
		
		@Override
		public String hostname() {
			return this.containerConfig.hostname();
		}

		@Override
		public String domainname() {
			return this.containerConfig.domainname();
		}

		@Override
		public String user() {
			return this.containerConfig.user();
		}

		@Override
		public Boolean attachStdin() {
			return this.containerConfig.attachStdin();
		}

		@Override
		public Boolean attachStdout() {
			return this.containerConfig.attachStdout();
		}

		@Override
		public Boolean attachStderr() {
			return this.containerConfig.attachStderr();
		}

		@Override
		public List<String> portSpecs() {
			return this.containerConfig.portSpecs();
		}

		@Override
		public Set<String> exposedPorts() {
			return this.containerConfig.exposedPorts();
		}

		@Override
		public Boolean tty() {
			return this.containerConfig.tty();
		}

		@Override
		public Boolean openStdin() {
			return this.containerConfig.openStdin();
		}

		@Override
		public Boolean stdinOnce() {
			return this.containerConfig.stdinOnce();
		}

		@Override
		public List<String> env() {
			return this.containerConfig.env();
		}

		@Override
		public List<String> cmd() {
			return this.containerConfig.cmd();
		}

		@Override
		public String image() {
			return this.containerConfig.image();
		}

		@Override
		public Set<String> volumes() {
			return this.containerConfig.volumes();
		}

		@Override
		public String workingDir() {
			return this.containerConfig.workingDir();
		}

		@Override
		public List<String> entrypoint() {
			return this.containerConfig.entrypoint();
		}

		@Override
		public Boolean networkDisabled() {
			return this.containerConfig.networkDisabled();
		}

		@Override
		public List<String> onBuild() {
			return this.containerConfig.onBuild();
		}

		@Override
		public Map<String, String> labels() {
			return this.containerConfig.labels();
		}

		@Override
		public String macAddress() {
			return this.containerConfig.macAddress();
		}
	
	}
	
	private static class HostConfig implements RxDocker.HostConfig {

		private final com.spotify.docker.client.messages.HostConfig hostConfig;

		public HostConfig(com.spotify.docker.client.messages.HostConfig hostConfig) {
			this.hostConfig = hostConfig;
		}
		
		@Override
		public List<String> binds() {
			return this.hostConfig.binds();
		}

		@Override
		public Boolean privileged() {
			return this.hostConfig.privileged();
		}

		@Override
		public List<String> links() {
			return this.hostConfig.links();
		}

		@Override
		public Boolean publishAllPorts() {
			return this.hostConfig.publishAllPorts();
		}

		@Override
		public List<String> dns() {
			return this.hostConfig.dns();
		}

		@Override
		public List<String> dnsSearch() {
			return this.hostConfig.dnsSearch();
		}

		@Override
		public List<String> extraHosts() {
			return this.hostConfig.extraHosts();
		}

		@Override
		public List<String> volumesFrom() {
			return this.hostConfig.volumesFrom();
		}

		@Override
		public String networkMode() {
			return this.hostConfig.networkMode();
		}

		@Override
		public List<String> securityOpt() {
			return this.hostConfig.securityOpt();
		}

		@Override
		public Long memory() {
			return this.hostConfig.memory();
		}

		@Override
		public Long memorySwap() {
			return this.hostConfig.memorySwap();
		}

		@Override
		public Long cpuShares() {
			return this.hostConfig.cpuShares();
		}

		@Override
		public String cpusetCpus() {
			return this.hostConfig.cpusetCpus();
		}

		@Override
		public Long cpuQuota() {
			return this.hostConfig.cpuQuota();
		}

		@Override
		public String cgroupParent() {
			return this.hostConfig.cgroupParent();
		}
	}
	
	private static class ContainerState implements RxDocker.ContainerState {
		
		private final com.spotify.docker.client.messages.ContainerState containerState;

		public ContainerState(com.spotify.docker.client.messages.ContainerState containerState) {
			this.containerState = containerState;
		}

		@Override
		public Boolean running() {
			return this.containerState.running();
		}

		@Override
		public Boolean paused() {
			return this.containerState.paused();
		}

		@Override
		public Boolean restarting() {
			return this.containerState.restarting();
		}

		@Override
		public Integer pid() {
			return this.containerState.pid();
		}

		@Override
		public Integer exitCode() {
			return this.containerState.exitCode();
		}

		@Override
		public Date startedAt() {
			return this.containerState.startedAt();
		}

		@Override
		public Date finishedAt() {
			return this.containerState.finishedAt();
		}

		@Override
		public String error() {
			return this.containerState.error();
		}

		@Override
		public Boolean oomKilled() {
			return this.containerState.oomKilled();
		}
	}
	
	private static class NetworkSettings implements RxDocker.NetworkSettings{

		private final com.spotify.docker.client.messages.NetworkSettings settings;
		private final Map<String, List<RxDocker.PortBinding>> ports;

		public NetworkSettings(com.spotify.docker.client.messages.NetworkSettings settings) {
			this.settings = settings;
			this.ports = wrapPorts();
		}
		
		@Override
		public String ipAddress() {
			return this.settings.ipAddress();
		}

		@Override
		public Integer ipPrefixLen() {
			return this.settings.ipPrefixLen();
		}

		@Override
		public String gateway() {
			return this.settings.gateway();
		}

		@Override
		public String bridge() {
			return this.settings.bridge();
		}

		@Override
		public Map<String, Map<String, String>> portMapping() {
			return this.settings.portMapping();
		}

		@Override
		public Map<String, List<RxDocker.PortBinding>> ports() {
			return this.ports;
		}

		private HashMap<String, List<RxDocker.PortBinding>> wrapPorts() {
			HashMap<String, List<RxDocker.PortBinding>> wrappedPorts = new HashMap<>();
			for (Entry<String, List<com.spotify.docker.client.messages.PortBinding>> e : this.settings.ports().entrySet()) {
				List<RxDocker.PortBinding> binding = new ArrayList<>(e.getValue().size());
				
				for (com.spotify.docker.client.messages.PortBinding p : e.getValue()) {
					binding.add(new PortBinding(p));
				}
				
				wrappedPorts.put(e.getKey(), binding);
			}
			return wrappedPorts;
		}

		@Override
		public String macAddress() {
			return this.settings.macAddress();
		}
	}
	
	private static class PortBinding implements RxDocker.PortBinding {
		
		private final com.spotify.docker.client.messages.PortBinding binding;
		private final int port;

		public PortBinding(com.spotify.docker.client.messages.PortBinding binding) {
			this.binding = binding;
			this.port = Integer.parseInt(this.binding.hostPort());
		}

		@Override
		public String hostIp() {
			return this.binding.hostIp();
		}

		@Override
		public int hostPort() {
			return port;
		}
	}
}
