package com.petercipov.mobi.junit;

import com.petercipov.mobi.ApiHost;
import com.petercipov.mobi.ExplicitTag;
import com.petercipov.mobi.Images;
import com.petercipov.mobi.Registry;
import com.petercipov.mobi.config.DockerConfig;
import com.petercipov.mobi.deployer.Deployer;
import com.petercipov.mobi.deployer.RxDocker;
import com.petercipov.traces.api.Level;
import com.petercipov.traces.api.NoopTrace;
import com.petercipov.traces.api.Trace;
import com.petercipov.traces.api.Trace.Event;
import com.spotify.docker.client.DefaultDockerClient;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import org.junit.rules.ExternalResource;
import rx.Scheduler;
import rx.schedulers.Schedulers;

public class MobiRule <T extends Images> extends ExternalResource {
	private final DefaultDockerClient.Builder clientBuilder;
	private final DockerConfig dockerConfig;
	private final Scheduler scheduler;
	private final ConfigReader reader;
	private final T images;

	private Deployer deployer;
	private RxDocker rxDocker;
	private final Supplier<Trace> traceSupplier;
	
	public MobiRule(BiFunction<? super Registry, List<ExplicitTag>, T> imageFactory) {
		this(imageFactory, Schedulers.computation(), DefaultDockerClient.builder(), () -> NoopTrace.INSTANCE);
	}
	
	public MobiRule(BiFunction<? super Registry, List<ExplicitTag>, T> imageFactory, Scheduler scheduler, DefaultDockerClient.Builder clientBuilder, Supplier<Trace> traceSupplier) {
		this.reader =  new ConfigReader();
		this.scheduler = scheduler;
		this.clientBuilder = clientBuilder;
		this.traceSupplier = traceSupplier;
		
		try {
			this.dockerConfig = this.reader.readFromDefaultLocations();
		} catch(Exception ex ) {
			throw new IllegalStateException("Configuration was not loaded", ex);
		}
		
		this.images = imageFactory.apply(
			this.dockerConfig.getRegistry(), 
			this.dockerConfig.getExplicitTags()
		);
	}

	@Override
	protected void before() throws Throwable {
		ApiHost api = this.dockerConfig.getRandomApiHost();
		this.rxDocker = new RxDocker(api.setupBuilder(this.clientBuilder), this.scheduler);
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
	
	public RxDocker docker() {
		return rxDocker;
	}
}
