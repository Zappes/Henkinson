package net.bluephod.henkinson.visualization;

import com.diozero.ws281xj.LedDriverInterface;
import com.diozero.ws281xj.PixelColour;

public class BlueSplashStartUpVisualization implements StartUpVisualization {
	private LedDriverInterface driver;

	@Override
	public void showStartUp(final LedDriverInterface driver) {
		for(int blue = 255; blue >= 0; blue--) {
			int colour = PixelColour.createColourRGB(0, 0, blue);
			for(int pixel = 0; pixel < driver.getNumPixels(); pixel++) {
				driver.setPixelColour(pixel, colour);
			}
			driver.render();
		}
	}
}
