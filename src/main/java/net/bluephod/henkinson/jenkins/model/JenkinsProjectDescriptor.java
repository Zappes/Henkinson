package net.bluephod.henkinson.jenkins.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class JenkinsProjectDescriptor extends AbstractJenkinsObject {
	@JsonProperty
	private String name;
	@JsonProperty
	private String url;

	public String getName() {
		return name;
	}

	public String getUrl() {
		return url;
	}

	public String getApiUrl() {
		return getApiUrl(url);
	}
}
