package net.bluephod.henkinson.jenkins.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class JenkinsProject extends AbstractJenkinsObject {
	@JsonProperty("jobs")
	private List<JenkinsBranchDescriptor> branches;

	public List<JenkinsBranchDescriptor> getBranches() {
		return branches;
	}
}
