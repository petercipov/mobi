package com.petercipov.mobi.config;

import com.google.common.base.Charsets;
import com.petercipov.mobi.Registry;
import com.petercipov.mobi.TagOverride;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.events.DocumentEndEvent;
import org.yaml.snakeyaml.events.DocumentStartEvent;
import org.yaml.snakeyaml.events.Event;
import org.yaml.snakeyaml.events.MappingEndEvent;
import org.yaml.snakeyaml.events.MappingStartEvent;
import org.yaml.snakeyaml.events.ScalarEvent;
import org.yaml.snakeyaml.events.SequenceEndEvent;
import org.yaml.snakeyaml.events.SequenceStartEvent;
import org.yaml.snakeyaml.events.StreamEndEvent;
import org.yaml.snakeyaml.events.StreamStartEvent;

/**
 *
 * @author petercipov
 */
public class YamlConfigParser {

	public MobiConfig parse(InputStream in) {
		Yaml parser = new Yaml();
		Events events = new Events(parser.parse(new InputStreamReader(in, Charsets.UTF_8)).iterator());		
		return toConfig(events);
	}

	private MobiConfig toConfig(Events events) {
		List<ApiHost> apis = new LinkedList<>();
		List<TagOverride> overrides = new LinkedList<>();
		Registry registry = new DefaultRegistry();
		
		events.expectStreamStart();
		if (events.isDocumentStarting()) {
			events.expectDocumentStart();
			events.expectMappingStart();
			
			while(events.isScalar()) {
				String name = events.expectScalar().getValue();
				switch(name) {
					case "api":
						if (events.isSequenceStarting()) {
							events.expectSequenceStart();
							while(events.isMappingStarting()) {
								apis.add(toApiHost(events));
							}
							events.expectSequenceEnd();
						}

						break;
					case "registry":
						registry = toApiRegistry(events);
						break;
					case "override":
						if (events.isSequenceStarting()) {
							events.expectSequenceStart();
							while(events.isMappingStarting()) {
								overrides.add(toOverrides(events));
							}
							events.expectSequenceEnd();
						}
						break;
					default:
						throw new IllegalStateException("unknown name " + name);
				}
			}

			events.expectMappingEnd();
			events.expectDocumentEnd();
		}
		events.expectStreamEnd();
		
		return new MobiConfig(apis, registry, overrides);
	}
	
	private Registry toApiRegistry(Events events) {
		String host = null;
		Integer port = null;
		
		events.expectMappingStart();

		while (true) {
			Event e = events.peek();

			if (e == null) { break; }
			if (e.is(Event.ID.MappingEnd)) { break; }

			String name = events.expectScalar().getValue();
			
			switch(name) {
				case "host":
					host = events.expectScalar().getValue();
					break;
				case "port":
					port = Integer.parseInt(events.expectScalar().getValue());
					break;
				default:
					throw new IllegalStateException("unknown name "+name);
			}
		}
		
		events.expectMappingEnd();

		Objects.requireNonNull(host, "host may be not be null for registry spec");
		Objects.requireNonNull(port, "port may be not be null for registry spec");
		return new RemoteRegistry(host, port);
	}

	private ApiHost toApiHost(Events events) {
		String type = null;
		String host = null;
		String path = null;
		String cert = null;
		Integer port = null;
		Optional<List<String>> volumes = Optional.empty();
		
		events.expectMappingStart();
		
		while (true) {
			Event e = events.peek();

			if (e == null) { break; }
			if (e.is(Event.ID.MappingEnd)) { break; }

			String name = events.expectScalar().getValue();
			switch(name) {
				case "type":
					type = events.expectScalar().getValue();
					break;
				case "cert":
					cert = events.expectScalar().getValue();
					break;
				case "host":
					host = events.expectScalar().getValue();
					break;
				case "path":
					path = events.expectScalar().getValue();
					break;
				case "port":
					port = Integer.parseInt(events.expectScalar().getValue());
					break;
				case "volumes":
					volumes = Optional.of(events.expectList());
					break;
				default:
					throw new IllegalStateException("unknown name "+name);
			}
		}
		
		events.expectMappingEnd();
		
		if (type == null) {
			throw new IllegalStateException("Type of API is not specified");
		} 
		
		switch(type) {
			case "http":
				Objects.requireNonNull(host, "host may be not be null for http api");
				Objects.requireNonNull(port, "port may be not be null for http api");
				return new HttpRestApiHost(host, port, volumes);
			case "https":
				Objects.requireNonNull(host, "host may be not be null for https api");
				Objects.requireNonNull(port, "port may be not be null for https api");
				Objects.requireNonNull(cert, "cert may be not be null for https api");
				return new HttpsRestApiHost(host, port, cert, volumes);
			case "unix":
				Objects.requireNonNull(cert, "path may be not be null for unix api");
				return new UnixRestApiHost(path, volumes);
			default:
				throw new IllegalStateException("Unknown type of api " + type);
		}
		
	}

	private TagOverride toOverrides(Events events) {
		events.expectMappingStart();
		
		String key = events.expectScalar().getValue();
		String explicitTag = events.expectScalar().getValue();
		Optional<String> repository = Optional.empty();
		String name;
		String tag;
		
		String[] split = key.split("\\.");
		
		if (split.length >= 3) {
			repository = Optional.of(split[0]);
			name = split[1];
			tag = split[2];
		} else {
			name = split[0];
			tag = split[1];
		}
		
		events.expectMappingEnd();
		
		return new TagOverride(repository, name, tag, explicitTag);
	}
	
	private static class Events {
		
		private final Iterator<Event> iterator;
		private Event current;

		public Events(Iterator<Event> iterator) {
			this.iterator = iterator;
			this.current = null;
		}
		
		public boolean hasNext() {
			if (current == null) {
				return iterator.hasNext();
			} else {
				return true;
			}
		}
		
		public Event next() {
			if (current == null) {
				return iterator.next();
			} else {
				Event e = current;
				current = null;
				return e;
			}
		}
		
		public Event peek() {
			if (hasNext()) {
				if (current == null) {
					current = iterator.next();
				} 
				return current;
			} else {
				return null;
			}
		}
		
		public MappingEndEvent expectMappingEnd() {
			return (MappingEndEvent) expect(Event.ID.MappingEnd);
		}
		
		public MappingStartEvent expectMappingStart() {
			return (MappingStartEvent) expect(Event.ID.MappingStart);
		}
		
		public DocumentEndEvent expectDocumentEnd() {
			return (DocumentEndEvent) expect(Event.ID.DocumentEnd);
		}
		
		public DocumentStartEvent expectDocumentStart() {
			return (DocumentStartEvent) expect(Event.ID.DocumentStart);
		}
		
		public StreamStartEvent expectStreamStart() {
			return (StreamStartEvent) expect(Event.ID.StreamStart);
		}
		
		public StreamEndEvent expectStreamEnd() {
			return (StreamEndEvent) expect(Event.ID.StreamEnd);
		}
		
		public SequenceStartEvent expectSequenceStart() {
			return (SequenceStartEvent) expect(Event.ID.SequenceStart);
		}
		
		public SequenceEndEvent expectSequenceEnd() {
			return (SequenceEndEvent) expect(Event.ID.SequenceEnd);
		}
		
		public ScalarEvent expectScalar() {
			return (ScalarEvent) expect(Event.ID.Scalar);
		}
		
		public Event expect(Event.ID id) {
			Event event = next();
			if (! event.is(id)) {
				throw new IllegalArgumentException("expected " + id + " and was " + event);
			}
			return event;
		}

		public Map<String, String> expectMap() {
			expectMappingStart();

			HashMap<String, String> map = new HashMap<>();
			
			while (true) {
				Event e = peek();
				
				if (e == null) { break; }
				if (e.is(Event.ID.MappingEnd)) { break; }
				
				map.put(
					expectScalar().getValue(), 
					expectScalar().getValue()
				);
			}
			
			expectMappingEnd();
			return map;
		}

		private List<String> expectList() {
			expectSequenceStart();
			List<String> list = new LinkedList<>();
			
			while(true) {
				Event e = peek();
				
				if (e == null) { break; }
				if (e.is(Event.ID.SequenceEnd)) { break; }
				
				list.add(expectScalar().getValue());
			}
			
			expectSequenceEnd();
			
			return list;
		}

		public boolean isSequenceStarting() {
			Event e = peek();
			if (e == null) { return false; }
			return e.is(Event.ID.SequenceStart);
		}
		
		public boolean isDocumentStarting() {
			Event e = peek();
			if (e == null) { return false; }
			return e.is(Event.ID.DocumentStart);
		}
		
		public boolean isMappingStarting() {
			Event e = peek();
			if (e == null) { return false; }
			return e.is(Event.ID.MappingStart);
		}
		
		public boolean isScalar() {
			Event e = peek();
			if (e == null) { return false; }
			return e.is(Event.ID.Scalar);
		}
	}
}
