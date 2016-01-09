package com.petercipov.mobi.deployer;

import com.petercipov.mobi.Image;
import com.petercipov.traces.api.NoopTrace;
import com.petercipov.traces.api.Trace;
import java.util.Collection;
import java.util.Optional;

/**
 *
 * @author Peter Cipov
 */
public abstract class Deployment <I extends Image, R> {
    protected Trace trace;
	protected final I image;
	protected Optional<String> name;

    public Deployment(I image) {
        this.image = image;
        this.trace = NoopTrace.INSTANCE;
		this.name = Optional.empty();
    }
        
    public Deployment<I, R> trace(Trace trace) {
		this.trace = trace;
		return this;
	}
		
    public Deployment<I, R> name(String name) {
        this.name = Optional.of(name);
        return this;
    }
    
    public Trace trace() {
		return trace;
	}

	public I image() {
		return image;
	}

	public Optional<String> name() {
		return name;
	}
    
	public abstract Deployment<I, R> volume(String ... volumeBindings);
	public abstract Deployment<I, R> volume(Collection<String> volumeBindings);
    public abstract Deployment<I, R> volume(String hostPath, String guestPath);
    public abstract Deployment<I, R> env(String variable);
    public abstract Deployment<I, R> port(String port, int customPort);
	public abstract boolean portsSpecified();
	
	public abstract Deployment<I, R> allPortsPublished(boolean enabled);
    
    public abstract R build();
}
