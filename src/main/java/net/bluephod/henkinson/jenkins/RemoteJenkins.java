package net.bluephod.henkinson.jenkins;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedList;
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

		Logger.debug("Connected");

		ObjectMapper mapper = new ObjectMapper();
		JenkinsApiRoot root = mapper.readValue(connection.getInputStream(), JenkinsApiRoot.class);

		Logger.debug(String.format("Found %d projects", root.getProjects().size()));

		JenkinsStatusImpl colors = new JenkinsStatusImpl();

		for(JenkinsProjectDescriptor projectDescriptor : root.getProjects()) {
			processProject(projectDescriptor, mapper, colors);
		}

		return colors;
	}

	private void processProject(final JenkinsProjectDescriptor projectDescriptor, final ObjectMapper mapper,
			final JenkinsStatusImpl colors) throws IOException {

		HttpURLConnection connection;
		connection = getConnection(projectDescriptor.getApiUrl(), "GET");
		connection.connect();

		String projectColor = projectDescriptor.getColor();
		if(projectColor != null) {
			// a color is only set for single-branch projects. if there is one, we can skip all the branch stuff and simply count the project
			// color as if it was a master branch.
			String projectName = projectDescriptor.getName();
			Logger.debug(String.format("Single-branch project '%s' is %s", projectName, projectColor));
			colors.updateStats(projectName, projectColor);

			return;
		}

		JenkinsProject project = mapper.readValue(connection.getInputStream(), JenkinsProject.class);
		String projectName = project.getName();
		Logger.debug(String.format("Checking branches for multi-branch project '%s'", projectName));

		List<JenkinsBranchDescriptor> branches = project.getBranches();

		processBranches(branches, projectName, colors);
	}

	private void processBranches(final List<JenkinsBranchDescriptor> branches, final String projectName,
			final JenkinsStatusImpl colors) throws IOException {
		if(branches == null) {
			Logger.debug(String.format("Branches collection for project %s is null, skipping.", projectName));
			return;
		}

		Logger.debug(String.format("Found %d branches", branches.size()));
		boolean includeFeatureBranches = Configuration.getInstance().isIncludeFeatureBranches();

		for(JenkinsBranchDescriptor branchDescriptor : branches) {
			if(includeFeatureBranches || branchDescriptor.isMaster()) {
				String branchColor = branchDescriptor.getColor();
				String branchName = branchDescriptor.getName();

				Logger.debug(String.format("Branch '%s' is %s", branchName, branchColor));
				colors.updateStats(projectName, branchName, branchColor);
			}
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

	private static class JenkinsStatusImpl extends AbstractJenkinsStatus {
		private List<JenkinsBranchInfo> branchInfos = new LinkedList<>();

		public void updateStats(String projectName, String color) {
			updateStats(projectName, "", color);
		}

		public void updateStats(String projectName, String branchName, String color) {
			branchInfos.add(new JenkinsBranchInfo(projectName, branchName, color));

			switch(color) {
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

		@Override
		public List<JenkinsBranchInfo> getBranchInfos() {
			return Collections.unmodifiableList(branchInfos);
		}
	}
}
