package com.petercipov.mobi;

import java.util.List;

/**
 *
 * @author pcipov
 */
public  class Builder {

	private final Registry registry;
	private final List<TagOverride> explicitTags;

	public Builder(Registry registry, List<TagOverride> explicitTags) {
		this.registry = registry;
		this.explicitTags = explicitTags;
	}
	
	public Instance build(Name image, String tag) {
		
		String override = tag;
		
		for (TagOverride t : explicitTags) {
			if (t.getRepository().equals(image.getRepository())
			&&	t.getName().equals(image.getName())
			&&	t.getTag().equals(tag)) {
				override = t.getOverride();
			}
		}
		return new Instance(registry, image, override);
	}

}
