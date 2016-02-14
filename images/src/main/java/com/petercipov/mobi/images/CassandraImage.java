package com.petercipov.mobi.images;

import com.petercipov.mobi.ImageDefinition;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class CassandraImage implements ImageDefinition {
	
	public static final String CLUSTER_COMM_PORT = "7000/tcp";
	public static final String SSL_CLUSTER_COMM_PORT =  "7001/tcp";
	public static final String JMX_PORT = "7199/tcp";
	public static final String NATIVE_COMM_PORT = "9042/tcp";
	public static final String THRIFT_PORT = "9160/tcp";
	
	private static final List<String> EXPOSED_PORTS = Collections.unmodifiableList(Arrays.asList(
		CLUSTER_COMM_PORT,
		SSL_CLUSTER_COMM_PORT, 
		JMX_PORT, 
		NATIVE_COMM_PORT, 
		THRIFT_PORT
	));

	@Override
	public String getName() {
		return "cassandra";
	}

	@Override
	public Iterable<String> getExposedPorts() {
		return EXPOSED_PORTS;
	}

	@Override
	public Optional<String> getRepository() {
		return Optional.empty();
	}
}
