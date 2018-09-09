package net.bluephod.henkinson;

import java.io.IOException;
import java.lang.management.ManagementFactory;

import com.diozero.devices.LED;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.bluephod.henkinson.config.Configuration;
import net.bluephod.henkinson.gui.HenkinsonGui;
import net.bluephod.henkinson.jenkins.Jenkins;
import net.bluephod.henkinson.jenkins.JenkinsStatus;
import net.bluephod.henkinson.jenkins.RemoteJenkins;
import net.bluephod.henkinson.visualization.VuMeterBuildStatusVisualization;
import org.pmw.tinylog.Logger;

public class Henkinson {
	private final Configuration config;
	private static boolean notInterrupted = true;

	public Henkinson() throws IOException {
		config = Configuration.getInstance();
		Logger.info("Using configuration: \n" + new ObjectMapper().writeValueAsString(config));
	}

	public static void main(String[] args) throws Exception {
		String processName = ManagementFactory.getRuntimeMXBean().getName();

		Logger.info(String.format("Started new Henkinson process %s", processName));

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			Logger.info("Shutdown requested by runtime.");
			notInterrupted = false;
		}));


		System.exit(new Henkinson().monitorJenkins());
	}

	private int monitorJenkins() throws IOException {
		if(!(config.isGuiEnabled() || config.isStripEnabled())) {
			Logger.warn("Neither strip nor GUI is enabled - exiting.");
			return 1;
		}

		HenkinsonGui gui = null;
		VuMeterBuildStatusVisualization visualization = null;
		LED led = null;
		BuzzerMorser morser = null;

		try {
			if(config.isBuzzerEnabled()) {
				morser = new BuzzerMorser(config);
				morser.buzzMorse("henkinson");
			}

			if(config.isLedEnabled()) {
				led = startBlinker();
			}

			if(config.isGuiEnabled()) {
				gui = startGui();
			}

			if(config.isStripEnabled()) {
				visualization = startVisualization();
			}

			startUpdateThread(gui, visualization, morser);

			if(gui != null) {
				gui.waitForKeypress();
				return 0;
			}
			else {
				while(notInterrupted) {
					HenkinsonUtil.sleep(1000);
				}
			}
		}
		catch(Exception e) {
			Logger.error(e, "Exception caught during monitoring");
			return 500;
		}
		finally {
			if(gui != null) {
				gui.close();
			}

			if(visualization != null) {
				visualization.close();
			}

			if(led != null) {
				led.close();
			}
		}

		return 0;

	}

	private HenkinsonGui startGui() throws IOException {
		HenkinsonGui gui = new HenkinsonGui(config, this);
		gui.init();

		Logger.info("GUI initialized");

		return gui;
	}

	private VuMeterBuildStatusVisualization startVisualization() {
		VuMeterBuildStatusVisualization visualization = new VuMeterBuildStatusVisualization();
		visualization.init(config, this);

		Logger.info("Visualization initialized");

		return visualization;
	}

	private LED startBlinker() {
		final LED finalLed = new LED(config.getLedGpio());
		final int ledInterval = config.getLedInterval();

		new Thread(() -> {
			Logger.info("Started LED blinker thread.");

			try {
				while(true) {
					finalLed.on();
					Thread.sleep(ledInterval);
					finalLed.off();
					Thread.sleep(ledInterval);
				}
			}
			catch(InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}).start();

		return finalLed;
	}

	private void startUpdateThread(final HenkinsonGui gui, final VuMeterBuildStatusVisualization visualization, final BuzzerMorser morser) {
		Jenkins jenkins = new RemoteJenkins(config);

		// start the UI update thread.
		HenkinsonGui finalGui = gui;
		VuMeterBuildStatusVisualization finalVisualization = visualization;
		new Thread(() -> {
			try {
				Logger.info("Started Jenkins update thread.");
				JenkinsStatus oldStatus = null;
				while(true) {
					JenkinsStatus status = jenkins.retrieveStatus();

					if(status.isWorseThan(oldStatus)) {
						morser.buzzMorse("fuck");
					}

					oldStatus = status;

					if(finalGui != null) {
						finalGui.update(status);
					}

					if(finalVisualization != null) {
						finalVisualization.update(status);
					}

					HenkinsonUtil.sleep(config.getPollingInterval());
				}
			}
			catch(IOException e) {
				throw new IllegalStateException("Could not update Jenkins status", e);
			}
		}).start();
	}
}
