package com.petercipov.mobi.deployer;

import com.petercipov.mobi.Image;
import com.petercipov.traces.api.Trace;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import rx.Observable;

/**
 *
 * @author Peter Cipov
 */
public interface RxDocker <O extends Options> {
    
    Observable<Image> pull(Trace trace, Image image);
    Observable<Boolean> isPresent(Trace trace, Image image);
    
    Observable<String> createContainer(Trace trace, Image image, O deployment);
    Observable<String> startContainer(Trace trace, String containerId);
    Observable<String> killContainer(Trace trace, String containerId);
    Observable<String> stopContainer(Trace trace, String containerId, int secondsBeforeFail);
    Observable<String> removeContainer(Trace trace, String containerId);
	
	Observable<ContainerInfo> inspectContainer(Trace trace, String containerId);
	
	interface ContainerInfo {
		String id();
		Date created();
		String path();
		List<String> args();
		String image();
		String resolvConfPath();
		String hostnamePath();
		String hostsPath();
		String name();
		String driver();
		String execDriver();
		String processLabel();
		String mountLabel();
		Map<String, String> volumes();
		Map<String, Boolean> volumesRW();
		ContainerConfig config();
		HostConfig hostConfig();
		ContainerState state();
		NetworkSettings networkSettings();
	}
	
	interface ContainerConfig {
		String hostname();
		String domainname();
		String user();
		Boolean attachStdin();
		Boolean attachStdout();
		Boolean attachStderr();
		List<String> portSpecs();
		Set<String> exposedPorts();
		Boolean tty();
		Boolean openStdin();
		Boolean stdinOnce();
		List<String> env();
		List<String> cmd();
		String image();
		Set<String> volumes();
		String workingDir();
		List<String> entrypoint();
		Boolean networkDisabled();
		List<String> onBuild();
		Map<String, String> labels();
		String macAddress();
	}
	
	interface HostConfig {
		List<String> binds();
		Boolean privileged();
		List<String> links();
		Boolean publishAllPorts();
		List<String> dns();
		List<String> dnsSearch();
		List<String> extraHosts();
		List<String> volumesFrom();
		String networkMode();
		List<String> securityOpt();
		Long memory();
		Long memorySwap();
		Long cpuShares();
		String cpusetCpus();
		Long cpuQuota();
		String cgroupParent();
	}

	interface PortBinding {
		String hostIp();
		int hostPort();
	}
	
	interface ContainerState {
		Boolean running();
		Boolean paused();
		Boolean restarting();
		Integer pid();
		Integer exitCode();
		Date startedAt();
		Date finishedAt();
		String error();
		Boolean oomKilled();
	}
	
	interface NetworkSettings {
		String ipAddress();
		Integer ipPrefixLen();
		String gateway();
		String bridge();
		Map<String, Map<String, String>> portMapping();
		Map<String, List<PortBinding>> ports();
		String macAddress();
	}
	
}
