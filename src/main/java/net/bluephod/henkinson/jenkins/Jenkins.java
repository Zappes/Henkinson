package net.bluephod.henkinson.jenkins;

import java.io.IOException;

public interface Jenkins {
	JenkinsStatus retrieveStatus() throws IOException;
}
