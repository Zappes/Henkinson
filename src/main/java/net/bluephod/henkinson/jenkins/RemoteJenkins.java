package net.bluephod.henkinson.jenkins;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.bluephod.henkinson.config.Configuration;
import net.bluephod.henkinson.jenkins.model.JenkinsApiRoot;
import net.bluephod.henkinson.jenkins.model.JenkinsBranchDescriptor;
import net.bluephod.henkinson.jenkins.model.JenkinsProject;
import net.bluephod.henkinson.jenkins.model.JenkinsProjectDescriptor;
import org.pmw.tinylog.Logger;

/**
 * A Jenkins driver that connects to an actual Jenkins server.
 * <p>
 * Please note that this was only tested with Jenkins 2.130 and multibranch projects. I have no clue if it will work on earlier or later
 * versions and with other types of projects.
 */
public class RemoteJenkins implements Jenkins {
	private String jenkinsBaseUrl;
	private String username;
	private String password;

	public RemoteJenkins(final String jenkinsBaseUrl, final String username, final String password) {
		this.jenkinsBaseUrl = jenkinsBaseUrl;
		this.username = username;
		this.password = password;
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

			if(project.getBranches() == null) {
				Logger.debug(String.format("Branches collection for project %s is null, skipping.", project.getName()));
				continue;
			}

			Logger.debug(String.format("Found %d branches", project.getBranches().size()));
			boolean includeFeatureBranches = Configuration.getInstance().isIncludeFeatureBranches();

			for(JenkinsBranchDescriptor branchDescriptor : project.getBranches()) {
				if(includeFeatureBranches || branchDescriptor.isMaster()) {
					Logger.debug(String.format("Branch '%s' is %s", branchDescriptor.getName(), branchDescriptor.getColor()));

					switch(branchDescriptor.getColor()) {
						case "blue":
						case "blue_anime":
							green++;
							break;
						case "yellow":
						case "yellow_anime":
							yellow++;
							break;
						case "red":
						case "red_anime":
							red++;
						default:
							// simply ignore the grey and disabled ones
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
