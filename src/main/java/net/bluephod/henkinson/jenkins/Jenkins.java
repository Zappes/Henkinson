package net.bluephod.henkinson.jenkins;

import java.io.IOException;

/**
 * The interface for accessing a Jenkins server.
 *
 * Its arguable if an interface is actually needed here, but it was useful for testing as I could use a dummy implementation as long as I
 * hadn't written the code for accessing a real server. This might be useful for you if you just want to test the LED stuff without
 * having a Jenkins server available.
 */
public interface Jenkins {
	/**
	 * Connects to the Jenkins server and retrieves the overall build status.
	 *
	 * Note that only branches called "master" will be included in the stats. It doesn't seem sensible to include feature branches.
	 *
	 * @return The build status for the master branches that are currently present on the server.
	 * @throws IOException If something goes wrong, either in the http request or during the deserialization of API objects.
	 */
	JenkinsStatus retrieveStatus() throws IOException;
}
