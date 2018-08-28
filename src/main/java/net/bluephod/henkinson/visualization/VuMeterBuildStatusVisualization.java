package net.bluephod.henkinson.visualization;

import java.io.IOException;
import java.util.Objects;

import com.diozero.ws281xj.PixelColour;
import com.diozero.ws281xj.rpiws281x.WS281x;
import net.bluephod.henkinson.Henkinson;
import net.bluephod.henkinson.HenkinsonUtil;
import net.bluephod.henkinson.config.Configuration;
import net.bluephod.henkinson.jenkins.JenkinsStatus;
import org.pmw.tinylog.Logger;

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

	private Configuration config;
	private Henkinson henkinson;

	private HenkinsonCanvas canvas;
	private StatusLedDistribution currentDist;

	@Override
	public void init(Configuration config, Henkinson henkinson) {
		this.config = config;
		this.henkinson = henkinson;
		this.currentDist = null;

		canvas = new HenkinsonCanvas(new WS281x(config.getGpio(), config.getBrightness(), config.getPixels()));
	}

	@Override
	public void update(JenkinsStatus status) {
		StatusLedDistribution target = getDistribution(status);

		if(currentDist == null) {
			// just fade to the distribution on the first update.
			currentDist = getDistribution(status);
			fadeToDistribution(currentDist);
		}
		else {
			while(!currentDist.equals(target)) {
				currentDist = morphTo(currentDist, target);

				Logger.trace("Morphing dist. Current is " + currentDist);

				renderDistribution(currentDist);

				HenkinsonUtil.sleep(DELAY_MORPH);
			}
		}
	}

	private StatusLedDistribution getDistribution(JenkinsStatus status) {
		double ledsPerCount = ((double) canvas.getNumberOfColumns()) / ((double) status.getTotal());

		int green = (int) Math.floor(ledsPerCount * status.getGreen());
		int yellow = (int) Math.floor(ledsPerCount * status.getYellow());
		int red = (int) Math.floor(ledsPerCount * status.getRed());

		int off = canvas.getNumberOfColumns() - (green + red + yellow);

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

	private StatusLedDistribution morphTo(StatusLedDistribution current, StatusLedDistribution target) {
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

	private void fadeToDistribution(StatusLedDistribution dist) {
		boolean changeHasOccurred;

		do {
			changeHasOccurred = false;

			for(int column = 0; column < canvas.getNumberOfColumns(); column++) {
				changeHasOccurred |= fadePixelTowardsTarget(column, getPixelColor(dist, column));
			}

			canvas.render();

			HenkinsonUtil.sleep(DELAY_PIXEL_FADE);
		}
		while(changeHasOccurred);
	}

	private boolean fadePixelTowardsTarget(int column, int targetColor) {
		if(targetColor != canvas.getColumnColor(column)) {
			int red = getNextFadeValue(canvas.getRedComponent(column), PixelColour.getRedComponent(targetColor));
			int green = getNextFadeValue(canvas.getGreenComponent(column), PixelColour.getGreenComponent(targetColor));
			int blue = getNextFadeValue(canvas.getBlueComponent(column), PixelColour.getBlueComponent(targetColor));

			canvas.setColumnColor(column, PixelColour.createColourRGB(red, green, blue));

			return true;
		}

		return false;
	}

	private int getPixelColor(StatusLedDistribution dist, int pixel) {
		if(pixel >= canvas.getNumberOfColumns()) {
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

	private void renderDistribution(StatusLedDistribution dist) {
		setPixelRange(0, dist.getGreen(), COLOR_GREEN);
		setPixelRange(dist.getGreen(), dist.getYellow(), COLOR_YELLOW);
		setPixelRange(dist.getGreen() + dist.getYellow(), dist.getRed(), COLOR_RED);

		canvas.render();
	}

	private void setPixelRange(int startIndex, int count, int colour) {
		for(int column = startIndex; column < startIndex + count; column++) {
			canvas.setColumnColor(column, colour);
		}
	}

	private int getNextFadeValue(int current, int target) {
		if(current == target) {
			return current;
		}

		return current > target ? Math.max(current - SPEED_PIXEL_FADE, target) : Math.min(current + SPEED_PIXEL_FADE, target);
	}

	@Override
	public void close() throws IOException {
		if(canvas != null) {
			canvas.close();
		}
	}

	private static class StatusLedDistribution {
		private int green;
		private int yellow;
		private int red;

		StatusLedDistribution(final int green, final int yellow, final int red) {
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
