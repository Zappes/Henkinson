package net.bluephod.henkinson.jenkins.model;

public abstract class AbstractJenkinsObject {
	protected String getApiUrl(String jenkinsUrl) {
		return jenkinsUrl + "api/json";
	}
}
