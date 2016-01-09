package com.petercipov.mobi.config;

import com.petercipov.mobi.ExplicitTag;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.petercipov.mobi.Registry;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import java.io.StringReader;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

/**
 *
 * @author pcipov
 */
public final class ConfigParser {
	
	private static final String KEY_DELIMITER = ".";
	private static final String VALUE_DELIMITER = ";";
	private static final String CERT_ATTR = "cert";
	private static final String PATH_ATTR = "path";
	private static final String VOLUME_BINDINGS_ATTR = "volumeBindings";
	private static final String PORT_ATTR = "port";
	private static final String HOST_ATTR = "host";
	private static final String TYPE_ATTR = "type";
	
	private ConfigParser() {}
	
	public static Iterable<ConfKey> toConfKeys(String ... lines) {
		return toConfKeys(toProperies(lines));
	}
	
	public static Iterable<ConfKey> toConfKeys(Properties properties) {
		return Iterables.transform(
			properties.entrySet(), 
			(entry) -> new ConfKey(
				entry.getKey().toString(), 
				entry.getValue().toString()
			));
	}
	
	public static Properties toProperies(String ... lines) {
		String config = Joiner.on("\n").join(lines);
		try {
			Properties p = new Properties();
			p.load(new StringReader(config));
			return p;
		} catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
	}
	
	public static DockerConfig parse(Properties p) {
		return parse(toConfKeys(p));
	}
	
	public static DockerConfig parse(Iterable<ConfKey> conf) {
		Set<ApiHost> apis = Sets.newHashSet(parseApiHost(conf));
		
		if (apis.isEmpty()) {
			throw new ConfigParseException("Expecting at leat one api host");
		}
		
		Registry registry = parseRegistry(conf);
		List<ExplicitTag> tags = Lists.newLinkedList(parseExplicitTags(conf));
		
		return new DockerConfig(apis, registry, tags);
	}

	public static Iterable<ApiHost> parseApiHost(Iterable<ConfKey> conf) {
		Map<String, Collection<ConfKey>> byName = Multimaps.index(
			Iterables.filter(conf, (c) -> c.getKey().size() >= 3 && "api".equalsIgnoreCase(c.getKey().get(0))),
			(c) -> c.getKey().get(1)
		).asMap();
		
		return Iterables.transform(byName.entrySet(), (configEntries) -> {
			String apiId = configEntries.getKey();
			
			Optional<String> type = opt(configEntries.getValue(), (c) -> TYPE_ATTR.equalsIgnoreCase(c.getKey().get(2)));
			if (! type.isPresent()) {
				throw new ConfigParseException("missing type specification for api "+apiId);
			}
			
			switch(type.get().toLowerCase()) {
				case "http": 
					return toHttpAPI(apiId, configEntries.getValue());
				case "https":
					return toHttpsAPI(apiId, configEntries.getValue());
				case "unix":
					return toUnixAPI(apiId, configEntries.getValue());
				default:
					throw new ConfigParseException("Unknown type "+ type.get()+" for config "+apiId);
			}
		});
	}
	
	private static UnixRestApiHost toUnixAPI(String apiId, Collection<ConfKey> configEntries) {
		Optional<String> host = opt(configEntries, (c) -> HOST_ATTR.equalsIgnoreCase(c.getKey().get(2)));
		Optional<String> path = opt(configEntries, (c) -> PATH_ATTR.equalsIgnoreCase(c.getKey().get(2)));
		Optional<List<String>> volumeBindings = opt(configEntries, (c) -> VOLUME_BINDINGS_ATTR.equalsIgnoreCase(c.getKey().get(2)))
			.map(ConfigParser::splitValue)
			.map(iterable -> Lists.newLinkedList(iterable));
		
		return new UnixRestApiHost(
			apiId,
			path.orElseThrow(() -> new ConfigParseException("path is missing for config "+apiId)),
			host.orElseThrow(() -> new ConfigParseException("host is missing for config "+apiId)),
			volumeBindings
		);
	}
	
	private static HttpsRestApiHost toHttpsAPI(String apiId, Collection<ConfKey> configEntries) {
		Optional<String> host = opt(configEntries, (c) -> HOST_ATTR.equalsIgnoreCase(c.getKey().get(2)));
		Optional<Integer> port = opt(configEntries, (c) -> PORT_ATTR.equalsIgnoreCase(c.getKey().get(2)))
			.map(Integer::parseInt);
		Optional<String> cert = opt(configEntries, (c) -> CERT_ATTR.equalsIgnoreCase(c.getKey().get(2)));
		Optional<List<String>> volumeBindings = opt(configEntries, (c) -> VOLUME_BINDINGS_ATTR.equalsIgnoreCase(c.getKey().get(2)))
			.map(ConfigParser::splitValue)
			.map(iterable -> Lists.newLinkedList(iterable));
		
		return new HttpsRestApiHost(
			apiId, 
			host.orElseThrow(() -> new ConfigParseException("host is missing for config "+apiId)), 
			port.orElseThrow(() -> new ConfigParseException("port is missing for config "+apiId)),
			cert.orElseThrow(() -> new ConfigParseException("cert is missing fot config "+apiId)), 
			volumeBindings
		);
	}

	private static HttpRestApiHost toHttpAPI(String apiId, Collection<ConfKey> configEntries) {
		Optional<String> host = opt(configEntries, (c) -> HOST_ATTR.equalsIgnoreCase(c.getKey().get(2)));
		Optional<Integer> port = opt(configEntries, (c) -> PORT_ATTR.equalsIgnoreCase(c.getKey().get(2)))
			.map(Integer::parseInt);
		Optional<List<String>> volumeBindings = opt(configEntries, (c) -> VOLUME_BINDINGS_ATTR.equalsIgnoreCase(c.getKey().get(2)))
			.map(ConfigParser::splitValue)
			.map(iterable -> Lists.newLinkedList(iterable));

		return new HttpRestApiHost(
			apiId, 
			host.orElseThrow(() -> new ConfigParseException("host is missing for config "+apiId)), 
			port.orElseThrow(() -> new ConfigParseException("port is missing for config "+apiId)), 
			volumeBindings
		);
	}
	
	private static Optional<String> opt(Iterable<ConfKey> confs, Predicate<ConfKey> predicate) {
		ConfKey key = Iterables.find(confs, predicate, null);
		return key == null 
			? Optional.empty() 
			: Optional.of(key.getValue());
	}
	
	public static Iterable<ExplicitTag> parseExplicitTags(Iterable<ConfKey> keys) {
		Iterable<ConfKey> byTag = Iterables.filter(keys, (key) -> key.getKey().size() >=3 && "tag".equals(key.getKey().get(0)));
		
		return Iterables.transform(byTag, (c) -> {
			Optional<String> repository;
			String name;
			String tag;
			if (c.getKey().size() >= 4) {
				repository = Optional.of(c.getKey().get(1));
				name = c.getKey().get(2);
				tag = c.getKey().get(3);
			} else {
				repository = Optional.empty();
				name = c.getKey().get(1);
				tag = c.getKey().get(2);
			}
			
			return new ExplicitTag(repository, name, tag, c.getValue());
		});
	}

	static Registry parseRegistry(Iterable<ConfKey> keys) {
		Iterable<ConfKey> byRegistry = Iterables.filter(keys, (key) -> key.getKey().size() >=2 && "registry".equals(key.getKey().get(0)));
		
		Optional<String> host = opt(byRegistry, (c) -> HOST_ATTR.equalsIgnoreCase(c.getKey().get(1)));
		Optional<Integer> port = opt(byRegistry, (c) -> PORT_ATTR.equalsIgnoreCase(c.getKey().get(1)))
			.map(Integer::parseInt);
		
		if (host.isPresent() && port.isPresent()) {
			return new RemoteRegistry(host.get(), port.get());
		} else {
			return new LocalRegistry();
		}
	}

	private static Iterable<String> splitKey(String raw) {
		return Splitter
			.on(KEY_DELIMITER)
			.trimResults()
			.split(raw);
	}
	
	private static Iterable<String> splitValue(String raw) {
		return Splitter
			.on(VALUE_DELIMITER)
			.trimResults()
			.split(raw);
	}

	public static List<Entry<String, String>> toPropertiesList(Properties properties) {
		List<Entry<String, String>> list = new ArrayList<>();
		
		for (Entry<Object, Object> entry : properties.entrySet()) {
			list.add(new AbstractMap.SimpleEntry<>(entry.getKey().toString(), entry.getValue().toString()));
		}
		
		return list;
	}
	
	public static class ConfKey {
		private final List<String> key;
		private final String value;

		public ConfKey(String key, String value) {
			this(Lists.newLinkedList(splitKey(key)), value);
		}
		
		public ConfKey(List<String> key, String value) {
			this.key = key;
			this.value = value;
		}

		public List<String> getKey() {
			return key;
		}

		public String getValue() {
			return value;
		}

		@Override
		public String toString() {
			return "ConfKey(" + "key=" + key + ", value=" + value + ')';
		}
	}
	
	public static class ConfigParseException extends RuntimeException {

		public ConfigParseException(String message) {
			super(message);
		}
	}
}