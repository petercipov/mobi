package com.petercipov.mobi.deployer;

import com.petercipov.mobi.Image;
import java.util.Optional;

/**
 *
 * @author Peter Cipov
 */
public abstract class Deployment <I extends Image, R> {
	protected final I image;
	protected Optional<String> name;

    public Deployment(I image) {
        this.image = image;
		this.name = Optional.empty();
    }
    
	public I image() {
		return image;
	}

	public Optional<String> name() {
		return name;
	}
    
	public abstract boolean portsSpecified();

	/**
	 * Sets container name
	 * @since 1.14
	 * @param name
	 * @return 
	 */	
    public Deployment<I, R> setName(String name) {
        this.name = Optional.of(name);
        return this;
    }
	
	/**
	 * Adds volume bindings to container as string in format /host/path:/container/path
	 * @since 1.14
	 * @param volumeBindings  iterable of bindings 
	 * @return 
	 */
	public abstract Deployment<I, R> addVolumes(Iterable<String> volumeBindings);
	/**
	 * Adds volume binding
	 * @since 1.14
	 * @param hostPath
	 * @param containerPath
	 * @return 
	 */
    public abstract Deployment<I, R> addVolume(String hostPath, String containerPath);
	/**
	 * Adds environment variable to container 
	 * @since 1.14
	 * @param variable variable in a format NAME=VALUE 
	 * @return 
	 */
    public abstract Deployment<I, R> addEnv(String variable);
	
	/**
	 * Adds environment variable to container 
	 * @since 1.14
	 * @param name 
	 * @param value
	 * @return 
	 */
    public abstract Deployment<I, R> addEnv(String name, String value);
	
	/**
	 * Add port that should be published
	 * @since 1.14
	 * @param port - container port spect in format [tcp/udp]/port. f.e tcp/8080
	 * @param customPort - remapping port
	 * @return 
	 */
    public abstract Deployment<I, R> addPort(String port, int customPort);
	
	/**
	 * Publishes all exposed ports if is set to true
	 * @since 1.14
	 * @param publish
	 * @return 
	 */
	public abstract Deployment<I, R> setPublishAllPorts(boolean publish);
	
	/**
	 * Runs the command when starting the container
	 * @since 1.14
	 * @param cmd - command in for of single string or multitude of string that
	 * contains parts of command
	 * @return 
	 */
	public abstract Deployment<I, R> setCmd(String ... cmd);
	
	/**
	 * Sets cpu quota
	 * @since 1.19
	 * @param quota Microseconds of CPU time that the container can get in a CPU period
	 * @return 
	 */
	public abstract Deployment<I, R> setCpuQuota(long quota);
	/**
	 * Sets cpu shares
	 * @since  1.14
	 * @param shares  An integer value containing the container’s CPU Shares (ie. the relative weight vs other containers)
	 * @return 
	 */
	public abstract Deployment<I, R> setCpuShares(long shares);
	/**
	 * Sets domain name
	 * @since 1.14
	 * @param name A string value containing the domain name to use for the container.
	 * @return 
	 */
	public abstract Deployment<I, R> setDomainName(String name);
	/**
	 * Sets entry point
	 * @since 1.15
	 * @param entry A command to run inside container. it overrides one specified by container docker file.
	 * @return 
	 */
	public abstract Deployment<I, R> setEntryPoint(String ... entry);
	/**
	 * adds container exposed port
	 * @since 1.14
	 * @param port in format [tcp/udp]/port. f.e tcp/8080
	 * @return 
	 */
	public abstract Deployment<I, R> addExposedPort(String port);
	/**
	 * Sets hostname
	 * @since 1.14
	 * @param hostName A string value containing the hostname to use for the container.
	 * @return 
	 */
	public abstract Deployment<I, R> setHostName(String hostName);
	/**
	 * Adds label
	 * @since 1.18
	 * @param key
	 * @param value
	 * @return 
	 */
    public abstract Deployment<I, R> addLabel(String key, String value);
	/**
	 * Sets MAC address.
	 * @since 1.15
	 * @param mac
	 * @return 
	 */
	public abstract Deployment<I, R> setMacAdress(String mac);
	/**
	 * Sets memory limits
	 * @since 1.14
	 * @param memory Memory limit in bytes
	 * @return 
	 */
	public abstract Deployment<I, R> setMemory(long memory);
	
	/**
	 * Sets memory limit
	 * @since 1.14
	 * @param memory Memory limit in bytes
	 * @param swap Memory limit for swap. Set -1 to disable swap.
	 * @return 
	 */
	public abstract Deployment<I, R> setMemory(long memory, long swap);
	
	/**
	 * Disables networking for the container
	 * @since 1.14
	 * @param disabled
	 * @return 
	 */
	public abstract Deployment<I, R> setNetworkDisabled(boolean disabled);
	/**
	 * Opens stdin
	 * @since 1.14
	 * @param open
	 * @return 
	 */
	public abstract Deployment<I, R> setOpenStdIn(boolean open);
	/**
	 * Opens stdin and closes stdin after the 1. attached client disconnects.
	 * @since 1.14
	 * @param once
	 * @return 
	 */
	public abstract Deployment<I, R> setStdInOnce(boolean once);
	/**
	 * Attaches standard streams to a tty, including stdin if it is not closed.
	 * @since 1.14
	 * @param enabled
	 * @return 
	 */
	public abstract Deployment<I, R> setTty(boolean enabled);
	/**
	 * @since 1.14
	 * @param user  A string value specifying the user inside the containe
	 * @return 
	 */
	public abstract Deployment<I, R> setUser(String user);
	/**
	 * @since 1.14
	 * @param workDir A string specifying the working directory for commands to run in.
	 * @return 
	 */
	public abstract Deployment<I, R> setWorkDir(String workDir);

    public abstract R build();
}
