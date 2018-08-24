package net.bluephod.henkinson.gui;

import com.googlecode.lanterna.TextColor;

class ColorSetting {
	public static final ColorSetting DEFAULT = new ColorSetting(TextColor.ANSI.WHITE, TextColor.ANSI.BLACK);
	public static final ColorSetting STATUS = new ColorSetting(TextColor.ANSI.BLACK, TextColor.ANSI.WHITE);
	public static final ColorSetting RED = new ColorSetting(TextColor.ANSI.WHITE, TextColor.ANSI.RED);
	public static final ColorSetting YELLOW = new ColorSetting(TextColor.ANSI.WHITE, TextColor.ANSI.YELLOW);

	private TextColor fore;
	private TextColor back;

	public ColorSetting(final TextColor fore, final TextColor back) {
		this.fore = fore;
		this.back = back;
	}

	public TextColor getFore() {
		return fore;
	}

	public TextColor getBack() {
		return back;
	}
}
