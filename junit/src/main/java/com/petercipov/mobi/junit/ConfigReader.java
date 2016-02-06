package com.petercipov.mobi.junit;

import com.petercipov.mobi.config.MobiConfig;
import com.petercipov.mobi.config.YamlConfigParser;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author pcipov
 */
public class ConfigReader {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigReader.class);
	
	private static final String ENV_MOBI_CONFIG_FILE = "MOBI_CONFIG_FILE";
	private static final String SYS_MOBI_CONFIG_FILE = "mobi.config.file";

	public MobiConfig readFromDefaultLocations() throws IOException{
		File confFile = fromConfigFile();
		YamlConfigParser parser = new YamlConfigParser();
		MobiConfig dockerConfig = parser.parse(new FileInputStream(confFile));
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
	
	private File fromConfigFile() throws IOException {
		String fileInput = System.getProperty(SYS_MOBI_CONFIG_FILE, System.getenv(ENV_MOBI_CONFIG_FILE));
		File inputFile;
		if (fileInput == null) {			
			fileInput = "mobi.yml";
			inputFile = new File(fileInput);
		} else {
			inputFile = new File(fileInput);
		}
		if (inputFile.exists()) {
			LOGGER.info("Using conf file {}", inputFile.getAbsolutePath());
			return inputFile;
		} else {
			File root = new File(".");
			throw new IOException(String.format(
				"Missing configuration! \n"
				+ "You can configure via: \n"
				+ "-> Property file docker.properties in %s \n"
				+ "-> Environment variable %s for mobi.yml path \n"
				+ "-> System property (-D) %s for mobi.yml path \n",

				root.getAbsolutePath(),

				ENV_MOBI_CONFIG_FILE,
				SYS_MOBI_CONFIG_FILE
			));
		}
	}
}