package net.bluephod.henkinson;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import net.bluephod.henkinson.config.Configuration;
import net.bluephod.henkinson.jenkins.Jenkins;
import net.bluephod.henkinson.jenkins.JenkinsBranchInfo;
import net.bluephod.henkinson.jenkins.JenkinsStatus;
import net.bluephod.henkinson.jenkins.RemoteJenkins;
import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;

/**
 * Test class to play with Jenkins without actually driving any LEDs.
 * <p>
 * This one is quite handy for working on the API code as no Raspberry Pi is needed to execute this code.
 */
public class JenkinsReader {
	private static Configuration config;

	public static void main(String[] args) throws IOException {
		config = Configuration.getInstance();

		Configurator.currentConfig()
				.level(Level.valueOf(config.getLoglevel()))
				.activate();

		Jenkins jenkins = new RemoteJenkins(config.getJenkinsBaseUrl(), config.getUsername(), config.getPassword());
		JenkinsStatus status = persistStatus(jenkins.retrieveStatus());

		System.out.println("\n\n===> Overall Jenkins status: " + status);
		System.out.println();
		System.out.println("     Problematic branches:");
		List<JenkinsBranchInfo> problematicBranches = status.getBranchInfos()
				.stream()
				.filter(
						jenkinsBranchInfo -> !jenkinsBranchInfo
								.getColor()
								.startsWith("blue"))
				.sorted(Comparator.comparing(JenkinsBranchInfo::getColor))
				.collect(Collectors.toList());
		for(JenkinsBranchInfo info : problematicBranches) {
			System.out.printf("     [%s] %s (%s)%n", info.getColor(), info.getProjectName(), info.getBranchName());
		}
	}

	/**
	 * Persists the status and returns the status object.
	 * <p>
	 * This could obviously be void, but it makes the code a bit nicer when you can just wrap the method call around the retrieval method...
	 *
	 * @param status The status to persist.
	 *
	 * @return The status, unchanged.
	 */
	private static JenkinsStatus persistStatus(JenkinsStatus status) {
		String statusFileName = config.getStatusFile();
		if(statusFileName == null || "".equals(statusFileName)) {
			return status;
		}

		Path statusFile = Paths.get(statusFileName);

		try(BufferedWriter writer = Files.newBufferedWriter(statusFile)) {
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
			objectMapper.writeValue(writer, status);
		}
		catch(IOException e) {
			Logger.error(e, "Error when persisting status file.");
		}

		return status;
	}
}
