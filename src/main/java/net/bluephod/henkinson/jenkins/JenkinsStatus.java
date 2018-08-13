package net.bluephod.henkinson.jenkins;

import java.util.List;

/**
 * Represents the overall build status of a Jenkins server.
 *
 * Yes, Jenkins 2 has blue balls, not green ones. I think that's utterly stupid, though, so I call the good builds green. Sue me.
 */
public interface JenkinsStatus {
	/**
	 * The number of master branches on the server that had successful builds.
	 *
	 * @return The number of successful projects.
	 */
	int getGreen();

	/**
	 * The number of master branches on the server that had unstable builds.
	 *
	 * Being unstable normally means that the project's tests have failed, but everything else was fine.
	 *
	 * @return The number of unstable projects.
	 */
	int getYellow();

	/**
	 * The number of master branches on the server that had failed builds.
	 *
	 * @return The number of failed projects.
	 */
	int getRed();

	/**
	 * The total number of projects that were analyzed and included zn this result.
	 *
	 * @return The total number ofg projects.
	 */
	int getTotal();

	List<JenkinsBranchInfo> getBranchInfos();
}
