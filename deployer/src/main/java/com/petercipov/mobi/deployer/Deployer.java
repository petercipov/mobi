package com.petercipov.mobi.deployer;

import com.petercipov.mobi.ImageDefinition;
import com.petercipov.mobi.ImageInstance;
import com.petercipov.mobi.config.ApiHost;
import com.petercipov.traces.api.Level;
import com.petercipov.traces.api.Trace;
import com.petercipov.traces.api.Trace.Event;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import rx.Observable;

/**
 *
 * @author pcipov
 */
public class Deployer{
	
	private final ApiHost api;
	private final List<Container<? extends ImageDefinition>> deployedContainers;
	private final RxDocker rxdocker;
	
	public Deployer(ApiHost api, RxDocker rxdocker) {
		this.api = api;
		this.rxdocker = rxdocker;
		this.deployedContainers = Collections.synchronizedList(new LinkedList<>());
	}
	
	public <I extends ImageDefinition> Observable<Container<I>> deploy(Trace trace, ImageInstance<I> image, RxDeployment deployment) {
		return Observable.defer(() -> {
			setDefaults(deployment);
			Event deployEvent = trace.start("Deployer: deploying image", image);
			return rxdocker.isPresent(trace, image)
				.flatMap(present -> {
					if (present) {
						trace.event("Deployer: Image already present in the docker", image);
						return  Observable.just(image);
					} else {
						trace.event("Deployer: Image not present in the docker, pulling image from registry", image);
						return rxdocker.pull(trace, image); 
					}
				})
				.flatMap((xxx) -> deployment.createContainer(trace, image))
				.flatMap(containerId -> rxdocker.startContainer(trace, containerId))
                .flatMap(containerId -> 
                    rxdocker.inspectContainer(trace, containerId)
                    .flatMap(inspection -> {
                        if (inspection.state().running()) {
                            return Observable.just(inspection);
                        } else {
                            return Observable.error(new IllegalStateException("Expecting container to run. containerId="+containerId));
                        }
                    })
					.map(inspection -> new Container<>(containerId, image, inspection.networkSettings().ports()))
					.onErrorResumeNext(ex ->  rxdocker.killContainer(trace, containerId)
						.flatMap(id -> rxdocker.removeContainer(trace, id))
						.flatMap(id -> Observable.error(ex))
					)
                )
				.doOnNext(container -> {
					trace.event("Deployer: image was deployed, (container)", container);
                    deployedContainers.add(container);
				})
				.doOnError((th) -> trace.event(Level.ERROR, "Deployer: could not deploy", th))
				.doOnTerminate(() -> deployEvent.end());
		});
    }
	
	
	protected void setDefaults(RxDeployment options) {
		api
			.getVolumeBindings()
			.ifPresent(options::addVolumes);
	}
	
	public <I extends ImageDefinition> Observable<Container<I>> killContainer(Trace trace, Container<I> container) {
		trace.event("Deployer: killing container, (container id, image)", container.getContainerId(), container.getImage());
		return rxdocker.killContainer(trace, container.getContainerId())
			.flatMap(id -> rxdocker.removeContainer(trace, id))
			.map(id -> {
				trace.event("Deployer: container was killed, (container id)", id);
				deployedContainers.remove(container);
				return container;
			});
    }
	
	public <I extends ImageDefinition> Observable<Container<I>> stopContainer(Trace trace, Container<I> container, int secondsBefaoreKill) {
		trace.event("Deployer: stopping container (container id, image)", container.getContainerId(), container.getImage());
		return rxdocker.stopContainer(trace, container.getContainerId(), secondsBefaoreKill)
			.flatMap(id -> rxdocker.removeContainer(trace, id))
			.map(id -> {
				trace.event("Deployer: container was stopped, (container id)", id);
				deployedContainers.remove(container);
				return container;
			});		
    }
	
	public void close(Trace trace) throws IOException {
		killAllPendingContainers(trace);
	}

	private void killAllPendingContainers(Trace trace) {
		trace.event("Deployer: killing all pending containers");
		
		List<Container<? extends ImageDefinition>> list;
		synchronized(deployedContainers) {
			list = new ArrayList<>(deployedContainers);
		}
		
		Observable.from(list)
			.flatMap((c) -> this.killContainer(trace, c))
			.toList().toBlocking().single()
		;
	}
}