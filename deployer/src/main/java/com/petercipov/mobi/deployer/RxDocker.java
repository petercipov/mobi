package com.petercipov.mobi.deployer;

import com.petercipov.mobi.Image;
import com.petercipov.traces.api.Trace;
import java.util.List;
import java.util.Map;
import rx.Observable;

/**
 *
 * @author Peter Cipov
 */
public interface RxDocker <R, B extends Deployment<?,R>>{
    
    Observable<Image> pull(Trace trace, Image image);
    Observable<Boolean> isPresent(Trace trace, Image image);
    
    Observable<String> createContainer(Trace trace, B builder);
    Observable<String> startContainer(Trace trace, String containerId);
    Observable<String> killContainer(Trace trace, String containerId);
    Observable<String> stopContainer(Trace trace, String containerId, int secondsBeforeFail);
    Observable<String> removeContainer(Trace trace, String containerId);
    Observable<Boolean> isContainerRunning(Trace trace, String containerId);
	Observable<Map<String, List<PortBinding>>> containerPorts(Trace trace, String containerId);
	
}
