package net.bluephod.henkinson.jenkins;

import java.util.Random;

/**
 * A dummy Jenkins implementation that returns random values or a specific status for testing.
 *
 * If you consider using this for any reason, you should also consider simply running a Jenkins instance using Docker. It's really easy
 * to do, and it is explained here:
 * <a href="https://wiki.jenkins.io/display/JENKINS/Installing+Jenkins+with+Docker">Installing Jenkins with Docker</a>
 */
public class DummyJenkins implements Jenkins {
	private JenkinsStatus dummyStatus;
	private Random random = new Random();

	/**
	 * Creates an instance that will return random status values.
	 */
	public DummyJenkins() {
		this(null);
	}

	/**
	 * Creates an instance that always returns the specified status object.
	 * @param dummyStatus The status that should always be returned.
	 */
	public DummyJenkins(final JenkinsStatus dummyStatus) {
		this.dummyStatus = dummyStatus;
	}

	@Override
	public JenkinsStatus retrieveStatus() {
		if(dummyStatus != null) {
			return dummyStatus;
		}
		else {
			return new JenkinsStatus(random.nextInt(20), random.nextInt(20), random.nextInt(20));
		}
	}
}
