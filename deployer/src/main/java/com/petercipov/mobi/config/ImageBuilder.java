package com.petercipov.mobi.config;

import com.petercipov.mobi.TagOverride;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.petercipov.mobi.Image;
import java.util.List;
import java.util.function.Function;

/**
 *
 * @author pcipov
 */
public  class ImageBuilder <T extends Image> {
	private static final String MASTER_TAG = "master";
	private static final String LATEST_TAG = "latest";
	
	private final Function<String, T> buildr;
	private final List<TagOverride> explicitTags;

	public ImageBuilder(Function<String, T> buildr, List<TagOverride> explicitTags) {
		this.buildr = buildr;
		this.explicitTags = explicitTags;
	}

	public T forTag(String tag) {
		String overriden = tag;
		for(TagOverride explicit : explicitTags) {
			if (explicit.getTag().equals(tag)) {
				overriden = explicit.getOverride();
				break;
			}
		}
		return buildr.apply(overriden);
	}

	public T forMaster() {
		return forTag(MASTER_TAG);
	}
	
	public T forLatest() {
		return forTag(LATEST_TAG);
	}
	
	public static <T extends Image> ImageBuilder<T> create(Function<String, T> buildFunc, List<TagOverride> overrides) {
		T image = buildFunc.apply(MASTER_TAG);
		
		List<TagOverride> tags = Lists.newLinkedList(Iterables.filter(overrides,
			(i) -> 
				i.getRepository().equals(image.getRepository())
				&& i.getName().equals(image.getName())
		));
		
		return new ImageBuilder<>(buildFunc, tags);
	}
}
