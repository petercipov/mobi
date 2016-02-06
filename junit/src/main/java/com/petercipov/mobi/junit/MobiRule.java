package com.petercipov.mobi.junit;

import com.petercipov.mobi.config.ApiHost;
import com.petercipov.mobi.Image;
import com.petercipov.mobi.Images;
import com.petercipov.mobi.Registry;
import com.petercipov.mobi.TagOverride;
import com.petercipov.mobi.config.MobiConfig;
import com.petercipov.mobi.deployer.Container;
import com.petercipov.mobi.deployer.Deployer;
import com.petercipov.mobi.deployer.Options;
import com.petercipov.mobi.deployer.spotify.SpotifyClientBuilder;
import com.petercipov.mobi.deployer.spotify.SpotifyOptions;
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
	private final MobiConfig dockerConfig;
	private final Scheduler scheduler;
	private final ConfigReader reader;
	private final T images;

	private Deployer deployer;
	private SpotifyRxDocker rxDocker;
	private final Supplier<Trace> traceSupplier;
	
	public MobiRule(BiFunction<? super Registry, List<TagOverride>, T> imageFactory) {
		this(imageFactory, Schedulers.computation(), new SpotifyClientBuilder(), () -> NoopTrace.INSTANCE);
	}
	
	public MobiRule(BiFunction<? super Registry, List<TagOverride>, T> imageFactory, Scheduler scheduler, SpotifyClientBuilder clientBuilder, Supplier<Trace> traceSupplier) {
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
			this.dockerConfig.getTags()
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
		return new MobiWork<>(deployer, image, new SpotifyOptions());
	}
	
	public SpotifyRxDocker docker() {
		return rxDocker;
	}
	
	public static class MobiWork<I extends Image> {

		private final Deployer deployer;
		private final Options options;
		private final I image;

		public MobiWork(Deployer deployer, I image, Options deployment) {
			this.deployer = deployer;
			this.options = deployment;
			this.image = image;
		}
		
		public MobiWork<I> with(Consumer<Options> b) {
			b.accept(options);
			return this;
		}
		
		public Observable<Container<I>> deploy(Trace trace) {
			return this.deployer.deploy(trace, image, options);
		}
	}
}
