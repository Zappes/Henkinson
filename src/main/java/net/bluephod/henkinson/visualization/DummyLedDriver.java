package net.bluephod.henkinson.visualization;

import java.util.Arrays;

import com.diozero.ws281xj.LedDriverInterface;

/**
 * A LED driver that behaves exactly like the one for the strip, only without actually doing anything.
 *
 * Provide this one to a HenkinsonCanvas and you can run the tool on a machine that's not a raspberry pi.
 */
public class DummyLedDriver implements LedDriverInterface {
	private int numberOfPixels;
	private int[] pixelBuffer;

	public DummyLedDriver(final int numberOfPixels) {
		this.numberOfPixels = numberOfPixels;
		this.pixelBuffer = new int[numberOfPixels];
	}

	@Override
	public void close() {
		// nothing to be done here as we don't really do anything.
	}

	@Override
	public int getNumPixels() {
		return numberOfPixels;
	}

	@Override
	public void render() {
		// at some point in time we could dump the pixel array to the console or something like that.
	}

	@Override
	public void allOff() {
		Arrays.fill(pixelBuffer, 0);
	}

	@Override
	public int getPixelColour(final int pixel) {
		return pixelBuffer[pixel];
	}

	@Override
	public void setPixelColour(final int pixel, final int colour) {
		pixelBuffer[pixel] = colour;
	}
}
