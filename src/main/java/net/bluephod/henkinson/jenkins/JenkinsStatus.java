package net.bluephod.henkinson.jenkins;

/**
 * Represents the overall build status of a Jenkins server.
 *
 * Yes, Jenkins 2 has blue balls, not green ones. I think that's utterly stupid, though, so I call the good builds green. Sue me.
 */
public class JenkinsStatus {
	private int green;
	private int yellow;
	private int red;

	public JenkinsStatus(final int green, final int yellow, final int red) {
		this.green = green;
		this.yellow = yellow;
		this.red = red;
	}

	/**
	 * The number of master branches on the server that had successful builds.
	 *
	 * @return The number of successful projects.
	 */
	public int getGreen() {
		return green;
	}

	/**
	 * The number of master branches on the server that had unstable builds.
	 *
	 * Being unstable normally means that the project's tests have failed, but everything else was fine.
	 *
	 * @return The number of unstable projects.
	 */
	public int getYellow() {
		return yellow;
	}

	/**
	 * The number of master branches on the server that had failed builds.
	 *
	 * @return The number of failed projects.
	 */
	public int getRed() {
		return red;
	}

	/**
	 * The total number of projects that were analyzed and included zn this result.
	 *
	 * @return The total number ofg projects.
	 */
	public int getTotal() {
		return red + green + yellow;
	}

	@Override
	public String toString() {
		return "JenkinsStatus{" +
				"green=" + green +
				", yellow=" + yellow +
				", red=" + red +
				'}';
	}
}
