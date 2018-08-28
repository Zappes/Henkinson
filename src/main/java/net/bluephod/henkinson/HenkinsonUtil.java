package net.bluephod.henkinson;

import org.pmw.tinylog.Logger;

public final class HenkinsonUtil {
	private HenkinsonUtil() {
	}

	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		}
		catch(InterruptedException e) {
			Logger.warn(e, "Interrupted while sleeping.");
			Thread.currentThread().interrupt();
		}
	}
}
