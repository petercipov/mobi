package com.petercipov.mobi.config;

import com.petercipov.mobi.ExplicitTag;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.petercipov.mobi.ApiHost;
import com.petercipov.mobi.Registry;
import com.petercipov.mobi.config.ConfigParser.ConfKey;
import com.petercipov.mobi.config.ConfigParser.ConfigParseException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author pcipov
 */
public class ConfigParserTest {
	
	@Test
	public void parseEmpty() {
		Iterable<ConfigParser.ConfKey> keys = ConfigParser.toConfKeys();
		Iterable<ApiHost> hosts = ConfigParser.parseApiHost(keys);
		
		assertEquals(0, Iterables.size(hosts));
	}
	
	@Test
	public void parseHttpApi() {
		Iterable<ConfKey> keys = ConfigParser.toConfKeys(
			"api.remote1.type = http",
			"api.remote1.host = 192.168.56.101",
			"api.remote1.port = 2375"
		);
		
		List<ApiHost> hosts = Lists.newLinkedList(ConfigParser.parseApiHost(keys));
		assertEquals(1, hosts.size());

		HttpRestApiHost httpHost = (HttpRestApiHost) hosts.get(0);
		assertEquals("remote1", httpHost.getId());
		assertEquals("192.168.56.101", httpHost.getHost());
		assertEquals(2375, httpHost.getPort());
		assertFalse(httpHost.getDefaultVolumeBindings().isPresent());
	}
	
	@Test(expected = ConfigParseException.class)
	public void missingHostInHttp() {
		Iterable<ConfKey> keys = ConfigParser.toConfKeys(
			"api.remote1.type = http",
			"api.remote1.port = 2375"
		);
		
		Lists.newLinkedList(ConfigParser.parseApiHost(keys));
	}
	
	@Test(expected = ConfigParseException.class)
	public void missingPortInHttp() {
		Iterable<ConfKey> keys = ConfigParser.toConfKeys(
			"api.remote1.type = http",
			"api.remote1.host = 192.168.56.101"
		);
		
		Lists.newLinkedList(ConfigParser.parseApiHost(keys));
	}
	
	@Test(expected = ConfigParseException.class)
	public void missingTypeInApi() {
		Iterable<ConfKey> keys = ConfigParser.toConfKeys(
			"api.remote1.host = 192.168.56.101",
			"api.remote1.port = 2375"
		);
		
		Lists.newLinkedList(ConfigParser.parseApiHost(keys));
	}
	
	@Test
	public void parseHttpApiWithOptionals() {
		Iterable<ConfKey> keys = ConfigParser.toConfKeys(
			"api.remote2.type = http",
			"api.remote2.host = 192.168.56.102",
			"api.remote2.port = 2375",
			"api.remote2.volumeBindings = /var/log:var/log;/etc/passwd:/etc/passwd"
		);
		
		List<ApiHost> hosts = Lists.newLinkedList(ConfigParser.parseApiHost(keys));
		assertEquals(1, hosts.size());

		HttpRestApiHost httpHost = (HttpRestApiHost) hosts.get(0);
		assertEquals("remote2", httpHost.getId());
		assertEquals("192.168.56.102", httpHost.getHost());
		assertEquals(2375, httpHost.getPort());
		assertEquals(
			Arrays.asList("/var/log:var/log", "/etc/passwd:/etc/passwd"), 
			httpHost.getDefaultVolumeBindings().get()
		);
	}
	
	@Test
	public void parseHttps() {
		Iterable<ConfKey> keys = ConfigParser.toConfKeys(
			"api.remote2.type = https",
			"api.remote2.host = 192.168.56.102",
			"api.remote2.port = 2375",
			"api.remote2.cert = /home/pc/.ssh/key.cert",
			"api.remote2.volumeBindings = /var/log:var/log;/etc/passwd:/etc/passwd"
		);
		
		List<ApiHost> hosts = Lists.newLinkedList(ConfigParser.parseApiHost(keys));
		assertEquals(1, hosts.size());

		HttpsRestApiHost httpsHost = (HttpsRestApiHost) hosts.get(0);
		assertEquals("remote2", httpsHost.getId());
		assertEquals("192.168.56.102", httpsHost.getHost());
		assertEquals(2375, httpsHost.getPort());
		assertEquals("/home/pc/.ssh/key.cert", httpsHost.getCertPath());
		assertTrue(httpsHost.getDefaultVolumeBindings().isPresent());
		assertEquals(
			Arrays.asList("/var/log:var/log", "/etc/passwd:/etc/passwd"), 
			httpsHost.getDefaultVolumeBindings().get()
		);
	}
	
	@Test
	public void parseUnixFileSocket() {
		Iterable<ConfKey> keys = ConfigParser.toConfKeys(
			"api.remote2.type = unix",
			"api.remote2.path = /var/run/docker.sock",
			"api.remote2.host = 192.168.56.102",
			"api.remote2.volumeBindings = /var/log:var/log;/etc/passwd:/etc/passwd"
		);
		
		List<ApiHost> hosts = Lists.newLinkedList(ConfigParser.parseApiHost(keys));
		assertEquals(1, hosts.size());

		UnixRestApiHost unixApi = (UnixRestApiHost) hosts.get(0);
		assertEquals("remote2", unixApi.getId());
		assertEquals("192.168.56.102", unixApi.getHost());
		assertEquals("/var/run/docker.sock", unixApi.getPath());
		assertTrue(unixApi.getDefaultVolumeBindings().isPresent());
		assertEquals(
			Arrays.asList("/var/log:var/log", "/etc/passwd:/etc/passwd"), 
			unixApi.getDefaultVolumeBindings().get()
		);
	}
	
	@Test
	public void parseRegistry() {
		Iterable<ConfKey> keys = ConfigParser.toConfKeys(
			"registry.host = docker-registry.petercipov.com",
			"registry.port = 5000"
		);
		
		Registry registry = ConfigParser.parseRegistry(keys);
		RemoteRegistry remote = (RemoteRegistry) registry;
		
		assertEquals("docker-registry.petercipov.com", remote.getHost());
		assertEquals(5000, remote.getPort());
	}
	
	@Test
	public void emptyConfigReturnsLocalRegistry() {
		Iterable<ConfKey> keys = ConfigParser.toConfKeys();
		
		Registry registry = ConfigParser.parseRegistry(keys);
		assertTrue(registry instanceof LocalRegistry);
	}
	
	@Test
	public void fullNameInTagsIsSupported() {
		Iterable<ConfKey> keys = ConfigParser.toConfKeys(
			"tag.pc.ubuntu.master = latest"
		);
		
		LinkedList<ExplicitTag> list = Lists.newLinkedList(ConfigParser.parseExplicitTags(keys));
		assertEquals(1, list.size());
		
		ExplicitTag tag = list.get(0);
		assertEquals("pc", tag.getRepository().get());
		assertEquals("ubuntu", tag.getName());
		assertEquals("master", tag.getTag());
		assertEquals("latest", tag.getExplicitTag());
	}
	
	@Test
	public void shortNameIsSupported() {
		Iterable<ConfKey> keys = ConfigParser.toConfKeys(
			"tag.ubuntu.master = latest"
		);
		
		LinkedList<ExplicitTag> list = Lists.newLinkedList(ConfigParser.parseExplicitTags(keys));
		assertEquals(1, list.size());
		
		ExplicitTag tag = list.get(0);
		assertFalse(tag.getRepository().isPresent());
		assertEquals("ubuntu", tag.getName());
		assertEquals("master", tag.getTag());
		assertEquals("latest", tag.getExplicitTag());
	}
}