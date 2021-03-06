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
import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;
import org.pmw.tinylog.writers.FileWriter;

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
	 * Number of milliseconds for which Henkinson will wait after starup before attempting to make a http connection.
	 * <p>
	 * This can be useful if you experience problems with the boot order of your system, e.g. if you have to wait for some VPN to get
	 * properly connected before the Jenkins server can be reached.
	 */
	@JsonProperty int startDelay = 0;

	/**
	 * If a connection to Jenkins times out, this is the number of retries that will be attempted.
	 */
	@JsonProperty
	private int connectionRetries = 10;

	/**
	 * The delay between connection retries in milliseconds.
	 */
	@JsonProperty
	private int connectionRetryDelay = 5000;

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
	 * The name of the branch that's considered to be the master branch of a project.
	 *
	 * If you use git, this should normally be "master".
	 */
	@JsonProperty
	private String masterBranchName = "master";

	/**
	 * Controls if feature branches should be considered.
	 *
	 * If this property is set to false, only the master branches of all projects will be counted for the stats. If it is true, <i>all</i>
	 * branches are counted.
	 */
	@JsonProperty
	private boolean includeFeatureBranches = false;

	/**
	 * The log level for output.
	 *
	 * @see https://tinylog.org/configuration#level
	 */
	@JsonProperty
	private String loglevel = "WARNING";

	@JsonProperty
	private String logfile = "./henkinson.log";

	/**
	 * Controls if the LED strip will be used.
	 *
	 * If this is set to false, the thing can run on a normal PC, thus making development of non-strippy features a lot easier.
	 */
	@JsonProperty
	private boolean stripEnabled = true;

	/**
	 * Controls if the GUI is shown.
	 *
	 * Only activate this when you started the tool manually or configured it to be displayed on a TTY, ideally with some kind of display
	 * attached. It will show a text-based UI with additional status information.
	 */
	@JsonProperty
	private boolean guiEnabled = false;

	/**
	 * If this is true, a blinker will be activated on the ledGpio pin.
	 * <p>
	 * Make sure to add a current limiting resistor between the GPIO and the LED. 50-100 ohms should be fine.
	 */
	@JsonProperty boolean ledEnabled = true;

	/**
	 * The GPIO pin on which the LED blinker will be active.
	 */
	@JsonProperty int ledGpio = 23;

	/**
	 * The on- and off-time of the LED blinker in ms.
	 */
	@JsonProperty int ledInterval = 1000;

	/**
	 * The pause in ms between polling cycles.
	 * <p>
	 * If this has the default value of 1000, the status will be polled every second. You should be able to figure out everything else by
	 * yourself.
	 */
	@JsonProperty
	private int pollingInterval = 1000;

	/**
	 * The GPIO pin to which your LED strip is connected.
	 *
	 * As the code uses WS2812 over PWM, only pins 18 and 10 are valid on a Pi Zero. I think that another pin is available on the Pi 3, but
	 * I don't own one, so I don't know and also don't care.
	 */
	@JsonProperty
	private int stripGpio = 18;

	/**
	 * GPIO pin for the buzzer.
	 */
	@JsonProperty
	private int buzzerGpio = 12;

	/**
	 * Number of ms that a morse dot is long.
	 * <p>
	 * A dash will always be thrice as long as the dot.
	 */
	@JsonProperty
	private int buzzerDitDuration = 70;

	/**
	 * If enabled, the Henkinson can be quite noisy.
	 */
	@JsonProperty
	private boolean buzzerEnabled = true;

	/**
	 * The brightness of the strip in a range of 0-255 with 0 being rather useless.
	 *
	 * I personally prefer using 255 and dimming using the colors used, as that can be changed at runtime. The overall brightness level is
	 * specified when the strip is initialized and can't be changed after that.
	 */
	@JsonProperty
	private int brightness = 64;

	/**
	 * The number of pixels/LEDs in your strip.
	 */
	@JsonProperty
	private int pixels = 120;

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

					Configurator.currentConfig()
							.level(Level.valueOf(instance.getLoglevel()))
							.writer(new FileWriter(instance.getLogfile()))
							.activate();

					return instance;
				}
			}
		}

		throw new FileNotFoundException("Could not find config file at default locations: " + String.join(", ", CONFIG_LOCATIONS));
	}

	public String getJenkinsBaseUrl() {
		return jenkinsBaseUrl;
	}

	public int getStartDelay() {
		return startDelay;
	}

	public int getConnectionRetries() {
		return connectionRetries;
	}

	public int getConnectionRetryDelay() {
		return connectionRetryDelay;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getMasterBranchName() {
		return masterBranchName;
	}

	public boolean isIncludeFeatureBranches() {
		return includeFeatureBranches;
	}

	public String getLoglevel() {
		return loglevel;
	}

	public String getLogfile() {
		return logfile;
	}

	public int getPollingInterval() {
		return pollingInterval;
	}

	public boolean isStripEnabled() {
		return stripEnabled;
	}

	public boolean isGuiEnabled() {
		return guiEnabled;
	}

	public boolean isLedEnabled() {
		return ledEnabled;
	}

	public int getStripGpio() {
		return stripGpio;
	}

	public int getBrightness() {
		return brightness;
	}

	public int getPixels() {
		return pixels;
	}

	public int getLedGpio() {
		return ledGpio;
	}

	public int getLedInterval() {
		return ledInterval;
	}

	public int getBuzzerGpio() {
		return buzzerGpio;
	}

	public boolean isBuzzerEnabled() {
		return buzzerEnabled;
	}

	public int getBuzzerDitDuration() {
		return buzzerDitDuration;
	}
}
