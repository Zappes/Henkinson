package net.bluephod.henkinson;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import net.bluephod.henkinson.config.Configuration;
import net.bluephod.henkinson.jenkins.Jenkins;
import net.bluephod.henkinson.jenkins.JenkinsBranchInfo;
import net.bluephod.henkinson.jenkins.JenkinsStatus;
import net.bluephod.henkinson.jenkins.RemoteJenkins;
import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Level;

/**
 * Test class to play with Jenkins without actually driving any LEDs.
 * <p>
 * This one is quite handy for working on the API code as no Raspberry Pi is needed to execute this code.
 */
public class JenkinsReader {
	public static void main(String[] args) throws IOException {
		Configuration config = Configuration.getInstance();

		Configurator.currentConfig()
				.level(Level.valueOf(config.getLoglevel()))
				.activate();

		Jenkins jenkins = new RemoteJenkins(config.getJenkinsBaseUrl(), config.getUsername(), config.getPassword());
		JenkinsStatus status = jenkins.retrieveStatus();
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
}
