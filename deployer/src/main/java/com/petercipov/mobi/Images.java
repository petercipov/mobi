package com.petercipov.mobi;

import java.util.List;

public class Images {
	
	protected final Registry registry;
	protected final List<ExplicitTag> explicitTags;

	public Images(Registry registry, List<ExplicitTag> explicitTags) {
		this.registry = registry;
		this.explicitTags = explicitTags;
	}

	public Registry getRegistry() {
		return registry;
	}

	public List<ExplicitTag> getExplicitTags() {
		return explicitTags;
	}
}