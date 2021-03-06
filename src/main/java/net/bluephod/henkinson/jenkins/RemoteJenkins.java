package net.bluephod.henkinson.jenkins;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.bluephod.henkinson.HenkinsonUtil;
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
	private Configuration config;

	public RemoteJenkins(Configuration config) {
		this.config = config;
	}

	@Override
	public JenkinsStatus retrieveStatus() throws IOException {
		String jenkinsBaseUrl = config.getJenkinsBaseUrl();

		Logger.debug(String.format("Retrieving stats from %s", jenkinsBaseUrl));

		JenkinsApiRoot root = null;
		ObjectMapper mapper = new ObjectMapper();

		try(InputStream in = getStreamForUrl(jenkinsBaseUrl)) {
			root = mapper.readValue(in, JenkinsApiRoot.class);
		}
		catch(IOException e) {
			System.exit(-1);
		}

		Logger.debug(String.format("Found %d projects", root.getProjects().size()));

		JenkinsStatus colors = new JenkinsStatus();

		for(JenkinsProjectDescriptor projectDescriptor : root.getProjects()) {
			processProject(projectDescriptor, mapper, colors);
		}

		Logger.info("Successfully retrieved Jenkins status.");

		return colors;
	}

	private void processProject(final JenkinsProjectDescriptor projectDescriptor, final ObjectMapper mapper,
			final JenkinsStatus colors) throws IOException {

		String projectColor = projectDescriptor.getColor();
		if(projectColor != null) {
			// a color is only set for single-branch projects. if there is one, we can skip all the branch stuff and simply count the project
			// color as if it was a master branch.
			String projectName = projectDescriptor.getName();
			Logger.debug(String.format("Single-branch project '%s' is %s", projectName, projectColor));
			colors.updateStats(projectName, projectColor);

			return;
		}

		JenkinsProject project = null;

		try(InputStream in = getStreamForUrl(projectDescriptor.getApiUrl())) {
			project = mapper.readValue(in, JenkinsProject.class);
		}
		catch(IOException e) {
			System.exit(-1);
		}

		String projectName = project.getName();
		Logger.debug(String.format("Checking branches for multi-branch project '%s'", projectName));

		List<JenkinsBranchDescriptor> branches = project.getBranches();

		processBranches(branches, projectName, colors);
	}

	private void processBranches(final List<JenkinsBranchDescriptor> branches, final String projectName,
			final JenkinsStatus colors) throws IOException {
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

	private void authenticateConnection(URLConnection connection) {
		String encoded =
				Base64.getEncoder().encodeToString((config.getUsername() + ":" + config.getPassword()).getBytes(StandardCharsets.UTF_8));
		connection.setRequestProperty("Authorization", "Basic " + encoded);
	}

	private InputStream getStreamForUrl(final String urlString) throws IOException {
		int retries = 0;
		int connectionRetryDelay = config.getConnectionRetryDelay();

		while(retries < config.getConnectionRetries()) {
			try {
				URL url = new URL(urlString);
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();

				connection.setRequestMethod("GET");
				connection.setReadTimeout(15 * 1000);
				authenticateConnection(connection);
				connection.connect();

				return connection.getInputStream();
			}
			catch(IOException e) {
				retries++;
				Logger.info(
						String.format("Connection stream retrieval attempt %d failed, waiting %dms before retrying...", retries, connectionRetryDelay));
				HenkinsonUtil.sleep(connectionRetryDelay);
			}
		}

		Logger.error("Exceeded maximum number of connection retries, failing.");
		throw new IOException("Connection retries exceeded.");
	}
}
