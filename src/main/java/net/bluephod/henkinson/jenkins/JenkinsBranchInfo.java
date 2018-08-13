package net.bluephod.henkinson.jenkins;

public class JenkinsBranchInfo {
	private String projectName;
	private String branchName;
	private String color;

	public JenkinsBranchInfo(final String projectName, final String branchName, final String color) {
		this.projectName = projectName;
		this.branchName = branchName;
		this.color = color;
	}

	public String getProjectName() {
		return projectName;
	}

	public String getBranchName() {
		return branchName;
	}

	public String getColor() {
		return color;
	}

	@Override
	public String toString() {
		return "JenkinsBranchInfo{" +
				"projectName='" + projectName + '\'' +
				", branchName='" + branchName + '\'' +
				", color='" + color + '\'' +
				'}';
	}
}
