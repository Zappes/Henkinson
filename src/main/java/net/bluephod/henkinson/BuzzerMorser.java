package net.bluephod.henkinson;

import com.diozero.api.DigitalOutputDevice;
import net.bluephod.henkinson.config.Configuration;

public class BuzzerMorser {
	// First 10 elements of Array are the digits. The rest are the letters.
	private final String[] morseSymbols = new String[] {"-----", ".----", "..---", "...--", "....-",
		".....", "-....", "--...", "---..", "----.",
		".-", "-...", "-.-.", "-..", ".", "..-.",
		"--.", "....", "..", ".---", "-.-", ".-..",
		"--", "-.", "---", ".--.", "--.-", ".-.",
		"...", "-", "..-", "...-", ".--", "-..-",
		"-.--", "--.."};
	private Configuration config;

	public BuzzerMorser(final Configuration config) {
		this.config = config;
	}

	public void buzzMorse(String word) {
		try(DigitalOutputDevice buzzer = new DigitalOutputDevice(config.getBuzzerGpio())) {
			for(char ch : word.toUpperCase().toCharArray()) {
				if(Character.isDigit(ch)) {
					buzzSymbol(buzzer, morseSymbols[((int) ch) - 48]);
				}
				else if(Character.isLetter(ch)) {
					buzzSymbol(buzzer, morseSymbols[((int) ch) - 55]);
				}
				else {
					buzzPause();
				}
			}
		}
	}

	private void buzzSymbol(DigitalOutputDevice buzzer, String morseSymbol) {
		for(char ch : morseSymbol.toCharArray()) {
			if(ch == '.') {
				buzzDit(buzzer);
			}
			else {
				buzzDah(buzzer);
			}
		}

		buzzPause();
	}

	private void buzzPause() {
		HenkinsonUtil.sleep(config.getBuzzerDitDuration());
	}

	private void buzzDit(final DigitalOutputDevice buzzer) {
		buzzer.on();
		HenkinsonUtil.sleep(config.getBuzzerDitDuration());
		buzzer.off();
	}

	private void buzzDah(final DigitalOutputDevice buzzer) {
		buzzer.on();
		HenkinsonUtil.sleep(config.getBuzzerDitDuration() * 3);
		buzzer.off();
	}
}
