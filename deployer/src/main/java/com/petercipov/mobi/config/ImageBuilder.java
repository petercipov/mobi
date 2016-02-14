package com.petercipov.mobi.config;

import com.petercipov.mobi.TagOverride;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.petercipov.mobi.ImageDefinition;
import com.petercipov.mobi.ImageInstance;
import com.petercipov.mobi.Registry;
import java.util.List;

/**
 *
 * @author pcipov
 */
public  class ImageBuilder <T extends ImageDefinition> {
	private static final String MASTER_TAG = "master";
	private static final String LATEST_TAG = "latest";

	private final Registry registry;
	private final T definition;
	private final List<TagOverride> explicitTags;

	public ImageBuilder(Registry registry, T definition, List<TagOverride> explicitTags) {
		this.registry = registry;
		this.definition = definition;
		this.explicitTags = explicitTags;
	}

	public ImageInstance<T> forTag(String tag) {
		String overriden = tag;
		for(TagOverride explicit : explicitTags) {
			if (explicit.getTag().equals(tag)) {
				overriden = explicit.getOverride();
				break;
			}
		}
		return new ImageInstance<>(registry, overriden, definition);
	}

	public ImageInstance<T>  forMaster() {
		return forTag(MASTER_TAG);
	}
	
	public ImageInstance<T>  forLatest() {
		return forTag(LATEST_TAG);
	}
	
	public static <T extends ImageDefinition> ImageBuilder<T> create(T definition, Registry registry, List<TagOverride> overrides) {
		
		List<TagOverride> tags = Lists.newLinkedList(Iterables.filter(overrides,
			(i) -> 
				i.getRepository().equals(definition.getRepository())
				&& i.getName().equals(definition.getName())
		));
		
		return new ImageBuilder<>(registry, definition, tags);
	}
}
