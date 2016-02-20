package com.petercipov.mobi.config;

import com.petercipov.mobi.TagOverride;
import com.petercipov.mobi.Registry;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

/**
 *
 * @author petercipov
 */
public class YamlConfigParserTest {
	
	private YamlConfigParser parser;
	
	@Before
	public void before() {
		parser = new YamlConfigParser();
	}
	
	@Test
	public void httpApiCanBespecied() {
		MobiConfig config = parser.parse(stream(
			"---\n" +
			"api:\n" +
			"    - type: http\n" +
			"      host: 192.168.56.101\n" +
			"      port: 2375\n" +
			"    "
		));
		
		assertEquals(1, config.getApis().size());
		assertTrue(config.getApis().get(0) instanceof HttpRestApiHost);
		HttpRestApiHost host  = (HttpRestApiHost) config.getApis().get(0);
		assertEquals("192.168.56.101", host.getHost());
		assertEquals(2375, host.getPort());
	}
	
	@Test(expected = Exception.class)
	public void typeInApiSpecMandatory() {
		parser.parse(stream(
			"---\n" +
			"api:\n" +
			"    - host: 192.168.56.101\n" +
			"      port: 2375\n" +
			"    "
		));
	}
	
	@Test(expected = Exception.class)
	public void hostIsMandatoryForHttpApi() {
		parser.parse(stream(
			"---\n" +
			"api:\n" +
			"    - type: http\n" +
			"      port: 2375\n" +
			"    "
		));
	}
	
	@Test(expected = Exception.class)
	public void portIsMandatoryForHttpApi() {
		parser.parse(stream(
			"---\n" +
			"api:\n" +
			"    - type: http\n" +
			"      host: 123.234.12.12\n" +
			"    "
		));
	}
	
	@Test
	public void volumebindingsAreOptionalyParsed() {
		MobiConfig config = parser.parse(stream(
			"api:\n" +
			"    - type: http\n" +
			"      host: 192.168.56.101\n" +
			"      port: 2375\n" +
			"      volumes: \n" +
			"        - /var/log:var/log\n" +
			"        - /etc/passwd:/etc/passwd\n" +
			"    "
		));
		
		assertEquals(1, config.getApis().size());
		assertTrue(config.getApis().get(0) instanceof HttpRestApiHost);
		HttpRestApiHost host  = (HttpRestApiHost) config.getApis().get(0);
		Optional<List<String>> bindings = host.getVolumeBindings();
		assertNotNull(bindings);
		assertTrue(bindings.isPresent());
		List<String> list = bindings.get();
		assertEquals(2, list.size());
		assertTrue(list.contains("/var/log:var/log"));
		assertTrue(list.contains("/etc/passwd:/etc/passwd"));
	}
	
	
	@Test
	public void httpsApiCanBeSpecified() {
		MobiConfig config = parser.parse(stream(
			"---\n" +
			"api:\n" +
			"    - type: https\n" +
			"      host: 192.168.56.101\n" +
			"      port: 2375\n" +
			"      cert: /var/lib/cert.key\n" +
			"    "
		));
		
		assertEquals(1, config.getApis().size());
		assertTrue(config.getApis().get(0) instanceof HttpsRestApiHost);
		HttpsRestApiHost host  = (HttpsRestApiHost) config.getApis().get(0);
		assertEquals("192.168.56.101", host.getHost());
		assertEquals(2375, host.getPort());
		assertEquals("/var/lib/cert.key", host.getCertPath());
	}
	
	@Test(expected = Exception.class)
	public void hostIsMandatoryForHttpsApi() {
		parser.parse(stream(
			"---\n" +
			"api:\n" +
			"    - type: https\n" +
			"      port: 2375\n" +
			"      cert: /etc/a.key\n" +
			"    "
		));
	}
	
	@Test(expected = Exception.class)
	public void portIsMandatoryForHttpsApi() {
		parser.parse(stream(
			"---\n" +
			"api:\n" +
			"    - type: https\n" +
			"      host: 123.234.12.12\n" +
			"      cert: /etc/a.key\n" +
			"    "
		));
	}
	
	@Test(expected = Exception.class)
	public void certIsMandatoryForHttpsApi() {
		parser.parse(stream(
			"---\n" +
			"api:\n" +
			"    - type: https\n" +
			"      host: 123.234.12.12\n" +
			"      port: 2375\n" +
			"    "
		));
	}
	
	@Test(expected = Exception.class)
	public void unixApiCanBeSpecified() {
		MobiConfig config = parser.parse(stream(
			"---\n" +
			"api:\n" +
			"    - type: unix\n" +
			"      path: /var/run/docker.sock\n" +
			"    "
		));
		
		assertEquals(1, config.getApis().size());
		assertTrue(config.getApis().get(0) instanceof UnixRestApiHost);
		UnixRestApiHost host  = (UnixRestApiHost) config.getApis().get(0);
		assertEquals("/var/run/docker.sock", host.getPath());
	}
	
	@Test(expected = Exception.class)
	public void pathIsMandatoryForHttpsApi() {
		parser.parse(stream(
			"---\n" +
			"api:\n" +
			"    - type: unix\n" +
			"    "
		));
	}
	
	@Test
	public void customRegstryCanBeSpecified() {
		MobiConfig config = parser.parse(stream(
			"registry:\n" +
			"    host: docker-registry.petercipov.com\n" +
			"    port: 5000\n"
		));
		
		Registry registry = config.getRegistry();
		assertNotNull(registry);
		
		assertEquals("docker-registry.petercipov.com:5000/", registry.getConnectionString());
	}
	
	@Test(expected = Exception.class)
	public void hostIsMandatoryInRegistrySpec() {
		parser.parse(stream(
			"registry:\n" +
			"    port: 5000\n"
		));
	}
	
	@Test(expected = Exception.class)
	public void portIsMandatoryInRegistrySpec() {
		parser.parse(stream(
			"registry:\n" +
			"    host: docker-registry.petercipov.com\n"
		));
	}
	
	@Test
	public void ifRegistryNotSpecifiedDefaultRegistryIsUsed() {
		MobiConfig config = parser.parse(stream(
			""
		));
		
		Registry registry = config.getRegistry();
		assertNotNull(registry);
		
		assertEquals("", registry.getConnectionString());
	}
	
	@Test
	public void multitudeOfApiEndpointsCanBeSpecified() {
		MobiConfig config = parser.parse(stream(
			"---\n" +
			"api:\n" +
			"    - type: https\n" +
			"      host: 123.234.12.12\n" +
			"      port: 1234\n" +
			"      cert: /etc/a.key\n" +
					
			"    - type: http\n" +
			"      host: 192.168.56.101\n" +
			"      port: 2375\n" +
			"    "
		));
		
		List<ApiHost> apis = config.getApis();
		assertNotNull(apis);
		assertEquals(2, apis.size());
	}
	
	@Test
	public void explicitTagsCanbeSpecified() {
		MobiConfig config = parser.parse(stream(
			"override:\n" +
			"    - pc.ubuntu.444: 500\n"
		));
		
		List<TagOverride> tags = config.getTags();
		assertNotNull(tags);
		assertEquals(1, tags.size());
		TagOverride tag = tags.get(0);
		
		assertEquals("pc", tag.getRepository().get());
		assertEquals("ubuntu", tag.getName());
		assertEquals("444", tag.getTag());
		assertEquals("500", tag.getOverride());
		
	}
	
	@Test
	public void multitudeOfTagsCanBeSpecified() {
		MobiConfig config = parser.parse(stream(
			"api:\n" +
			"    - type: http\n" +
			"      host: 192.168.56.101\n" +
			"      port: 2375\n" +
			"override:\n" +
			"    - pc.ubuntu.444: 500\n" +
			"    - pc.ubuntu.44: 50\n"+
			"    " 
		));
		
		List<TagOverride> tags = config.getTags();
		
		assertEquals(2, tags.size());
	}
	
	private InputStream stream(String s) {
		return new ByteArrayInputStream(s.getBytes(Charset.forName("UTF-8")));
	}
}
