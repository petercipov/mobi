package com.petercipov.mobi.images;

import com.petercipov.mobi.deployer.Container;
import com.petercipov.mobi.junit.MobiRule;
import com.petercipov.traces.api.Trace;
import com.petercipov.traces.junit.TraceRule;
import java.util.concurrent.TimeUnit;
import org.junit.Rule;
import org.junit.Test;

public class CassandraIT {
	
	@Rule
	public TraceRule traces = new TraceRule();
	
	@Rule
	public MobiRule<DefaultImages> mobi = new MobiRule<>(DefaultImages::new, traces::trace);
	
	@Test
	public void cassandraClusterDeployment() throws Exception {
		final Trace trace = traces.trace();
		
		Container<CassandraImage> first = mobi
				.image(images -> images.cassandra().forTag("3.0.2"))
				.with(setup -> setup
					.publishAllPorts()
					.setName("firstNode-deploy")
				)
				.deploy(trace)
				.toBlocking().toFuture().get(1, TimeUnit.MINUTES);
		
		String ipAddress = mobi.docker().inspectContainer(trace, first.getContainerId())
			.toBlocking().toFuture().get(1, TimeUnit.MINUTES)
			.networkSettings().ipAddress();
		
		Container<CassandraImage> second = mobi
			.image(images -> images.cassandra().forTag("3.0.2"))
			.with(setup -> setup
				.publishAllPorts()
				.setName("second-node")
				.addVolume("/var/log", "/var/log")
				.addEnv("CASSANDRA_SEEDS="+ipAddress)
			)
			.deploy(trace)
			.toBlocking().toFuture().get(1, TimeUnit.MINUTES);
	}
}
