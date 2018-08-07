package net.bluephod.henkinson.jenkins.model;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * A simple abstract base class for the Jenkins API objects.
 *
 * This is the base class for all the Jenkins model classes. It doesn't do too much, so I guess you don't need excessive documentation here.
 */
public abstract class AbstractJenkinsObject {
	/**
	 * The suffix that must be added to a view URL in order to get an API URL.
	 */
	protected static final String API_URL_SUFFIX = "api/json";

	/**
	 * Converts a Jenkins page URL into the corresponding API URL.
	 *
	 * For reasons I don't quite understand, the API doesn't return API URLs. It returns the web URLs for the objects it references, which
	 * might be nice for a human user, but useless for a REST client. Luckily, the conversion to an API URL is quite trivial - you just
	 * have to append the string "api/json" to the view URL. This is exactly what this method does.
	 *
	 * @param jenkinsUrl The Jenkins view URL of some resource.
	 * @return The Jenkins API URL for the view resource.
	 */
	protected String getApiUrl(String jenkinsUrl) {
		return jenkinsUrl + API_URL_SUFFIX;
	}

	/**
	 * Performs URL decoding of strings.
	 *
	 * The project names are returned by Jenkins as URL encoded strings for no apparent reason. We want readable names, though, so the
	 * getters for all URL encoded attributes of the API objects can and should use this method to decode them.
	 *
	 * If the argument is <code>null</code>, an empty string will be returned, thus making the world a little more null-safe.
	 *
	 * @param encoded A URL encoded string.
	 * @return
	 */
	protected String urlDecode(String encoded) {
		if(encoded == null) {
			return "";
		}

		try {
			return URLDecoder.decode(encoded, StandardCharsets.UTF_8.name());
		}
		catch(UnsupportedEncodingException e) {
			throw new IllegalStateException("Wow, your JDK doesn't support UTF-8. You're fucked.", e);
		}
	}
}
