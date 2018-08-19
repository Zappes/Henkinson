package net.bluephod.henkinson.visualization;

import com.diozero.ws281xj.LedDriverInterface;
import com.diozero.ws281xj.PixelColour;

public class HenkinsonCanvas {
	private LedDriverInterface driver;

	public HenkinsonCanvas(final LedDriverInterface driver) {
		this.driver = driver;
	}

	public int getNumberOfColumns() {
		return driver.getNumPixels() / 2;
	}

	public void setColumnColor(int column, int color) {
		driver.setPixelColour(column, color);
		driver.setPixelColour(driver.getNumPixels() - column - 1, color);
	}

	public void render() {
		driver.render();
	}

	public int getRedComponent(final int column) {
		return PixelColour.getRedComponent(getColumnColor(column));
	}

	public int getColumnColor(int column) {
		return driver.getPixelColour(column);
	}

	public int getGreenComponent(final int column) {
		return PixelColour.getGreenComponent(getColumnColor(column));
	}

	public int getBlueComponent(final int column) {
		return PixelColour.getBlueComponent(getColumnColor(column));
	}
}
