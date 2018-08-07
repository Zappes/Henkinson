package net.bluephod.henkinson.jenkins.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Details about a project in Jenkins.
 *
 * Instances of this class are created by deserializing the API URL of a project in the Jenkins server. The interesting part here is the
 * list of branches for the project.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class JenkinsProject extends AbstractJenkinsObject {
	/**
	 * The name of the project.
	 */
	@JsonProperty
	private String name;

	/**
	 * The list of branches contained in the project.
	 */
	@JsonProperty("jobs")
	private List<JenkinsBranchDescriptor> branches;

	/**
	 * Retrieve the project's branches.
	 *
	 * Just as in the projects list of the root resource, this method will only return a list of descriptors, not the entire data about the
	 * branch builds. This is no problem, though, as that descriptor already contains the information we're looking for - have a look at
	 * {@link JenkinsBranchDescriptor#getColor()}.
	 *
	 * @return A list of branch descriptors for the project.
	 */
	public List<JenkinsBranchDescriptor> getBranches() {
		return branches;
	}

	/**
	 * The name of the project.
	 *
	 * We don't actually need this. We mainly read it for logging and debugging purposes.
	 *
	 * @return The name of the project.
	 */
	public String getName() {
		return urlDecode(name);
	}
}
