package net.bluephod.henkinson.jenkins.model;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.bluephod.henkinson.config.Configuration;

/**
 * A descriptor for a branch in a specific Jenkins project.
 *
 * Instances of this class can be obtained using the method {@link JenkinsProject#getBranches()}. They represent the overview variant of
 * the description for a given branch.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class JenkinsBranchDescriptor extends AbstractJenkinsObject {
	/**
	 * The name of the branch.
	 */
	@JsonProperty
	private String name;

	/**
	 * The build status of the branch.
	 */
	@JsonProperty
	private String color;

	/**
	 * The name of the branch.
	 *
	 * This is information we actually need. While we don't care too much about the name of the project, the name of the branch is
	 * important as we are only interested in the branches called "master".
	 *
	 * @return The name of the branch.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the build status of the branch.
	 *
	 * For some reason, the Jenkins people don't provide an actual status like "SUCCESS", "FAIL" or "TOTALLY_FUCKED" here. Instead of that
	 * you get a color code, which corresponds to the color of the ball (blue, yellow, red, grey) in the Jenkins UI. If you want to know
	 * what to expect here, have a look at <a href="https://javadoc.jenkins-ci.org/hudson/model/BallColor.html">this piece of JavaDoc</a>.
	 *
	 * @return The build status as a color name.
	 */
	public String getColor() {
		return color;
	}

	/**
	 * Returns true if this branch is called "master".
	 *
	 * The main reason for including this method was that it was easier to make the master branch name configurable like this.
	 *
	 * @return True if this branch is called "master".
	 */
	@JsonIgnore
	public boolean isMaster() {
		try {
			return Configuration.getInstance().getMasterBranchName().equalsIgnoreCase(name);
		}
		catch(IOException e) {
			throw new IllegalStateException("Configuration wasn't initialized properly.", e);
		}
	}
}
