package net.bluephod.henkinson;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.bluephod.henkinson.jenkins.Jenkins;
import net.bluephod.henkinson.jenkins.JenkinsStatus;
import net.bluephod.henkinson.jenkins.RemoteJenkins;

public class JenkinsReader {
	public static void main(String[] args) throws IOException {
		String jenkinsBaseUrl = "http://localhost:9001/api/json";
		// String jenkinsBaseUrl = "https://jenkins.frp:8443/api/json";
		String username = "admin";
		String password = "admin";

		Jenkins jenkins = new RemoteJenkins(jenkinsBaseUrl, username, password);
		JenkinsStatus status = jenkins.retrieveStatus();
		System.out.println("\n\n===> Overall Jenkins status: " + status);
	}
}
