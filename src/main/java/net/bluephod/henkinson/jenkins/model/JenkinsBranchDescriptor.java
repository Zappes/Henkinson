package net.bluephod.henkinson.jenkins.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class JenkinsBranchDescriptor extends AbstractJenkinsObject {
	@JsonProperty
	private String name;
	@JsonProperty
	private String color;

	public String getColor() {
		return color;
	}

	public String getName() {
		return name;
	}

	@JsonIgnore
	public boolean isMaster() {
		return "master".equalsIgnoreCase(name);
	}
}
