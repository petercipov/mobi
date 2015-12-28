package com.petercipov.mobi.config;

import com.petercipov.mobi.Registry;

/**
 *
 * @author pcipov
 */
public class LocalRegistry implements Registry {
	
	@Override
	public String getConnectionString() {
		return "";
	}

	@Override
	public String toString() {
		return "LocalRegistry";
	}
}