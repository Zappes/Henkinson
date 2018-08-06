package net.bluephod.henkinson.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.pmw.tinylog.Logger;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Configuration {
	private static final String[] CONFIG_LOCATIONS = {"/etc/henkinson.conf", "etc/henkinson.conf", "henkinson.conf"};

	@JsonProperty
	private String jenkinsBaseUrl;
	@JsonProperty
	private String username;
	@JsonProperty
	private String password;
	@JsonProperty
	private String loglevel;

	@JsonIgnore
	private static Configuration instance;

	public static Configuration getInstance() throws IOException {
		if(instance != null) {
			return instance;
		}

		for(String configLocation : CONFIG_LOCATIONS) {
			Path configPath = Paths.get(configLocation);
			if(Files.exists(configPath) && Files.isRegularFile(configPath) && Files.isReadable(configPath)) {
				try(InputStream in = Files.newInputStream(configPath)) {
					Logger.info(String.format("Reading configuration from file %s", configPath));

					instance = new ObjectMapper().readValue(in, Configuration.class);
					return instance;
				}
			}
		}

		throw new FileNotFoundException("Could not find config file at default locations: " + String.join(", ", CONFIG_LOCATIONS));
	}

	public String getJenkinsBaseUrl() {
		return jenkinsBaseUrl;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getLoglevel() {
		return loglevel;
	}
}
