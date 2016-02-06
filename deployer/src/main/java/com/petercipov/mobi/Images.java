package com.petercipov.mobi;

import java.util.List;

public class Images {
	
	protected final Registry registry;
	protected final List<TagOverride> explicitTags;

	public Images(Registry registry, List<TagOverride> explicitTags) {
		this.registry = registry;
		this.explicitTags = explicitTags;
	}

	public Registry getRegistry() {
		return registry;
	}

	public List<TagOverride> getExplicitTags() {
		return explicitTags;
	}
}