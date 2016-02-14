package com.petercipov.mobi.junit;

import com.petercipov.mobi.ImageDefinition;
import com.petercipov.mobi.config.ApiHost;
import com.petercipov.mobi.ImageInstance;
import com.petercipov.mobi.Images;
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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.junit.rules.ExternalResource;
import rx.Observable;
import rx.Scheduler;
import rx.schedulers.Schedulers;

public class MobiRule <T extends Images> extends ExternalResource {

	private static final String CONNECTOR_NAME = "com.petercipov.mobi.deployer.RxConnectorImpl";

	private final MobiConfig mobiConfig;
	private final Scheduler scheduler;
	private final T images;

	private Deployer deployer;
	private RxDocker rxDocker;
	private final RxConnectorLoader connectorLoader;
	private final Supplier<Trace> traceSupplier;

	public MobiRule(Function<MobiConfig, T> imagesBuilder) {
		this(imagesBuilder, () -> NoopTrace.INSTANCE);
	}
	
	public MobiRule(Function<MobiConfig, T> imagesBuilder, Supplier<Trace> traceSupplier) {
		this(imagesBuilder, Schedulers.computation(), new ReflexRxConnectorLoader(CONNECTOR_NAME), traceSupplier);
	}
	
	public MobiRule(Function<MobiConfig, T> imagesBuilder, Scheduler scheduler, RxConnectorLoader connectorLoader, Supplier<Trace> traceSupplier) {
		this.scheduler = scheduler;
		this.connectorLoader = connectorLoader;
		this.traceSupplier = traceSupplier;
		
		MobiConfigReader reader =  new MobiConfigReader();
		try {
			this.mobiConfig = reader.readFromDefaultLocations();
		} catch(Exception ex ) {
			throw new IllegalStateException("Configuration was not loaded", ex);
		}

		this.images = imagesBuilder.apply(this.mobiConfig);
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
	
	public T images() {
		return images;
	}
	
	public <I extends ImageDefinition> MobiWork<I> image(Function<T, ImageInstance<I>> imageChooser) {
		ImageInstance<I> image = imageChooser.apply(images);
		return new MobiWork<>(deployer, rxDocker, image);
	}
	
	public RxDocker docker() {
		return rxDocker;
	}
	
	public static class MobiWork<I extends ImageDefinition> {

		private final Deployer deployer;
		private final RxDeployment options;
		private final ImageInstance<I> image;

		public MobiWork(Deployer deployer, RxDocker docker, ImageInstance<I> image) {
			this.deployer = deployer;
			this.options = docker.deployment();
			this.image = image;
		}
		
		public MobiWork<I> with(Consumer<RxDeployment> b) {
			b.accept(options);
			return this;
		}
		
		public Observable<Container<I>> deploy(Trace trace) {
			return this.deployer.deploy(trace, image, options);
		}
	}
}
