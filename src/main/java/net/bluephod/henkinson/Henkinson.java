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

		try {
			if(config.isGuiEnabled()) {
				gui = new HenkinsonGui(config, this);
				gui.init();
			}

			if(config.isStripEnabled()) {
				visualization = new VuMeterBuildStatusVisualization();
				visualization.init(config, this);
			}

			if(config.isLedEnabled()) {
				led = new LED(23);
			}

			Jenkins jenkins = new RemoteJenkins(config);

			// start the UI update thread.
			HenkinsonGui finalGui = gui;
			VuMeterBuildStatusVisualization finalVisualization = visualization;
			new Thread(() -> {
				try {
					Logger.info("Started Jenkins update thread.");

					while(true) {
						JenkinsStatus status = jenkins.retrieveStatus();
						if(finalGui != null) {
							finalGui.update(status);
						}

						if(finalVisualization != null) {
							finalVisualization.update(status);
						}

						HenkinsonUtil.sleep(config.getInterval());
					}
				}
				catch(IOException e) {
					throw new IllegalStateException("Could not update Jenkins status", e);
				}
			}).start();

			LED finalLed = led;
			new Thread(() -> {
				Logger.info("Started LED blinker thread.");

				try {
					while(true) {
						finalLed.on();
						Thread.sleep(500);
						finalLed.off();
						Thread.sleep(500);
					}
				}
				catch(InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}).start();

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
}
