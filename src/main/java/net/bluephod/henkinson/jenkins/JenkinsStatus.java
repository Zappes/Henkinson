package net.bluephod.henkinson.jenkins;

public class JenkinsStatus {
	private int green;
	private int yellow;
	private int red;

	public JenkinsStatus(final int green, final int yellow, final int red) {
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
}
