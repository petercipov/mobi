package com.petercipov.mobi.config;

import com.petercipov.mobi.Registry;
import java.util.Objects;

/**
 *
 * @author pcipov
 */
public class RemoteRegistry implements Registry {
	private final String host;
	private final int port;

	public RemoteRegistry(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	@Override
	public String toString() {
		return "Registry(" + "host=" + host + ", port=" + port + ')';
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 41 * hash + Objects.hashCode(this.host);
		hash = 41 * hash + this.port;
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final RemoteRegistry other = (RemoteRegistry) obj;
		if (!Objects.equals(this.host, other.host)) {
			return false;
		}
		if (this.port != other.port) {
			return false;
		}
		return true;
	}

	@Override
	public String getConnectionString() {
		return getHost()+":"+getPort()+"/";
	}
}
