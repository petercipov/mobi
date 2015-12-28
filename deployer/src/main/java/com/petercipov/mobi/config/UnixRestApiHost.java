package com.petercipov.mobi.config;

import com.petercipov.mobi.ApiHost;
import com.spotify.docker.client.DefaultDockerClient;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author pcipov
 */
public class UnixRestApiHost extends ApiHost {
	private static final String UNIX_SOCKET_SCHEME = "unix://";
	
	private final String path;
	private final String host;

	public UnixRestApiHost(String id, String path, String host, Optional<List<String>> volumeBindings) {
		super(id, volumeBindings);
		this.path = path;
		this.host = host;
	}

	@Override
	public String getHost() {
		return host;
	}

	public String getPath() {
		return path;
	}
	
	public String getUri() {
		return UNIX_SOCKET_SCHEME + path;
	}
	
	@Override
	public DefaultDockerClient.Builder setupBuilder(DefaultDockerClient.Builder builder) {
		builder.uri(getUri());
		return builder;
	}

	@Override
	public String toString() {
		return "UnixRestApiHost(" + "id=" + getId() + ", path=" + path +", host=" + host +", volumeBindings=" + getDefaultVolumeBindings() + ')';
	}
}
