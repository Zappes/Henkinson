package net.bluephod.henkinson;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.diozero.ws281xj.LedDriverInterface;
import com.diozero.ws281xj.rpiws281x.WS281x;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import net.bluephod.henkinson.config.Configuration;
import net.bluephod.henkinson.gui.HenkinsonGui;
import net.bluephod.henkinson.jenkins.Jenkins;
import net.bluephod.henkinson.jenkins.JenkinsStatus;
import net.bluephod.henkinson.jenkins.RemoteJenkins;
import net.bluephod.henkinson.visualization.*;
import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;

public class Henkinson implements Runnable {
	private final Configuration config;
	private int connectionRetry = 0;
	private boolean stopRequested;
	private boolean stopExecuted;

	public Henkinson() throws IOException {
		config = Configuration.getInstance();

		Logger.info("Using configuration: \n" + new ObjectMapper().writeValueAsString(config));

		Configurator.currentConfig()
				.level(Level.valueOf(config.getLoglevel()))
				.activate();

	}

	@SuppressWarnings("squid:S2189") // the loop is intended to be infinite
	public static void main(String[] args) throws Exception {
		Henkinson henkinson = new Henkinson();
		Thread worker = new Thread(henkinson);
		worker.start();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				Logger.info("Shutdown hook executing.");
				henkinson.setStopRequested();
				worker.interrupt();
				Logger.info("Waiting for Henkinson to actually stop...");
				while(!henkinson.isStopExecuted()) {
					;
				}
			}
		});

		while(true) {
			// go to sleep here, because we'll consume LOTS of CPU if we don't...
			Thread.sleep(1000);
		}
	}

	private void setStopRequested() {
		this.stopRequested = true;
	}

	public boolean isStopExecuted() {
		return stopExecuted;
	}

	public void run() {
		try(HenkinsonCanvas canvas = new HenkinsonCanvas(getLedDriver()); HenkinsonGui gui = new HenkinsonGui(config)) {
			Logger.info("LED driver initialized.");

			int startDelay = config.getStartDelay();
			if(startDelay > 0) {
				Logger.info("Delaying startup for " + startDelay + "ms as configured.");
				sleep(startDelay);
				Logger.info("Continuing startup.");
			}

			do {
				try {
					Logger.info("Starting visualizations.");
					executeVisualizations(canvas, gui);
					Logger.info("Visualizations ended.");
				}
				catch(SocketTimeoutException | UnknownHostException e) {
					if(connectionRetry >= config.getConnectionRetries()) {
						Logger.warn("Maximum number of connection retries (" + config.getConnectionRetries() + " exceeded, exiting.");
						break;
					}
					else {
						connectionRetry++;
						Logger.info("Connection to Jenkins server timed out, retrying.");
						sleep(config.getConnectionRetryDelay());
					}
				}
				catch(Exception e) {
					Logger.error(e, "Caught unexpected Exception, terminating");
					break;
				}
			}
			while(!isStopRequested());
		}
		catch(IOException e) {
			Logger.error("Exception when closing the canvas:", e);
		}

		stopExecuted = true;
	}

	/**
	 * Pause without having to deal with an InterruptedException.
	 *
	 * @param sleepMillis The number of millseconds for which to pause.
	 *
	 * @see Thread#sleep(long)
	 */
	private void sleep(long sleepMillis) {
		try {
			Thread.sleep(sleepMillis);
		}
		catch(InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * Shows the actual visualizations until the service is stopped.
	 *
	 * @param canvas The LED driver to use for visualizations.
	 *
	 * @throws IOException If something goes wrong while reading data from Jenkins.
	 */
	private void executeVisualizations(HenkinsonCanvas canvas, HenkinsonGui gui) throws IOException {
		new BlueSplashStartUpVisualization().showStartUp(canvas);

		Jenkins jenkins = new RemoteJenkins(config.getJenkinsBaseUrl(), config.getUsername(), config.getPassword());

		BuildStatusVisualization visualization = new VuMeterBuildStatusVisualization();

		gui.init();

		JenkinsStatus initialStatus = jenkins.retrieveStatus();
		gui.update(initialStatus);
		visualization.init(canvas, initialStatus);

		connectionRetry = 0;

		Logger.info(String.format("Initialization done. Sleeping for %dms before first update occurs.", config.getInterval()));

		sleep(config.getInterval());

		Logger.info("Starting visualization update loop.");

		while(!isStopRequested()) {
			JenkinsStatus status = jenkins.retrieveStatus();

			gui.update(status);
			visualization.update(status);

			connectionRetry = 0;

			// it would be nice to have some kind of event trigger on status change in Jenkins instead of periodically polling the status. maybe
			// in a future release.
			sleep(config.getInterval());
		}
	}

	private boolean isStopRequested() {
		return stopRequested;
	}

	private LedDriverInterface getLedDriver() {
		if(config.isStripEnabled()) {
			return new WS281x(config.getGpio(), config.getBrightness(), config.getPixels());
		}
		else {
			return new DummyLedDriver(config.getPixels());
		}
	}
}
