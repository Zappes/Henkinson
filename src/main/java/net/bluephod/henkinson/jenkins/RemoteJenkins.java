package net.bluephod.henkinson.jenkins;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.bluephod.henkinson.jenkins.model.JenkinsApiRoot;
import net.bluephod.henkinson.jenkins.model.JenkinsBranchDescriptor;
import net.bluephod.henkinson.jenkins.model.JenkinsProject;
import net.bluephod.henkinson.jenkins.model.JenkinsProjectDescriptor;
import org.pmw.tinylog.Logger;

public class RemoteJenkins implements Jenkins {
	private String jenkinsBaseUrl;
	private String username;
	private String password;

	public RemoteJenkins(final String jenkinsBaseUrl, final String username, final String password) {
		this.jenkinsBaseUrl = jenkinsBaseUrl;
		this.username = username;
		this.password = password;

		Logger.info(String.format("Jenkins remote access to %s initialized.", jenkinsBaseUrl));
	}

	@Override
	public JenkinsStatus retrieveStatus() throws IOException {
		Logger.debug(String.format("Retrieving stats from %s", jenkinsBaseUrl));

		HttpURLConnection connection = getConnection(jenkinsBaseUrl, "GET");
		connection.connect();

		ObjectMapper mapper = new ObjectMapper();
		JenkinsApiRoot root = mapper.readValue(connection.getInputStream(), JenkinsApiRoot.class);

		Logger.debug(String.format("Found %d projects", root.getProjects().size()));

		int red = 0;
		int yellow = 0;
		int green = 0;

		for(JenkinsProjectDescriptor projectDescriptor : root.getProjects()) {
			connection = getConnection(projectDescriptor.getApiUrl(), "GET");
			connection.connect();

			JenkinsProject project = mapper.readValue(connection.getInputStream(), JenkinsProject.class);
			Logger.debug(String.format("Checking project %s", project.getName()));
			Logger.debug(String.format("Found %d branches", project.getBranches().size()));

			for(JenkinsBranchDescriptor branchDescriptor : project.getBranches()) {
				if(branchDescriptor.isMaster()) {
					Logger.debug(String.format("Master branch is %s", branchDescriptor.getColor()));

					switch(branchDescriptor.getColor()) {
						case "blue":
						case "blue_anime":
							green++;
							break;
						case "yellow":
						case "yellow_anime":
							yellow++;
							break;
						default:
							red++;
					}
				}
			}
		}

		return new JenkinsStatus(green, yellow, red);
	}

	private HttpURLConnection getConnection(final String urlString, final String method) throws IOException {
		URL url = new URL(urlString);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod(method);
		connection.setReadTimeout(15 * 1000);
		authenticateConnection(connection);

		return connection;
	}

	private void authenticateConnection(final URLConnection connection) {
		String encoded = Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
		connection.setRequestProperty("Authorization", "Basic " + encoded);
	}
}
