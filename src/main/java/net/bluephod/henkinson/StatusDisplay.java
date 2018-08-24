package net.bluephod.henkinson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import net.bluephod.henkinson.config.Configuration;
import net.bluephod.henkinson.jenkins.JenkinsBranchInfo;
import net.bluephod.henkinson.jenkins.JenkinsStatus;
import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;

@Deprecated
public class StatusDisplay {
	private static Configuration config;
	private static FileTime lastStatusUpdate;
	private static boolean exited;

	public static void main(String[] args) throws IOException, InterruptedException {
		config = Configuration.getInstance();

		Configurator.currentConfig()
				.level(Level.valueOf(config.getLoglevel()))
				.activate();

		String statusFileName = "current_status.json";
		if(statusFileName == null || "".equals(statusFileName)) {
			Logger.error("No status file specified in the configuration, exiting.");
			return;
		}

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			Logger.info("Shutdown hook executing.");
			exited = true;
		}));

		try(Terminal terminal = new DefaultTerminalFactory().createTerminal()) {
			terminal.enterPrivateMode();
			terminal.clearScreen();
			terminal.setCursorVisible(false);
			TextGraphics textGraphics = terminal.newTextGraphics();
			textGraphics.setForegroundColor(TextColor.ANSI.WHITE);
			textGraphics.setBackgroundColor(TextColor.ANSI.BLACK);

			while(!exited) {
				Path statusFile = Paths.get(statusFileName);

				showUpdatedStatus(terminal, textGraphics, statusFile);

				Thread.sleep(1000);
			}

			terminal.exitPrivateMode();
		}
	}

	private static void showUpdatedStatus(final Terminal terminal, final TextGraphics textGraphics, final Path statusFile)
			throws IOException {
		if(Files.exists(statusFile)) {
			FileTime statusUpdated = Files.getLastModifiedTime(statusFile);

			if(lastStatusUpdate == null || statusUpdated.compareTo(lastStatusUpdate) > 0) {
				lastStatusUpdate = statusUpdated;

				drawTitleBar(textGraphics);

				JenkinsStatus status = new ObjectMapper().readValue(statusFile.toFile(), JenkinsStatus.class);
				List<JenkinsBranchInfo> yellowBranches = new LinkedList<>();
				List<JenkinsBranchInfo> redBranches = new LinkedList<>();

				for(JenkinsBranchInfo info : status.getBranchInfos()) {
					switch(info.getColor()) {
						case "yellow":
							yellowBranches.add(info);
							break;
						case "red":
							redBranches.add(info);
							break;
					}
				}

				printProjects(textGraphics, yellowBranches, 4, 4, TextColor.ANSI.BLACK, TextColor.ANSI.YELLOW);
				printProjects(textGraphics, redBranches, 41, 4, TextColor.ANSI.WHITE, TextColor.ANSI.RED);

				terminal.flush();
			}
		}
	}

	private static void drawTitleBar(final TextGraphics textGraphics) {
		textGraphics.setForegroundColor(TextColor.ANSI.BLACK);
		textGraphics.setBackgroundColor(TextColor.ANSI.WHITE);
		textGraphics.putString(4, 2, String.format(" %1$-71s", String.format("Last update: %s", lastStatusUpdate.toString())));
		textGraphics.setForegroundColor(TextColor.ANSI.WHITE);
		textGraphics.setBackgroundColor(TextColor.ANSI.BLACK);
	}

	private static void printProjects(
			TextGraphics textGraphics,
			List<JenkinsBranchInfo> branchInfos,
			int column,
			int row,
			TextColor foregroundColor,
			TextColor backgroundColor) {

		TextColor oldForeground = textGraphics.getForegroundColor();
		TextColor oldBackground = textGraphics.getBackgroundColor();

		textGraphics.setForegroundColor(foregroundColor);
		textGraphics.setBackgroundColor(backgroundColor);

		for(JenkinsBranchInfo info : branchInfos) {
			String projectName = info.getProjectName();
			String project35 = projectName.length() > 33 ? projectName.substring(0, 32) : String.format(" %1$-33s ", projectName);

			textGraphics.putString(column, row++, project35);
		}

		textGraphics.setForegroundColor(oldForeground);
		textGraphics.setBackgroundColor(oldBackground);
	}
}
