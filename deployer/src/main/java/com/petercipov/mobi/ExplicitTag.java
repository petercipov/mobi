package com.petercipov.mobi;

import java.util.Optional;

/**
 *
 * @author pcipov
 */
public class ExplicitTag {

	private final Optional<String> repository;
	private final String name;
	private final String tag;
	private final String explicitTag;
	
	public ExplicitTag(Optional<String> repository, String name, String tag, String explicitTag) {
		this.repository = repository;
		this.name = name;
		this.tag = tag;
		this.explicitTag = explicitTag;
	}

	public String getName() {
		return name;
	}

	public Optional<String> getRepository() {
		return repository;
	}

	public String getTag() {
		return tag;
	}

	public String getExplicitTag() {
		return explicitTag;
	}

	@Override
	public String toString() {
		return "ExplicitTag(" + ", repository=" + repository+"name=" + name + ", tag=" + tag + ", explicitTag=" + explicitTag + ')';
	}
}