package com.petercipov.mobi.junit;

import com.petercipov.mobi.config.ApiHost;
import com.petercipov.mobi.deployer.RxConnector;
import com.petercipov.mobi.deployer.RxDocker;
import com.petercipov.traces.api.Trace;
import com.petercipov.traces.junit.TraceRule;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import rx.Scheduler;

/**
 *
 * @author petercipov
 */
public class ReflexRxConnectorLoaderTest {
	
	@Rule
	public TraceRule traces = new TraceRule();
	
	@Test
	public void loaderLooksForSpeciedClassName() {
		final Trace trace = traces.trace();
		final String className = NoopLoader.class.getName();
		final ReflexRxConnectorLoader loader = new ReflexRxConnectorLoader(className);
		
		RxConnector connector = loader.load(trace);
		
		assertNotNull(connector);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void ifClassNotFoundIllegalArgumentExceptionIsThrown() {
		final Trace trace = traces.trace();
		final ReflexRxConnectorLoader loader = new ReflexRxConnectorLoader("unknown_class_name");
		
		loader.load(trace);
	}
	
	public static class NoopLoader implements RxConnector {

		@Override
		public RxDocker createRxDocker(ApiHost api, Scheduler scheduler) {
			return null;
		}
	
	}
	
}
