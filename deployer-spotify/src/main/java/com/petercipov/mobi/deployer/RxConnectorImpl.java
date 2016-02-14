package com.petercipov.mobi.deployer;

import com.petercipov.mobi.config.ApiHost;
import com.petercipov.mobi.deployer.spotify.SpotifyClientBuilder;
import com.petercipov.mobi.deployer.spotify.SpotifyRxDocker;
import rx.Scheduler;

/**
 *
 * @author petercipov
 */
public class RxConnectorImpl implements RxConnector {
	
	private final SpotifyClientBuilder apiBuilder = new SpotifyClientBuilder();

	@Override
	public RxDocker createRxDocker(ApiHost api, Scheduler scheduler) {
		return new SpotifyRxDocker(api.setupBuilder(apiBuilder), scheduler);
	}
}
