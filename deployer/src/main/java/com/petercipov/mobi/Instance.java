package com.petercipov.mobi;

/**
 *
 * @author pcipov
 */
public class Instance {	
	private static final String REPOSITORY_DELIMITER = "/";
	private static final String TAG_DELIMITER = ":";
	
	private final String tag;
	private final Registry registry;
	private final Name image;

	public Instance(Registry registry, Name definition, String tag) {
		this.registry = registry;
		this.image = definition;
		this.tag = tag;
	}

	public Registry getRegistry() {
		return registry;
	}

	public String getTag() {
		return tag;
	}

	public Name getImage() {
		return image;
	}
	
	@Override
	public String toString() {
		return registry.getConnectionString()
			+ getImage().getRepository().map(repository -> repository + REPOSITORY_DELIMITER).orElse("")
			+ getImage().getName()
			+ TAG_DELIMITER
			+ tag;
	}
}
