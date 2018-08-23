package net.bluephod.henkinson.jenkins;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public final class JenkinsStatus {
	private int red;
	private int yellow;
	private int green;
	private List<JenkinsBranchInfo> branchInfos = new LinkedList<>();

	public int getRed() {
		return red;
	}

	public int getYellow() {
		return yellow;
	}

	public int getGreen() {
		return green;
	}

	public int getTotal() {
		return red + yellow + green;
	}

	public String toString() {
		return "JenkinsStatus{" +
				"red=" + red +
				", yellow=" + yellow +
				", green=" + green +
				'}';
	}

	public void updateStats(String projectName, String color) {
		updateStats(projectName, "", color);
	}

	public void updateStats(String projectName, String branchName, String color) {
		String simplifiedColor = "unknown";

		switch(color) {
			case "blue":
			case "blue_anime":
				green++;
				simplifiedColor = "green";
				break;
			case "yellow":
			case "yellow_anime":
				yellow++;
				simplifiedColor = "yellow";
				break;
			case "red":
			case "red_anime":
				red++;
				simplifiedColor = "red";
			default:
				// simply ignore the grey and disabled ones
		}

		branchInfos.add(new JenkinsBranchInfo(projectName, branchName, simplifiedColor));
	}

	public List<JenkinsBranchInfo> getBranchInfos() {
		return Collections.unmodifiableList(branchInfos);
	}
}
