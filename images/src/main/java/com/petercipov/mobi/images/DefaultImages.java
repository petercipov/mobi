package com.petercipov.mobi.images;

import com.petercipov.mobi.Images;
import com.petercipov.mobi.config.ImageBuilder;
import com.petercipov.mobi.config.MobiConfig;

public class DefaultImages extends Images {

	private final ImageBuilder<CassandraImage> cassandra;
	
	public DefaultImages(MobiConfig config) {
		super(config.getRegistry(), config.getTags());
		cassandra = ImageBuilder.create(new CassandraImage(), registry, explicitTags);
	}

	public ImageBuilder<CassandraImage> cassandra() {
		return cassandra;
	}
}
