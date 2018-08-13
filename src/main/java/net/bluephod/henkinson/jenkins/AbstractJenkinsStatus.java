package net.bluephod.henkinson.jenkins;

public abstract class AbstractJenkinsStatus implements JenkinsStatus {
	protected int red;
	protected int yellow;
	protected int green;

	@Override
	public int getRed() {
		return red;
	}
	@Override
	public int getYellow() {
		return yellow;
	}
	@Override
	public int getGreen() {
		return green;
	}

	@Override
	public int getTotal() {
		return red+yellow+green;
	}

	@Override
	public String toString() {
		return "JenkinsStatus{" +
				"red=" + red +
				", yellow=" + yellow +
				", green=" + green +
				'}';
	}
}
