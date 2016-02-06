package com.petercipov.mobi.images;

import com.petercipov.mobi.Images;
import com.petercipov.mobi.Registry;
import com.petercipov.mobi.TagOverride;
import com.petercipov.mobi.config.ImageBuilder;
import java.util.List;

public class STDImages extends Images {

	private final ImageBuilder<CassandraImage> cassandra;
	
	public STDImages(Registry registry, List<TagOverride> explicitTags) {
		super(registry, explicitTags);
		cassandra = ImageBuilder.create(tag -> new CassandraImage(registry, tag), explicitTags);
	}

	public ImageBuilder<CassandraImage> cassandra() {
		return cassandra;
	}
	
}
