package com.petercipov.mobi.junit;

import com.petercipov.mobi.config.ApiHost;
import com.petercipov.mobi.ExplicitTag;
import com.petercipov.mobi.Image;
import com.petercipov.mobi.Images;
import com.petercipov.mobi.Registry;
import com.petercipov.mobi.config.DockerConfig;
import com.petercipov.mobi.deployer.Container;
import com.petercipov.mobi.deployer.Deployer;
import com.petercipov.mobi.deployer.Deployment;
import com.petercipov.mobi.deployer.spotify.SpotifyClientBuilder;
import com.petercipov.mobi.deployer.spotify.SpotifyDeployment;
import com.petercipov.mobi.deployer.spotify.SpotifyRxDocker;
import com.petercipov.traces.api.Level;
import com.petercipov.traces.api.NoopTrace;
import com.petercipov.traces.api.Trace;
import com.petercipov.traces.api.Trace.Event;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.junit.rules.ExternalResource;
import rx.Observable;
import rx.Scheduler;
import rx.schedulers.Schedulers;

public class MobiRule <T extends Images> extends ExternalResource {
	private final SpotifyClientBuilder clientBuilder;
	private final DockerConfig dockerConfig;
	private final Scheduler scheduler;
	private final ConfigReader reader;
	private final T images;

	private Deployer deployer;
	private SpotifyRxDocker rxDocker;
	private final Supplier<Trace> traceSupplier;
	
	public MobiRule(BiFunction<? super Registry, List<ExplicitTag>, T> imageFactory) {
		this(imageFactory, Schedulers.computation(), new SpotifyClientBuilder(), () -> NoopTrace.INSTANCE);
	}
	
	public MobiRule(BiFunction<? super Registry, List<ExplicitTag>, T> imageFactory, Scheduler scheduler, SpotifyClientBuilder clientBuilder, Supplier<Trace> traceSupplier) {
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
		this.rxDocker = new SpotifyRxDocker(api.setupBuilder(clientBuilder), this.scheduler);
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
	
	public <I extends Image> MobiWork<I> image(Function<T, I> imageChooser) {
		I image = imageChooser.apply(images);
		return new MobiWork<>(deployer, new SpotifyDeployment<I>(image));
	}
	
	public SpotifyRxDocker docker() {
		return rxDocker;
	}
	
	public static class MobiWork<I extends Image> {

		private final Deployer deployer;
		private final Deployment<I, ?> builder;

		public MobiWork(Deployer deployer, Deployment<I, ?> deployment) {
			this.deployer = deployer;
			this.builder = deployment;
		}
		
		public MobiWork<I> with(Consumer<Deployment<I, ?>> b) {
			b.accept(builder);
			return this;
		}
		
		public Observable<Container<I>> deploy() {
			return this.deployer.deploy(builder);
		}
	}
}
