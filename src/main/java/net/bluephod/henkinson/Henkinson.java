package net.bluephod.henkinson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.diozero.ws281xj.LedDriverInterface;
import com.diozero.ws281xj.PixelAnimations;
import com.diozero.ws281xj.PixelColour;
import com.diozero.ws281xj.rpiws281x.WS281x;
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
			while(true) {
				PixelAnimations.colourWipe(ledDriver, PixelColour.createColourRGB(255, 0, 0), 25); // Red
				PixelAnimations.colourWipe(ledDriver, PixelColour.createColourRGB(0, 0, 255), 25); // Blue

				if(serviceShouldStop()) {
					Logger.info(String.format("Kill file (%s) found, deleting and exiting.", KILL_FILE));
					break;
				}
			}
		}

		Logger.info("Exiting Henkinson.");
		System.exit(0);
	}

	private static boolean serviceShouldStop() throws IOException {
		return Files.deleteIfExists(KILL_FILE);
	}
}
