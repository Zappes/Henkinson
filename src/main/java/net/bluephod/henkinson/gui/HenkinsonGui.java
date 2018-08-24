package net.bluephod.henkinson.gui;

import java.io.Closeable;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import net.bluephod.henkinson.config.Configuration;
import net.bluephod.henkinson.jenkins.JenkinsBranchInfo;
import net.bluephod.henkinson.jenkins.JenkinsStatus;

public final class HenkinsonGui implements Closeable {
	private Configuration config;
	private Terminal terminal;
	private TextGraphics graphics;
	private TerminalDimensions dimensions;
	private JenkinsStatus currentJenkinsStatus;
	private Date lastUpdateTime;

	public HenkinsonGui(Configuration config) throws IOException {
		this.config = config;

		if(config.isGuiEnabled()) {
			terminal = new DefaultTerminalFactory().createTerminal();
			graphics = terminal.newTextGraphics();
		}
	}

	public void init() throws IOException {
		if(!config.isGuiEnabled()) {
			return;
		}

		terminal.enterPrivateMode();
		terminal.clearScreen();
		terminal.setCursorVisible(false);

		dimensions = getTerminalDimensions();

		setColorSetting(ColorSetting.DEFAULT);
		drawTitleBar("Henkinson v1.0 - Monitoring Your Builds With Style.");
		drawStatusBar("Henkinson initialized.");

		terminal.flush();

		terminal.addResizeListener((terminal, newSize) -> {
			try {
				dimensions = getTerminalDimensions();
				update(null);
				terminal.flush();
			}
			catch(IOException e) {
				// Not much we can do here
				throw new IllegalStateException(e);
			}
		});
	}

	public void setStatus(String status) throws IOException {
		if(!config.isGuiEnabled()) {
			return;
		}

		drawStatusBar(status);
		terminal.flush();
	}

	public void update(JenkinsStatus status) throws IOException {
		if(!config.isGuiEnabled()) {
			return;
		}

		if(status != null) {
			currentJenkinsStatus = status;
			lastUpdateTime = new Date();
		}

		terminal.clearScreen();

		String updateTime = lastUpdateTime == null ? "n/a" : DateFormat.getDateTimeInstance().format(lastUpdateTime);

		drawTitleBar(("Henkinson v1.0 - Monitoring Your Builds With Style."));

		if(currentJenkinsStatus != null) {
			showProjects(currentJenkinsStatus.getBranchesWitchColor("yellow"),
									 0,
									 (dimensions.getColumns() / 2) - 1, ColorSetting.YELLOW);
			showProjects(currentJenkinsStatus.getBranchesWitchColor("red"),
									 (dimensions.getColumns() / 2) + 1,
									 (dimensions.getColumns() / 2) - 1,
									 ColorSetting.RED);

			drawStatusBar(String.format("Last update: %s, overall status (G/Y/R): %d/%d/%d", updateTime,
																	currentJenkinsStatus.getGreen(),
																	currentJenkinsStatus.getYellow(),
																	currentJenkinsStatus.getRed()));
		}
		else {
			drawStatusBar("No data loaded yet, please wait.");
		}

		terminal.flush();
	}

	public void close() throws IOException {
		if(!config.isGuiEnabled()) {
			return;
		}

		terminal.close();
	}

	private void showProjects(List<JenkinsBranchInfo> branches, int column, int width, ColorSetting colors) {
		int row = 2;

		for(JenkinsBranchInfo branch : branches) {
			putTextBlock(column, row++, width, branch.getProjectName(), colors);

			if(row > dimensions.getLines() - 2) {
				break;
			}
		}
	}

	private TerminalDimensions getTerminalDimensions() throws IOException {
		return new TerminalDimensions(terminal.getTerminalSize());
	}

	private void drawStatusBar(String text) {
		putTextBlock(0, dimensions.getLines() - 1, dimensions.getColumns(), text, ColorSetting.STATUS);
	}

	private void drawTitleBar(String text) {
		putTextBlock(0, 0, dimensions.getColumns(), text, ColorSetting.STATUS);
	}

	private void putTextBlock(int column, int row, int size, String text, ColorSetting colorSetting) {
		ColorSetting oldcolors = getColorSetting();
		setColorSetting(colorSetting);

		graphics.putString(column, row, getTextBlock(text, size));

		setColorSetting(oldcolors);
	}

	private String getTextBlock(String text, int size) {
		return text.length() > size ? text.substring(0, size - 1) : String.format("%1$-" + size + "s", text);
	}

	private ColorSetting getColorSetting() {
		return new ColorSetting(graphics.getForegroundColor(), graphics.getBackgroundColor());
	}

	private void setColorSetting(ColorSetting setting) {
		graphics.setForegroundColor(setting.getFore());
		graphics.setBackgroundColor(setting.getBack());
	}
}
