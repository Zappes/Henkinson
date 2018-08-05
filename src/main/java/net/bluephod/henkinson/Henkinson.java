package net.bluephod.henkinson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.diozero.ws281xj.LedDriverInterface;
import com.diozero.ws281xj.rpiws281x.WS281x;
import net.bluephod.henkinson.jenkins.DummyJenkins;
import net.bluephod.henkinson.jenkins.Jenkins;
import net.bluephod.henkinson.ledstrip.StripController;
import org.pmw.tinylog.Logger;

public class Henkinson {
	private static final Path KILL_FILE = Paths.get("/home/pi/henkinson/killfile");

	public static void main(String[] args) throws Exception {
		// must be 18 or 10 - those pins can do PWM
		int gpioNum = 18;

		// 0..255
		int brightness = 64;

		// number of LEDs in the strip
		int numPixels = 60;

		Logger.info(String.format("Using GPIO %d", gpioNum));

		try(LedDriverInterface ledDriver = new WS281x(gpioNum, brightness, numPixels)) {
			Jenkins jenkins = new DummyJenkins();
			StripController controller = new StripController(ledDriver);

			while(true) {
				controller.showStatus(jenkins.retrieveStatus());

				if(serviceShouldStop()) {
					Logger.info(String.format("Kill file (%s) found, deleting and exiting.", KILL_FILE));
					break;
				}

				Thread.sleep(1000);
			}
		}

		Logger.info("Exiting Henkinson.");
		System.exit(0);
	}

	private static boolean serviceShouldStop() throws IOException {
		return Files.deleteIfExists(KILL_FILE);
	}
}
