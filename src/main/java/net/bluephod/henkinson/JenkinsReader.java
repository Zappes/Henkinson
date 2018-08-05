package net.bluephod.henkinson;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JenkinsReader {
	public static void main(String[] args) throws IOException {
		String jenkinsBaseUrl = "http://192.168.2.196:9001/api/json?pretty=true";

		URL url = new URL(jenkinsBaseUrl);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		connection.setReadTimeout(15 * 1000);
		String encoded = Base64.getEncoder().encodeToString(("admin:admin").getBytes(StandardCharsets.UTF_8));
		connection.setRequestProperty("Authorization", "Basic " + encoded);

		connection.connect();

		System.out.printf("Response code: %d%n", connection.getResponseCode());

		ObjectMapper mapper = new ObjectMapper();
		JsonNode root = mapper.readTree(connection.getInputStream());

		JsonNode jobs = root.path("jobs");
		for(JsonNode job : jobs) {
			System.out.println("Name:     " + job.path("name").asText());

			String jobUrl = job.path("url").asText();
			System.out.println("Job-URL:  " + jobUrl);
			String apiUrl = jobUrl + "api/json";
			System.out.println("API-URL:  " + apiUrl);

			connection = (HttpURLConnection) new URL(apiUrl).openConnection();
			connection.setRequestMethod("GET");
			connection.setReadTimeout(15 * 1000);
			encoded = Base64.getEncoder().encodeToString(("admin:admin").getBytes(StandardCharsets.UTF_8));
			connection.setRequestProperty("Authorization", "Basic " + encoded);
			connection.connect();

			JsonNode branches = mapper.readTree(connection.getInputStream()).path("jobs");

			for(JsonNode branch : branches) {
				System.out.println("  Name:  " + branch.path("name"));
				System.out.println("  Color: " + branch.path("color"));
			}
		}
	}
}
