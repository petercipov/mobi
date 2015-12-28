package com.petercipov.mobi;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 *
 * @author pcipov
 */
public abstract class Image {	
	private static final String REPOSITORY_DELIMITER = "/";
	private static final String TAG_DELIMITER = ":";
	
	private final String tag;
	private final Registry registry;

	public Image(Registry registry, String tag) {
		this.registry = registry;
		this.tag = tag;
	}

	public Registry getRegistry() {
		return registry;
	}

	public String getTag() {
		return tag;
	}
	
	public abstract String getName();
	
	public Optional<String> getRepository() {
		return Optional.empty();
	}
	
	public Collection<String> getExposedPorts() {
		return Collections.emptyList();
	}

	@Override
	public String toString() {
		return registry.getConnectionString()
			+ getRepository().map(repository -> repository + REPOSITORY_DELIMITER).orElse("")
			+ getName()
			+ TAG_DELIMITER
			+ tag;
	}
}
