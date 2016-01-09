package com.petercipov.mobi.config;

import java.util.List;
import java.util.Optional;

/**
 *
 * @author pcipov
 */
public class HttpsRestApiHost extends ApiHost {
	private final String host;
	private final int port;
	private final String certPath;

	public HttpsRestApiHost(String id, String host, int port, String certPath, Optional<List<String>> volumeBindings) {
		super(id, volumeBindings);
		this.host = host;
		this.port = port;
		this.certPath = certPath;
	}
	
	@Override
	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public String getCertPath() {
		return certPath;
	}
	
	public String getUri() {
		return "https://"+ this.getHost() + ":" + this.getPort();
	}
	
	@Override
	public <T> T setupBuilder(Builder<T> b) {
		return b.build(this);
	}
	
	@Override
	public String toString() {
		return "HttpsRestApiHost(" + "id=" + getId() + ", host=" + host + ", port=" + port + ", certPath=" + certPath + ", volumeBindings=" + getDefaultVolumeBindings() + ')';
	}
}