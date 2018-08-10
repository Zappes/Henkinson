package net.bluephod.henkinson.jenkins;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

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
 * Please note that this was only tested with Jenkins 2.130. I have no clue if it will work on earlier or later
 * versions.
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

		ColorHolder colors = new ColorHolder();

		for(JenkinsProjectDescriptor projectDescriptor : root.getProjects()) {
			processProject(projectDescriptor, mapper, colors);
		}

		return new JenkinsStatus(colors.greenCount, colors.yellowCount, colors.redCount);
	}

	private void processProject(final JenkinsProjectDescriptor projectDescriptor, final ObjectMapper mapper,
			final ColorHolder colors) throws IOException {

		HttpURLConnection connection;
		connection = getConnection(projectDescriptor.getApiUrl(), "GET");
		connection.connect();

		String projectColor = projectDescriptor.getColor();
		if(projectColor != null) {
			// a color is only set for single-branch projects. if there is one, we can skip all the branch stuff and simply count the project
			// color as if it was a master branch.
			Logger.debug(String.format("Single-branch project '%s' is %s", projectDescriptor.getName(), projectColor));
			updateColors(projectColor, colors);

			return;
		}

		JenkinsProject project = mapper.readValue(connection.getInputStream(), JenkinsProject.class);
		String projectName = project.getName();
		Logger.debug(String.format("Checking branches for multi-branch project '%s'", projectName));

		List<JenkinsBranchDescriptor> branches = project.getBranches();

		processBranches(branches, projectName, colors);
	}

	private void processBranches(final List<JenkinsBranchDescriptor> branches, final String projectName,
			final ColorHolder colors) throws IOException {
		if(branches == null) {
			Logger.debug(String.format("Branches collection for project %s is null, skipping.", projectName));
			return;
		}

		Logger.debug(String.format("Found %d branches", branches.size()));
		boolean includeFeatureBranches = Configuration.getInstance().isIncludeFeatureBranches();

		for(JenkinsBranchDescriptor branchDescriptor : branches) {
			if(includeFeatureBranches || branchDescriptor.isMaster()) {
				String branchColor = branchDescriptor.getColor();

				Logger.debug(String.format("Branch '%s' is %s", branchDescriptor.getName(), branchColor));
				updateColors(branchColor, colors);
			}
		}
	}

	private void updateColors(final String color, final ColorHolder colors) {
		switch(color) {
			case "blue":
			case "blue_anime":
				colors.greenCount++;
				break;
			case "yellow":
			case "yellow_anime":
				colors.yellowCount++;
				break;
			case "red":
			case "red_anime":
				colors.redCount++;
			default:
				// simply ignore the grey and disabled ones
		}
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

	private static class ColorHolder {
		public int redCount;
		public int yellowCount;
		public int greenCount;
	}
}
