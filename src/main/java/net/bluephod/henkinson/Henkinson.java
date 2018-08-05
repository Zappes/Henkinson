package net.bluephod.henkinson;

import com.diozero.ws281xj.LedDriverInterface;
import com.diozero.ws281xj.PixelAnimations;
import com.diozero.ws281xj.PixelColour;
import com.diozero.ws281xj.rpiws281x.WS281x;

public class Henkinson {
	public static void main(String[] args) {
		int gpio_num = 18;
		//int gpio_num = 10;
		int brightness = 255;  // 0..255
		//int num_pixels = 12;
		int num_pixels = 60;

		System.out.println("Using GPIO " + gpio_num);

		try(LedDriverInterface led_driver = new WS281x(gpio_num, brightness, num_pixels)) {
			rainbowColours(led_driver);
			test2(led_driver);
			hsbTest(led_driver);
			hslTest(led_driver);

			while(true) {
				System.out.println("loop()");
				PixelAnimations.demo(led_driver);
			}
		}
	}

	private static void rainbowColours(LedDriverInterface ledDriver) {
		System.out.println("rainbowColours()");

		int[] colours = PixelColour.RAINBOW;

		for(int i = 0; i < 250; i++) {
			for(int pixel = 0; pixel < ledDriver.getNumPixels(); pixel++) {
				ledDriver.setPixelColour(pixel, colours[(i + pixel) % colours.length]);
			}

			ledDriver.render();
			PixelAnimations.delay(50);
		}
	}

	private static void test2(LedDriverInterface ledDriver) {
		System.out.println("test2()");

		// Set all off
		ledDriver.allOff();

		int delay = 20;

		// Gradually add red
		System.out.println("Adding red...");
		for(int i = 0; i < 256; i += 2) {
			for(int pixel = 0; pixel < ledDriver.getNumPixels(); pixel++) {
				ledDriver.setRedComponent(pixel, i);
			}

			ledDriver.render();
			PixelAnimations.delay(delay);
		}

		// Gradually add green
		System.out.println("Adding green...");
		for(int i = 0; i < 256; i += 2) {
			for(int pixel = 0; pixel < ledDriver.getNumPixels(); pixel++) {
				ledDriver.setGreenComponent(pixel, i);
			}

			ledDriver.render();
			PixelAnimations.delay(delay);
		}

		// Gradually add blue
		System.out.println("Adding blue...");
		for(int i = 0; i < 256; i += 2) {
			for(int pixel = 0; pixel < ledDriver.getNumPixels(); pixel++) {
				ledDriver.setBlueComponent(pixel, i);
			}

			ledDriver.render();
			PixelAnimations.delay(delay);
		}

		// Set all off
		ledDriver.allOff();
	}

	private static void hsbTest(LedDriverInterface ledDriver) {
		System.out.println("hsbTest()");
		float brightness = 0.5f;

		for(float hue = 0; hue < 1; hue += 0.05f) {
			for(float saturation = 0; saturation <= 1; saturation += 0.05f) {
				for(int pixel = 0; pixel < ledDriver.getNumPixels(); pixel++) {
					ledDriver.setPixelColourHSB(pixel, hue, saturation, brightness);
				}
				ledDriver.render();
				PixelAnimations.delay(20);
			}
		}
	}

	private static void hslTest(LedDriverInterface ledDriver) {
		System.out.println("hslTest()");
		float luminance = 0.5f;

		for(float hue = 0; hue < 360; hue += (360 / 20)) {
			for(float saturation = 0; saturation <= 1; saturation += 0.05f) {
				for(int pixel = 0; pixel < ledDriver.getNumPixels(); pixel++) {
					ledDriver.setPixelColourHSL(pixel, hue, saturation, luminance);
				}
				ledDriver.render();
				PixelAnimations.delay(20);
			}
		}
	}
}
