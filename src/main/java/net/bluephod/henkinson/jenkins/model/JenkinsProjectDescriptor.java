package net.bluephod.henkinson.jenkins.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The descriptor of a project as it is contained in the root resource of the API.
 * <p>
 * In Jenkins wording, everything is a job. Using this term, it's quite difficult to distinguish between the actual project and the
 * branches inside that project - and this is why I call this thing a "project" and the things contained in it a "branch". I hope this
 * doesn't trouble you too much, but if it does, you are allowed to make love to yourself.
 * <p>
 * Instances of this class can be retrieved from the {@link JenkinsApiRoot} instance deserialized from the root resource. Their main
 * purpose is providing the actual URL from which the project information can be retrieved. You'll want to use {@link #getApiUrl()} for
 * that as {@link #getUrl()} returns the view URL, not the API URL.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class JenkinsProjectDescriptor extends AbstractJenkinsObject {
	@JsonProperty
	private String name;
	@JsonProperty
	private String url;

	/**
	 * The name of the project.
	 *
	 * @return The name of the project.
	 */
	public String getName() {
		return urlDecode(name);
	}

	/**
	 * The view URL of the project.
	 * <p>
	 * with this URL, you can access the actual web page for the project in Jenkins. How nice. Utterly useless in our context, though.
	 *
	 * @return The view URL of the project.
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * The API URL of the project.
	 *
	 * This is the URL from which you can get the detailed JSON data for the project, which is exactly what we need. The class you can
	 * deserialize from that resource is {@link JenkinsProject}.
	 *
	 * @return The API URL of the project.
	 */
	public String getApiUrl() {
		return getApiUrl(url);
	}
}
