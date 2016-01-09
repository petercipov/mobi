package com.petercipov.mobi.config;

import java.util.List;
import java.util.Optional;

/**
 *
 * @author pcipov
 */
public class HttpRestApiHost extends ApiHost {
	private final String host;
	private final int port;

	public HttpRestApiHost(String id, String host, int port, Optional<List<String>> volumeBindings) {
		super(id, volumeBindings);
		this.host = host;
		this.port = port;
	}

	@Override
	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public String getUri() {
		return "http://"+ this.getHost() + ":" + this.getPort();
	}
	
	@Override
	public String toString() {
		return "HttpRestApiHost(" + "id=" + getId() + ", host=" + host + ", port=" + port + ", volumeBindings=" + getDefaultVolumeBindings() + ')';
	}
	
	@Override
	public <T> T setupBuilder(Builder<T> b) {
		return b.build(this);
	}
}