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

/**
 * Configuration class for Henkinson.
 *
 * The idea of this class is simple: There's a config file somewhere, which is a JSON file. This class gets deserialized from that file
 * and then you can access the config options from the rest of the code.
 *
 * The config file must be a JSON file that can be found at one of the following locations:
 * <ul>
 *   <li>/etc/henkinson.conf</li>
 *   <li>etc/henkinson.conf</li>
 *   <li>henkinson.conf</li>
 * </ul>
 * The locations are searched in the given order, i.e. if there's one file in /etc and one in your local directory, the one in /etc wins.
 * A sample config file is provided in the etc folder of this repository, so when you run the project from the root directory of the
 * repo, that one will automatically be loaded.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Configuration {
	private static final String[] CONFIG_LOCATIONS = {"/etc/henkinson.conf", "etc/henkinson.conf", "henkinson.conf"};

	/**
	 * The base URL of the Jenkins API you want to access.
	 *
	 * This is usually something like http://hostname.foo/api/json
	 */
	@JsonProperty
	private String jenkinsBaseUrl;

	/**
	 * The user name for authentication against Jenkins.
	 */
	@JsonProperty
	private String username;

	/**
	 * The password for authentication against Jenkins.
	 *
	 * I strongly suggest using a token for that, not your actual password.
	 */
	@JsonProperty
	private String password;

	/**
	 * The log level for output.
	 *
	 * @see https://tinylog.org/configuration#level
	 */
	@JsonProperty
	private String loglevel;

	/**
	 * The killfile for the program.
	 *
	 * Henkinson will normally run in an endless loop. It will check for the existence of a file with the name specified here, and if that
	 * exists, it will be deleted and then Henkinson will terminate. Have a look at the service configuration in etc/henkinson.service to
	 * see how that can be used in a systemd service.
	 *
	 * You can also simply kill the service, but then the LED strip will stay on which is probably not what you want.
	 */
	@JsonProperty
	private String killfile;

	/**
	 * The GPIO pin to which your LED strip is connected.
	 *
	 * As the code uses WS2812 over PWM, only pins 18 and 10 are valid on a Pi Zero. I think that another pin is available on the Pi 3, but
	 * I don't own one, so I don't know and also don't care.
	 */
	@JsonProperty
	private int gpio;

	/**
	 * The brightness of the strip in a range of 0-255 with 0 being rather useless.
	 *
	 * I personally prefer using 255 and dimming using the colors used, as that can be changed at runtime. The overall brightness level is
	 * specified when the strip is initialized and can't be changed after that.
	 */
	@JsonProperty
	private int brightness;

	/**
	 * The number of pixels/LEDs in your strip.
	 */
	@JsonProperty
	private int pixels;

	@JsonIgnore
	private static Configuration instance;

	/**
	 * Returns an instance of this configuration class.
	 *
	 * This will always be the same instance after the first call, i.e. you're dealing with a Singleton here. Use this method instead of
	 * the constructor (which must be public because of JSON deserialization) as it will also <i>load</i> the configuration.
	 *
	 * @return The initialized configuration object.
	 * @throws IOException If the configuration can't be loaded for some reason.
	 */
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

	public String getKillfile() {
		return killfile;
	}

	public int getGpio() {
		return gpio;
	}

	public int getBrightness() {
		return brightness;
	}

	public int getPixels() {
		return pixels;
	}
}
