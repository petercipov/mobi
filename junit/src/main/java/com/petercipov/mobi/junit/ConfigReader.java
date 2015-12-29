package com.petercipov.mobi.junit;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.petercipov.mobi.config.ConfigParser;
import com.petercipov.mobi.config.DockerConfig;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Optional;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author pcipov
 */
public class ConfigReader {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigReader.class);
	
	private static final String ENV_DOCKER_CONFIG_FILE = "DOCKER_CONFIG_FILE";
	private static final String SYS_DOCKER_CONFIG_FILE = "docker.config.file";

	public DockerConfig readFromDefaultLocations() throws IOException{
		Optional<String> confFileString = fromConfigFile();
		
		Properties p = new Properties();
		
		confFileString.ifPresent((conf) ->load(p, conf));
		
		if (p.isEmpty()) {
			throwMissingConfig();
		}
		
		DockerConfig dockerConfig = ConfigParser.parse(p);
		LOGGER.info("Test configuration has been parsed {}", dockerConfig);
		return dockerConfig;
	}
	
	private void load(Properties p, String value) {
		try {
			p.load(new StringReader(value));
		} catch (Exception ex) {
			throw new IllegalArgumentException(ex);
		}
	}
	
	private Optional<String> fromConfigFile() throws IOException {
		String fileInput = System.getProperty(SYS_DOCKER_CONFIG_FILE, System.getenv(ENV_DOCKER_CONFIG_FILE));
		File inputFile;
		if (fileInput == null) {			
			fileInput = "docker.properties";
			inputFile = new File(fileInput);
		} else {
			inputFile = new File(fileInput);
		}
		if (inputFile.exists()) {
			LOGGER.info("Using conf file {}", inputFile.getAbsolutePath());
			return Optional.of(Files.toString(inputFile, Charsets.UTF_8));
		} else {
			return Optional.empty();
		}
	}

	private void throwMissingConfig() throws IOException {
		File root = new File(".");
		throw new IOException(String.format(
				"Missing configuration! \n"
				+ "You can configure via: \n"
				+ "-> Property file docker.properties in %s \n"
				+ "-> Environment variable %s for docker.properties path \n"
				+ "-> System property (-D) %s for docker.properties path \n",
				
				root.getAbsolutePath(),
				
				ENV_DOCKER_CONFIG_FILE,
				SYS_DOCKER_CONFIG_FILE
		));
	}
}