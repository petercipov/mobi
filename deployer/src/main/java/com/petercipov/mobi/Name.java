package com.petercipov.mobi;

import java.util.Optional;

/**
 *
 * @author petercipov
 */
public class Name {
	
	private final Optional<String> repository;
	private final String name;
	
	public Name(String name) {
		this(Optional.empty(), name);
	}
	
	public Name(String repository, String name) {
		this(Optional.of(repository), name);
	}

	public Name(Optional<String> repository, String name) {
		this.repository = repository;
		this.name = name;
	}

	public Optional<String> getRepository() {
		return repository;
	}

	public String getName() {
		return name;
	}
	
}
