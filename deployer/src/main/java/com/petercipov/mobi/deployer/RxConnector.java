package com.petercipov.mobi.deployer;

import com.petercipov.mobi.config.ApiHost;
import rx.Scheduler;

/**
 *
 * @author petercipov
 */
public interface RxConnector {

	RxDocker createRxDocker(ApiHost api, Scheduler scheduler);
	
}
