package com.petercipov.mobi;

import com.spotify.docker.client.DefaultDockerClient;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 *
 * @author pcipov
 */
public abstract class ApiHost {

	private final String id;
	private final Optional<List<String>> defaultVolumeBindings;

	public ApiHost(String id, Optional<List<String>> volumeBindings) {
		this.id = id;
		this.defaultVolumeBindings = volumeBindings;
	}

	public String getId() {
		return id;
	}
	
	public abstract String getHost();

	public abstract DefaultDockerClient.Builder setupBuilder(DefaultDockerClient.Builder builder);

	public Optional<List<String>> getDefaultVolumeBindings() {
		return defaultVolumeBindings;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 29 * hash + Objects.hashCode(this.id);
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
		final ApiHost other = (ApiHost) obj;
		if (!Objects.equals(this.id, other.id)) {
			return false;
		}
		return true;
	}
}
