package net.bluephod.henkinson.visualization;

import java.util.Objects;

import com.diozero.ws281xj.LedDriverInterface;
import com.diozero.ws281xj.PixelColour;
import net.bluephod.henkinson.jenkins.JenkinsStatus;

/**
 * Visualizes the build status as a simple VU meter.
 * <p>
 * This visualization sets segments of the strip to red, yellow and green according to the proportions of the red, yellow and green
 * branches in the status, much like a VU meter.
 */
public class VuMeterBuildStatusVisualization implements BuildStatusVisualization {

	private static final int DELAY_PIXEL_FADE = 5;
	private static final int DELAY_MORPH = 25;
	private static final int SPEED_PIXEL_FADE = 2;

	private static final int COLOR_GREEN = PixelColour.createColourRGB(0, 255, 0);
	private static final int COLOR_YELLOW = PixelColour.createColourRGB(255, 255, 0);
	private static final int COLOR_RED = PixelColour.createColourRGB(255, 0, 0);

	private LedDriverInterface driver;
	private StatusLedDistribution currentDist;

	@Override
	public void init(final LedDriverInterface driver, final JenkinsStatus initialStatus) {
		if(driver == null) {
			throw new IllegalArgumentException("Driver may not be null");
		}

		this.driver = driver;
		this.currentDist = getDistribution(initialStatus);

		fadeToDistribution(currentDist);
	}

	@Override
	public void update(JenkinsStatus status) {
		StatusLedDistribution target = getDistribution(status);

		while(!currentDist.equals(target)) {
			currentDist = morphTo(currentDist, target);

			renderDistribution(currentDist);

			try {
				Thread.sleep(DELAY_MORPH);
			}
			catch(InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	@Override
	public void shutDown() {
		// nothing to be done here
	}

	protected StatusLedDistribution getDistribution(JenkinsStatus status) {
		double ledsPerCount = ((double) driver.getNumPixels()) / ((double) status.getTotal());

		int green = (int) Math.floor(ledsPerCount * status.getGreen());
		int yellow = (int) Math.floor(ledsPerCount * status.getYellow());
		int red = (int) Math.floor(ledsPerCount * status.getRed());

		int off = driver.getNumPixels() - (green + red + yellow);

		while(off > 0) {
			if(green != 0) {
				green++;
				off--;
			}
			else if(yellow != 0) {
				yellow++;
				off--;
			}
			else if(red != 0) {
				red++;
				off--;
			}
		}

		return new StatusLedDistribution(green, yellow, red);
	}

	protected StatusLedDistribution morphTo(StatusLedDistribution current, StatusLedDistribution target) {
		if(current.equals(target)) {
			return current;
		}

		if(current.getTotal() != target.getTotal()) {
			throw new IllegalArgumentException("Distribuitions for morphing must be of equal size");
		}

		int red = current.getRed();
		int yellow = current.getYellow();
		int green = current.getGreen();

		// there surely is a better way to do this, but this will work for the time being.

		if(red > target.getRed()) {
			red--;

			if(green < target.getGreen()) {
				green++;
			}
			else {
				yellow++;
			}
		}
		else if(yellow > target.getYellow()) {
			yellow--;

			if(green < target.getGreen()) {
				green++;
			}
			else {
				red++;
			}
		}
		else if(green > target.getGreen()) {
			green--;

			if(yellow < target.getYellow()) {
				yellow++;
			}
			else {
				red++;
			}
		}

		return new StatusLedDistribution(green, yellow, red);
	}

	protected void renderDistribution(StatusLedDistribution dist) {
		setPixelRange(0, dist.getGreen(), COLOR_GREEN);
		setPixelRange(dist.getGreen(), dist.getYellow(), COLOR_YELLOW);
		setPixelRange(dist.getGreen() + dist.getYellow(), dist.getRed(), COLOR_RED);

		driver.render();
	}

	protected void setPixelRange(int startIndex, int count, int colour) {
		for(int index = startIndex; index < startIndex + count; index++) {
			driver.setPixelColour(index, colour);
		}
	}

	protected int getPixelColor(StatusLedDistribution dist, int pixel) {
		if(pixel >= driver.getNumPixels()) {
			return 0;
		}

		if(pixel >= dist.getGreen() + dist.getYellow()) {
			return COLOR_RED;
		}

		if(pixel >= dist.getGreen()) {
			return COLOR_YELLOW;
		}

		return COLOR_GREEN;
	}

	protected void fadeToDistribution(StatusLedDistribution dist) {
		boolean changeHasOccurred = false;

		do {
			for(int pixel = 0; pixel < driver.getNumPixels(); pixel++) {
				changeHasOccurred |= fadePixelTowardsTarget(pixel, getPixelColor(dist, pixel));
			}

			driver.render();

			try {
				Thread.sleep(DELAY_PIXEL_FADE);
			}
			catch(InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		while(changeHasOccurred);
	}

	protected boolean fadePixelTowardsTarget(int pixel, int targetColor) {
		if(targetColor != driver.getPixelColour(pixel)) {
			int red = getNextFadeValue(driver.getRedComponent(pixel), PixelColour.getRedComponent(targetColor));
			int green = getNextFadeValue(driver.getGreenComponent(pixel), PixelColour.getGreenComponent(targetColor));
			int blue = getNextFadeValue(driver.getBlueComponent(pixel), PixelColour.getBlueComponent(targetColor));

			driver.setPixelColour(pixel, PixelColour.createColourRGB(red, green, blue));

			return true;
		}

		return false;
	}

	protected int getNextFadeValue(int current, int target) {
		if(current == target) {
			return current;
		}

		return current > target ? Math.max(current - SPEED_PIXEL_FADE, target) : Math.min(current + SPEED_PIXEL_FADE, target);
	}

	private static class StatusLedDistribution {
		private int green;
		private int yellow;
		private int red;

		public StatusLedDistribution(final int green, final int yellow, final int red) {
			this.green = green;
			this.yellow = yellow;
			this.red = red;
		}

		public int getGreen() {
			return green;
		}

		public int getYellow() {
			return yellow;
		}

		public int getRed() {
			return red;
		}

		public int getTotal() {
			return red + green + yellow;
		}

		@Override
		public int hashCode() {
			return Objects.hash(green, yellow, red);
		}

		@Override
		public boolean equals(final Object o) {
			if(this == o) {
				return true;
			}
			if(o == null || getClass() != o.getClass()) {
				return false;
			}
			StatusLedDistribution that = (StatusLedDistribution) o;
			return green == that.green &&
					yellow == that.yellow &&
					red == that.red;
		}

		@Override
		public String toString() {
			return "StatusLedDistribution{" +
					"green=" + green +
					", yellow=" + yellow +
					", red=" + red +
					'}';
		}
	}
}
