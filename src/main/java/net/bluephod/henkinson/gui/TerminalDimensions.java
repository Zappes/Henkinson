package net.bluephod.henkinson.gui;

import com.googlecode.lanterna.TerminalSize;

class TerminalDimensions {
	private int columns;
	private int lines;

	public TerminalDimensions(TerminalSize size) {
		this.columns = size.getColumns();
		this.lines = size.getRows();
	}

	public int getColumns() {
		return columns;
	}

	public int getLines() {
		return lines;
	}
}
