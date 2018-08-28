package net.bluephod.henkinson;

import java.io.IOException;

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

	public Henkinson() throws IOException {
		config = Configuration.getInstance();
		Logger.info("Using configuration: \n" + new ObjectMapper().writeValueAsString(config));
	}

	public static void main(String[] args) throws Exception {
		System.exit(new Henkinson().monitorJenkins());
	}

	private int monitorJenkins() throws IOException {
		if(!(config.isGuiEnabled() || config.isStripEnabled())) {
			Logger.warn("Neither strip nor GUI is enabled - exiting.");
			return 1;
		}

		HenkinsonGui gui = null;
		VuMeterBuildStatusVisualization visualization = null;

		try {
			if(config.isGuiEnabled()) {
				gui = new HenkinsonGui(config, this);
				gui.init();
			}

			if(config.isStripEnabled()) {
				visualization = new VuMeterBuildStatusVisualization();
				visualization.init(config, this);
			}

			Jenkins jenkins = new RemoteJenkins(config);

			// start the UI update thread.
			HenkinsonGui finalGui = gui;
			VuMeterBuildStatusVisualization finalVisualization = visualization;
			new Thread(() -> {
				try {
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

			if(gui != null) {
				gui.waitForKeypress();
				return 0;
			}
			else {
				while(true) {
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
		}
	}
}
