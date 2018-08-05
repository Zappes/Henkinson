package net.bluephod.henkinson.ledstrip;

import java.util.Objects;

import com.diozero.ws281xj.LedDriverInterface;
import com.diozero.ws281xj.PixelColour;
import net.bluephod.henkinson.jenkins.JenkinsStatus;

public class StripController {

	private LedDriverInterface driver;
	private StatusLedDistribution currentDist;

	public StripController(final LedDriverInterface driver) {
		if(driver == null) {
			throw new IllegalArgumentException("Driver may not be null");
		}

		this.driver = driver;
		this.currentDist = getDistribution(new JenkinsStatus(1, 1, 1));
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

	public void showStatus(JenkinsStatus status) {
		StatusLedDistribution target = getDistribution(status);

		while(!currentDist.equals(target)) {
			currentDist = morphTo(currentDist, target);

			renderDistribution(currentDist);

			try {
				Thread.sleep(25);
			}
			catch(InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	protected StatusLedDistribution morphTo(StatusLedDistribution current, StatusLedDistribution target) {
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
		setPixelRange(0, dist.getGreen(), PixelColour.GREEN);
		setPixelRange(dist.getGreen(), dist.getYellow(), PixelColour.YELLOW);
		setPixelRange(dist.getGreen() + dist.getYellow(), dist.getRed(), PixelColour.RED);

		driver.render();
	}

	protected void setPixelRange(int startIndex, int count, int colour) {
		for(int index = startIndex; index < startIndex + count; index++) {
			driver.setPixelColour(index, colour);
		}
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
