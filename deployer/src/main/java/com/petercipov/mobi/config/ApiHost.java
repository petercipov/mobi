package com.petercipov.mobi.config;

import java.util.List;
import java.util.Optional;

/**
 *
 * @author pcipov
 */
public abstract class ApiHost {

	private final Optional<List<String>> defaultVolumeBindings;

	public ApiHost(Optional<List<String>> volumeBindings) {
		this.defaultVolumeBindings = volumeBindings;
	}
	
	public interface Builder<T> {
		T build(HttpRestApiHost host);
		T build(HttpsRestApiHost host);
		T build(UnixRestApiHost host);
	}

	public abstract <T> T setupBuilder(Builder<T> b);

	public Optional<List<String>> getVolumeBindings() {
		return defaultVolumeBindings;
	}
}
