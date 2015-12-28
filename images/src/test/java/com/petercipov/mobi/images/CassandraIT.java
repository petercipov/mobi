package com.petercipov.mobi.images;

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
	public MobiRule<STDImages> mobi = new MobiRule<>(STDImages::new);
	
	@Test
	public void empty() throws Exception {
		final Trace trace = traces.trace();
		
		mobi.deployer().deploy(trace, mobi.images().cassandra().forTag("3.0.2"))
			.toBlocking().toFuture().get(1, TimeUnit.MINUTES);
		
	
	}
}
