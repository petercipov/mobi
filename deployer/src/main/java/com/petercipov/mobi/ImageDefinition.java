package com.petercipov.mobi;

import java.util.Optional;

/**
 *
 * @author petercipov
 */
public interface ImageDefinition {

	String getName();
	Optional<String> getRepository();
	Iterable<String> getExposedPorts();
	
}
