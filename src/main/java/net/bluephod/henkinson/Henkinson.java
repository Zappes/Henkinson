package net.bluephod.henkinson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.diozero.ws281xj.LedDriverInterface;
import com.diozero.ws281xj.rpiws281x.WS281x;
import net.bluephod.henkinson.config.Configuration;
import net.bluephod.henkinson.jenkins.DummyJenkins;
import net.bluephod.henkinson.jenkins.Jenkins;
import net.bluephod.henkinson.ledstrip.StripController;
import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;

public class Henkinson {
	private final Configuration config;

	public Henkinson() throws IOException {
		config = Configuration.getInstance();

		Configurator.currentConfig()
				.level(Level.valueOf(config.getLoglevel()))
				.activate();
	}

	public static void main(String[] args) throws Exception {
		int returnValue = new Henkinson().run();
		Logger.info("Exiting Henkinson.");
		System.exit(returnValue);
	}

	private int run() throws IOException, InterruptedException {
		// must be 18 or 10 - those pins can do PWM
		int gpioNum = config.getGpio();

		// 0..255
		int brightness = config.getBrightness();

		// number of LEDs in the strip
		int numPixels = config.getPixels();

		Logger.info(String.format("Using GPIO %d", gpioNum));

		try(LedDriverInterface ledDriver = new WS281x(gpioNum, brightness, numPixels)) {
			Jenkins jenkins = new DummyJenkins();
			StripController controller = new StripController(ledDriver);

			while(true) {
				controller.showStatus(jenkins.retrieveStatus());

				if(serviceShouldStop()) {
					Logger.info(String.format("Kill file (%s) found, deleting and exiting.", config.getKillfile()));
					break;
				}

				Thread.sleep(1000);
			}
		}

		return 0;
	}

	private boolean serviceShouldStop() throws IOException {
		return Files.deleteIfExists(Paths.get(config.getKillfile()));
	}
}
