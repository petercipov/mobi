package com.petercipov.mobi.junit;

import com.petercipov.mobi.deployer.RxConnector;
import com.petercipov.traces.api.Trace;

/**
 *
 * @author petercipov
 */
public interface RxConnectorLoader {
	RxConnector load(Trace trace);
}
