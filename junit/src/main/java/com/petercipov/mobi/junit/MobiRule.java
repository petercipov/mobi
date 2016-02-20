package com.petercipov.mobi.junit;

import com.petercipov.mobi.Builder;
import com.petercipov.mobi.config.ApiHost;
import com.petercipov.mobi.Instance;
import com.petercipov.mobi.Name;
import com.petercipov.mobi.config.MobiConfig;
import com.petercipov.mobi.deployer.Container;
import com.petercipov.mobi.deployer.Deployer;
import com.petercipov.mobi.deployer.RxConnector;
import com.petercipov.mobi.deployer.RxDeployment;
import com.petercipov.mobi.deployer.RxDocker;
import com.petercipov.traces.api.Level;
import com.petercipov.traces.api.NoopTrace;
import com.petercipov.traces.api.Trace;
import com.petercipov.traces.api.Trace.Event;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.junit.rules.ExternalResource;
import rx.Observable;
import rx.Scheduler;
import rx.schedulers.Schedulers;

public class MobiRule extends ExternalResource {

	private static final String CONNECTOR_NAME = "com.petercipov.mobi.deployer.RxConnectorImpl";

	private final MobiConfig mobiConfig;
	private final Scheduler scheduler;
	private final Builder imageBuilder;

	private Deployer deployer;
	private RxDocker rxDocker;
	private final RxConnectorLoader connectorLoader;
	private final Supplier<Trace> traceSupplier;

	public MobiRule() {
		this(() -> NoopTrace.INSTANCE);
	}
	
	public MobiRule(Supplier<Trace> traceSupplier) {
		this(Schedulers.computation(), new ReflexRxConnectorLoader(CONNECTOR_NAME), traceSupplier);
	}
	
	public MobiRule(Scheduler scheduler, RxConnectorLoader connectorLoader, Supplier<Trace> traceSupplier) {
		this.scheduler = scheduler;
		this.connectorLoader = connectorLoader;
		this.traceSupplier = traceSupplier;
		
		MobiConfigReader reader =  new MobiConfigReader();
		try {
			this.mobiConfig = reader.readFromDefaultLocations();
		} catch(Exception ex ) {
			throw new IllegalStateException("Configuration was not loaded", ex);
		}

		this.imageBuilder = new Builder(this.mobiConfig.getRegistry(), this.mobiConfig.getTags());
	}

	@Override
	protected void before() throws Throwable {
		final Trace trace = traceSupplier.get();
		final ApiHost api = this.mobiConfig.getRandomApiHost();
		final RxConnector connector = this.connectorLoader.load(trace);

		this.rxDocker = connector.createRxDocker(api, scheduler);
		this.deployer = new Deployer(api, this.rxDocker);
	}

	@Override
	protected void after() {
		Trace trace = traceSupplier.get();
		Event closeEvent = trace.start("Closing mobi");
		
		try {
			this.deployer.close(trace);
		} catch(Exception ex) {
			trace.event(Level.ERROR, "Closing of deployer has failed", ex);
		}

		try {
			this.rxDocker.close(trace);
		} catch(Exception ex) {
			trace.event(Level.ERROR, "Closing of rxDocker has Failed", ex);
		}
		
		closeEvent.end();
	}
	
	public Deployer deployer() {
		return this.deployer;
	}
	
	public MobiWork image(Name name) {
		return image(name, "latest");
	}
	
	public MobiWork image(String name, String tag) {
		return image(new Name(name), tag);
	}
	
	public MobiWork image(String repository, String name, String tag) {
		return image(new Name(repository, name), tag);
	}
	
	public MobiWork image(Name name, String tag) {
		Objects.requireNonNull(name);
		Objects.requireNonNull(tag);
		
		Instance instance = imageBuilder.build(name, tag);
		return new MobiWork(deployer, rxDocker, instance);
	}
	
	public RxDocker docker() {
		return rxDocker;
	}
	
	public static class MobiWork {

		private final Deployer deployer;
		private final RxDeployment options;
		private final Instance image;

		public MobiWork(Deployer deployer, RxDocker docker, Instance image) {
			this.deployer = deployer;
			this.options = docker.deployment();
			this.image = image;
		}
		
		public MobiWork with(Consumer<RxDeployment> b) {
			b.accept(options);
			return this;
		}
		
		public Observable<Container> deploy(Trace trace) {
			return this.deployer.deploy(trace, image, options);
		}
	}
}
