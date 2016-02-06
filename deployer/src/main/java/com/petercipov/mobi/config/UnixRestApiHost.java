package com.petercipov.mobi.config;

import java.util.List;
import java.util.Optional;

/**
 *
 * @author pcipov
 */
public class UnixRestApiHost extends ApiHost {
	private static final String UNIX_SOCKET_SCHEME = "unix://";
	
	private final String path;

	public UnixRestApiHost(String path, Optional<List<String>> volumeBindings) {
		super(volumeBindings);
		this.path = path;
	}


	public String getPath() {
		return path;
	}
	
	public String getUri() {
		return UNIX_SOCKET_SCHEME + path;
	}
	
	@Override
	public <T> T setupBuilder(Builder<T> b) {
		return b.build(this);
	}

	@Override
	public String toString() {
		return "UnixRestApiHost(path=" + path +", volumeBindings=" + getVolumeBindings() + ')';
	}
}