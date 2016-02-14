package com.petercipov.mobi.junit;

import com.petercipov.mobi.deployer.RxConnector;
import com.petercipov.traces.api.Level;
import com.petercipov.traces.api.Trace;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 * @author petercipov
 */
public class ReflexRxConnectorLoader implements RxConnectorLoader{

	private final String loaderPath;
	private final String className;

	public ReflexRxConnectorLoader(String className) {
		this.loaderPath = className.replace('.', '/')+".class";
		this.className = className;
	}

	@Override
	public RxConnector load(Trace trace) {
		Set<URL> loaders = findPossibleLoaders(trace);
		
		assertNotEmpty(loaders, trace);
		assertSingle(loaders, trace);
		
		try {
			return (RxConnector)this.getClass().getClassLoader().loadClass(className).newInstance();
		} catch(Exception ex) {
			throw new IllegalStateException("Error while initializing connector", ex);
		}
	}
	
	private Set<URL> findPossibleLoaders(Trace trace) {
		
        Set<URL> loadersPaths = new LinkedHashSet<>();
        try {
            ClassLoader loader = this.getClass().getClassLoader();
            Enumeration<URL> paths;
            if (loader == null) {
                paths = ClassLoader.getSystemResources(loaderPath);
            } else {
                paths = loader.getResources(loaderPath);
            }
			
			if (paths != null) {
				while (paths.hasMoreElements()) {
					URL path = paths.nextElement();
					trace.event("ReflexRxDockerLoader: found connector class (path)", path);
					loadersPaths.add(path);
				}
			}
        } catch (Exception ex) {
			throw new IllegalStateException("Exception while loading class", ex);
        }
        return loadersPaths;
    }
	
	private void assertSingle(Set<URL> loaders, Trace trace) {
		int size = loaders.size();
		if (size != 1) {
			trace.event(Level.ERROR, "Multiple connector classes were found on the classpath. It is ambiguous !(class name)", className);
			throw new IllegalArgumentException("Multiple connector classes were found on the classpath. It is ambiguous !");
		}
	}

	private void assertNotEmpty(Set<URL> loaders, Trace trace) {
		if (loaders.isEmpty()) {
			trace.event(Level.ERROR, "Connector class was not found on the classpath (class name)", className);
			throw new IllegalArgumentException("Connector class was not found on the classpath");
		}
	}
}
