package net.bluephod.henkinson.jenkins.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class JenkinsApiRoot extends AbstractJenkinsObject {
	@JsonProperty("jobs")
	private List<JenkinsProjectDescriptor> projects;

	public List<JenkinsProjectDescriptor> getProjects() {
		return projects;
	}
}
