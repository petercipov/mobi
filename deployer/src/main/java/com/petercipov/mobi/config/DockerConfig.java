package com.petercipov.mobi.config;

import com.petercipov.mobi.ExplicitTag;
import com.petercipov.mobi.Registry;
import com.google.common.collect.Iterables;
import java.util.List;
import java.util.Set;

/**
 *
 * @author pcipov
 */
public class DockerConfig {
	
	private final Set<ApiHost> api;
	private final Registry repository;
	private final List<ExplicitTag> explicitTags;

	public DockerConfig(Set<ApiHost> api, Registry repository, List<ExplicitTag> explicitTags) {
		this.api = api;
		this.repository = repository;
		this.explicitTags = explicitTags;
	}

	public Set<ApiHost> getApi() {
		return api;
	}
	
	public ApiHost getRandomApiHost() {
		Set<ApiHost> apis = getApi();
		if (apis.isEmpty()) {
			throw new IllegalStateException("empty apis, expecting at least one");
		}
		int id = Math.abs((int)System.currentTimeMillis()) % apis.size();
		return Iterables.get(apis, id);
	}

	public Registry getRegistry() {
		return repository;
	}

	public List<ExplicitTag> getExplicitTags() {
		return explicitTags;
	}

	@Override
	public String toString() {
		return "DockerConfig(" + "\napi=" + api + ", \nrepository=" + repository + ", \nexplicitImages=" + explicitTags + ')';
	}
}