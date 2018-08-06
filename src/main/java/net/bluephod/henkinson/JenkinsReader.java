package net.bluephod.henkinson;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.bluephod.henkinson.config.Configuration;
import net.bluephod.henkinson.jenkins.Jenkins;
import net.bluephod.henkinson.jenkins.JenkinsStatus;
import net.bluephod.henkinson.jenkins.RemoteJenkins;
import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Level;

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
