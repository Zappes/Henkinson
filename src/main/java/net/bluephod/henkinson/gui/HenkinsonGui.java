package net.bluephod.henkinson.gui;

import java.io.Closeable;
import java.io.IOException;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import net.bluephod.henkinson.config.Configuration;
import net.bluephod.henkinson.jenkins.JenkinsStatus;

public class HenkinsonGui implements Closeable {
	private Configuration config;
	private Terminal terminal;
	private TextGraphics graphics;

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

		graphics.setForegroundColor(TextColor.ANSI.WHITE);
		graphics.setBackgroundColor(TextColor.ANSI.BLACK);
	}

	public void update(JenkinsStatus status) {
		if(!config.isGuiEnabled()) {
			return;
		}

	}

	public void close() throws IOException {
		if(!config.isGuiEnabled()) {
			return;
		}

		terminal.exitPrivateMode();
		terminal.close();
	}
}
