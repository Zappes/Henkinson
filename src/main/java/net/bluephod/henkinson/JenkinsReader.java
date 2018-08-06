package net.bluephod.henkinson;

import java.io.IOException;

import net.bluephod.henkinson.config.Configuration;
import net.bluephod.henkinson.jenkins.Jenkins;
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
	}
}
