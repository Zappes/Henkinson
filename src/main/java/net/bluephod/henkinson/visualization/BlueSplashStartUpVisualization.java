package net.bluephod.henkinson.visualization;

import com.diozero.ws281xj.PixelColour;

public class BlueSplashStartUpVisualization implements StartUpVisualization {
	@Override
	public void showStartUp(final HenkinsonCanvas canvas) {
		for(int blue = 255; blue >= 0; blue--) {
			int colour = PixelColour.createColourRGB(0, 0, blue);
			for(int column = 0; column < canvas.getNumberOfColumns(); column++) {
				canvas.setColumnColor(column, colour);
			}
			canvas.render();
		}
	}
}
