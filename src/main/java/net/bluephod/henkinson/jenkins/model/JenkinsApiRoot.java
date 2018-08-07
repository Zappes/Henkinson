package net.bluephod.henkinson.jenkins.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.bluephod.henkinson.config.Configuration;

/**
 * Represents the root resource of the Jenkins API.
 *
 * The API root can be retrieved at <code>http://yourjenkins.yourdomain/api/json</code>, which is exactly the URL you have to specify in
 * the configuration option {@link Configuration#getJenkinsBaseUrl()}. The ressource contains some information about the Jenkins server
 * itself, which doesn't concern us, and a list of projects, which we actually need.
 *
 * As it is customary for a REST API, the project information included in that list doesn't contain all the data about the project, just
 * the bare minimum required to retrieve the actual data. I call that kind of object a "descriptor", so the objects in the list here are
 * instances of the class {@link JenkinsProjectDescriptor}, not instances of {@link JenkinsProject}.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class JenkinsApiRoot extends AbstractJenkinsObject {
	@JsonProperty("jobs")
	private List<JenkinsProjectDescriptor> projects;

	public List<JenkinsProjectDescriptor> getProjects() {
		return projects;
	}
}
