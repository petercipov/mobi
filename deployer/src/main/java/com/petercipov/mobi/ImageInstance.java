package com.petercipov.mobi;

/**
 *
 * @author pcipov
 */
public class ImageInstance<I extends ImageDefinition> {	
	private static final String REPOSITORY_DELIMITER = "/";
	private static final String TAG_DELIMITER = ":";
	
	private final String tag;
	private final Registry registry;
	private final I definition;

	public ImageInstance(Registry registry, String tag, I definition) {
		this.registry = registry;
		this.tag = tag;
		this.definition = definition;
	}

	public Registry getRegistry() {
		return registry;
	}

	public String getTag() {
		return tag;
	}

	public I getDefinition() {
		return definition;
	}
	
	@Override
	public String toString() {
		return registry.getConnectionString()
			+ getDefinition().getRepository().map(repository -> repository + REPOSITORY_DELIMITER).orElse("")
			+ getDefinition().getName()
			+ TAG_DELIMITER
			+ tag;
	}
}
