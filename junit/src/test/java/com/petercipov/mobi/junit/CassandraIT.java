package com.petercipov.mobi.junit;

import com.petercipov.mobi.deployer.Container;
import com.petercipov.traces.api.Trace;
import com.petercipov.traces.junit.TraceRule;
import java.util.concurrent.TimeUnit;
import org.junit.Rule;
import org.junit.Test;

public class CassandraIT {
	
	public static final String CLUSTER_COMM_PORT = "7000/tcp";
	public static final String SSL_CLUSTER_COMM_PORT =  "7001/tcp";
	public static final String JMX_PORT = "7199/tcp";
	public static final String NATIVE_COMM_PORT = "9042/tcp";
	public static final String THRIFT_PORT = "9160/tcp";
	
	@Rule
	public TraceRule traces = new TraceRule(TraceRule.Type.VIZU);
	
	@Rule
	public MobiRule mobi = new MobiRule(traces::trace);
	
	@Test
	public void cassandraClusterDeployment() throws Exception {
		final Trace trace = traces.trace();
		
		Container first = mobi
			.image("cassandra", "3.0.2")
			.with(setup -> setup
				.publishAllPorts()
				//.addPortMapping(CassandraImage.CLUSTER_COMM_PORT, 12888)
				//.addEnv("CASSANDRA_BROADCAST_ADDRESS", "192.168.56.101:12888")
				.setName("first-node")
			)
			.deploy(trace)
			.toBlocking().toFuture().get(1, TimeUnit.MINUTES);
		
		String ipAddress = mobi.docker().inspectContainer(trace, first.getContainerId())
			.toBlocking().toFuture().get(1, TimeUnit.MINUTES)
			.networkSettings().ipAddress();
				
		Container second = mobi
			.image("cassandra", "3.0.2")
			.with(setup -> setup
				.publishAllPorts()
				.setName("second-node")
				.addVolume("/var/log", "/var/log")
				.addEnv("CASSANDRA_SEEDS="+ipAddress)
			)
			.deploy(trace)
			.toBlocking().toFuture().get(1, TimeUnit.MINUTES);

		int port = first.getPort(NATIVE_COMM_PORT);

	}
}
