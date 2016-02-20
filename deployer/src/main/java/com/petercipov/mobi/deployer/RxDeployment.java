package com.petercipov.mobi.deployer;

import com.petercipov.mobi.Instance;
import com.petercipov.traces.api.Trace;
import java.util.Optional;
import rx.Observable;

/**
 *
 * @author Peter Cipov
 */
public abstract class RxDeployment {
	protected Optional<String> name;

    public RxDeployment() {
		this.name = Optional.empty();
    }
    
	public Optional<String> name() {
		return name;
	}
    
	/**
	 * Sets container name
	 * @since 1.14
	 * @param name
	 * @return 
	 */	
    public RxDeployment setName(String name) {
        this.name = Optional.of(name);
        return this;
    }
	/**
	 * Adds volume bindings to container as string in format /host/path:/container/path
	 * @since 1.14
	 * @param volumeBindings  iterable of bindings 
	 * @return 
	 */
	public abstract RxDeployment addVolumes(Iterable<String> volumeBindings);
	/**
	 * Adds volume binding
	 * @since 1.14
	 * @param hostPath
	 * @param containerPath
	 * @return 
	 */
    public abstract RxDeployment addVolume(String hostPath, String containerPath);
	/**
	 * Adds environment variable to container 
	 * @since 1.14
	 * @param variable variable in a format NAME=VALUE 
	 * @return 
	 */
    public abstract RxDeployment addEnv(String variable);
	
	/**
	 * Adds environment variable to container 
	 * @since 1.14
	 * @param name 
	 * @param value
	 * @return 
	 */
    public abstract RxDeployment addEnv(String name, String value);
	
	/**
	 * Add port that should be published
	 * @since 1.14
	 * @param port - container port spect in format [tcp/udp]/port. f.e tcp/8080
	 * @param customPort - remapping port
	 * @return 
	 */
    public abstract RxDeployment addPortMapping(String port, int customPort);
	
	/**
	 * Publishes all exposed ports if is set to true
	 * @since 1.14
	 * @param publish
	 * @return 
	 */
	public abstract RxDeployment setPublishAllPorts(boolean publish);
	
	/**
	 * Publishes all exposed ports
	 * @since 1.14
	 * @return 
	 */
	public abstract RxDeployment publishAllPorts();
	
	/**
	 * Runs the command when starting the container
	 * @since 1.14
	 * @param cmd - command in for of single string or multitude of string that
	 * contains parts of command
	 * @return 
	 */
	public abstract RxDeployment setCmd(String ... cmd);
	
	/**
	 * Sets cpu quota
	 * @since 1.19
	 * @param quota Microseconds of CPU time that the container can get in a CPU period
	 * @return 
	 */
	public abstract RxDeployment setCpuQuota(long quota);
	/**
	 * Sets cpu shares
	 * @since  1.14
	 * @param shares  An integer value containing the container’s CPU Shares (ie. the relative weight vs other containers)
	 * @return 
	 */
	public abstract RxDeployment setCpuShares(long shares);
	/**
	 * Sets domain name
	 * @since 1.14
	 * @param name A string value containing the domain name to use for the container.
	 * @return 
	 */
	public abstract RxDeployment setDomainName(String name);
	/**
	 * Sets entry point
	 * @since 1.15
	 * @param entry A command to run inside container. it overrides one specified by container docker file.
	 * @return 
	 */
	public abstract RxDeployment setEntryPoint(String ... entry);
	/**
	 * adds container exposed port
	 * @since 1.14
	 * @param port in format [tcp/udp]/port. f.e tcp/8080
	 * @return 
	 */
	public abstract RxDeployment addExposedPort(String port);
	/**
	 * Sets hostname
	 * @since 1.14
	 * @param hostName A string value containing the hostname to use for the container.
	 * @return 
	 */
	public abstract RxDeployment setHostName(String hostName);
	/**
	 * Adds label
	 * @since 1.18
	 * @param key
	 * @param value
	 * @return 
	 */
    public abstract RxDeployment addLabel(String key, String value);
	/**
	 * Sets MAC address.
	 * @since 1.15
	 * @param mac
	 * @return 
	 */
	public abstract RxDeployment setMacAdress(String mac);
	/**
	 * Sets memory limits
	 * @since 1.14
	 * @param memory Memory limit in bytes
	 * @return 
	 */
	public abstract RxDeployment setMemory(long memory);
	
	/**
	 * Sets memory limit
	 * @since 1.14
	 * @param memory Memory limit in bytes
	 * @param swap Memory limit for swap. Set -1 to disable swap.
	 * @return 
	 */
	public abstract RxDeployment setMemory(long memory, long swap);
	
	/**
	 * Disables networking for the container
	 * @since 1.14
	 * @param disabled
	 * @return 
	 */
	public abstract RxDeployment setNetworkDisabled(boolean disabled);
	/**
	 * Opens stdin
	 * @since 1.14
	 * @param open
	 * @return 
	 */
	public abstract RxDeployment setOpenStdIn(boolean open);
	/**
	 * Opens stdin and closes stdin after the 1. attached client disconnects.
	 * @since 1.14
	 * @param once
	 * @return 
	 */
	public abstract RxDeployment setStdInOnce(boolean once);
	/**
	 * Attaches standard streams to a tty, including stdin if it is not closed.
	 * @since 1.14
	 * @param enabled
	 * @return 
	 */
	public abstract RxDeployment setTty(boolean enabled);
	/**
	 * @since 1.14
	 * @param user  A string value specifying the user inside the containe
	 * @return 
	 */
	public abstract RxDeployment setUser(String user);
	/**
	 * @since 1.14
	 * @param workDir A string specifying the working directory for commands to run in.
	 * @return 
	 */
	public abstract RxDeployment setWorkDir(String workDir);

	/**
	 * Sets path to cgroup
	 * @since 1.18
	 * @param parent Path to cgroups under which the cgroup for the container will be created. If the path is not absolute, the path is considered to be relative to the cgroups path of the init process. Cgroups will be created if they do not already exist
	 * @return 
	 */
	public abstract RxDeployment setCgroupParent(String parent);
	
	/**
	 * Adds DNS for fontainer
	 * @since 1.14
	 * @param dns A list of DNS servers for the container to use
	 * @return 
	 */
	public abstract RxDeployment addDns(String ... dns);
	
	/**
	 * Adds DNS search domains
	 * @since 1.15
	 * @param dns A list of DNS servers for the container to use.
	 * @return 
	 */
	public abstract RxDeployment addDnsSearch(String ... dns);
	
	/**
	 * Adds extra hosts co container /etc/hosts
	 * @since 1.15
	 * @param hosts  A list of hostnames/IP mappings to add to the container’s /etc/hosts file. Specified in the form ["hostname:IP"]
	 * @return 
	 */
	public abstract RxDeployment addExtraHosts(String ... hosts);
	
	/**
	 * Adds links to other containers
	 * @since 1.14
	 * @param links A list of links for the container. Each link entry should be in the form of container_name:alias
	 * @return 
	 */
	public abstract RxDeployment addLinks(String ... links);
	
	/**
	 * Sets LXC specific configurations. These configurations only work when using the lxc execution driver.
	 * @since 1.14
	 * @param key
	 * @param value
	 * @return 
	 */
	public abstract RxDeployment addLxcParameter(String key, String value);
	
	/**
	 * Sets the networking mode for the container
	 * @since 1.15
	 * @param mode Supported values are: bridge, host, and container:name|id
	 * @return 
	 */
	public abstract RxDeployment setNetworkMode(String mode);
	
	/**
	 * Gives the container full access to the host.
	 * @since 1.14
	 * @param privileged
	 * @return 
	 */
	public abstract RxDeployment setPrivileged(boolean privileged);
	/**
	 * @since 1.15
	 * @param opts  string value to customize labels for MLS systems, such as SELinux.
	 * @return 
	 */
	public abstract RxDeployment addSecurityOpt(String ... opts);
	
	/**
	 * Adds volume from an other container
	 * @since 1.14
	 * @param volumes volume to inherit from another container. Specified in the form container name:ro|rw
	 * @return 
	 */
	public abstract RxDeployment addVolumeFrom(String ... volumes);
	
	
	protected abstract Observable<String> createContainer(Trace trace, Instance image);
	
}
