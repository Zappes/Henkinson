package net.bluephod.henkinson.jenkins;

import java.util.Random;

public class DummyJenkins implements Jenkins {
	private JenkinsStatus dummyStatus;
	private Random random = new Random();

	public DummyJenkins() {
		this(null);
	}

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
