package com.petercipov.mobi.deployer;

/**
 *
 * @author Peter Cipov
 */
public class PortBinding {
	private final String host;
	private final int port;

	public PortBinding(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}
}
