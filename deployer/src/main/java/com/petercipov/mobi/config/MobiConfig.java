package com.petercipov.mobi.config;

import com.petercipov.mobi.TagOverride;
import com.petercipov.mobi.Registry;
import java.util.List;

/**
 *
 * @author pcipov
 */
public class MobiConfig {
	
	private final Registry registry;	
	private final List<ApiHost> apis;
	private final List<TagOverride> tags;

	public MobiConfig(List<ApiHost> api, Registry registry, List<TagOverride> explicitTags) {
		this.apis = api;
		this.registry = registry;
		this.tags = explicitTags;
	}

	public List<ApiHost> getApis() {
		return apis;
	}
	
	public ApiHost getRandomApiHost() {
		List<ApiHost> hosts = getApis();
		if (hosts.isEmpty()) {
			throw new IllegalStateException("empty apis, expecting at least one");
		}
		int id = Math.abs((int)System.currentTimeMillis()) % hosts.size();
		return hosts.get(id);
	}

	public Registry getRegistry() {
		return registry;
	}

	public List<TagOverride> getTags() {
		return tags;
	}

	@Override
	public String toString() {
		return "MobiConfig(" + "\napi=" + apis + ", \nrepository=" + registry + ", \ntags=" + tags + ')';
	}
}