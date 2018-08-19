package net.bluephod.henkinson;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import com.diozero.ws281xj.rpiws281x.WS281x;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.bluephod.henkinson.config.Configuration;
import net.bluephod.henkinson.jenkins.Jenkins;
import net.bluephod.henkinson.jenkins.RemoteJenkins;
import net.bluephod.henkinson.visualization.BlueSplashStartUpVisualization;
import net.bluephod.henkinson.visualization.BuildStatusVisualization;
import net.bluephod.henkinson.visualization.HenkinsonCanvas;
import net.bluephod.henkinson.visualization.VuMeterBuildStatusVisualization;
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
			;
		}
	}

	private void setStopRequested() {
		this.stopRequested = true;
	}

	public boolean isStopExecuted() {
		return stopExecuted;
	}

	public void run() {
		HenkinsonCanvas canvas = new HenkinsonCanvas(new WS281x(config.getGpio(), config.getBrightness(), config.getPixels()));
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
				executeVisualizations(canvas);
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

		// this is where one would turn off the strip, but doing so reproducibly crashes the VM...
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
	 * Shows the actual visualizations until the service is stopped through the creation of the killfile.
	 *
	 * @param canvas The LED driver to use for visualizations.
	 *
	 * @throws IOException If something goes wrong while reading data from Jenkins.
	 */
	private void executeVisualizations(final HenkinsonCanvas canvas) throws IOException {
		new BlueSplashStartUpVisualization().showStartUp(canvas);

		Jenkins jenkins = new RemoteJenkins(config.getJenkinsBaseUrl(), config.getUsername(), config.getPassword());
		BuildStatusVisualization visualization = new VuMeterBuildStatusVisualization();

		visualization.init(canvas, jenkins.retrieveStatus());
		connectionRetry = 0;

		Logger.info(String.format("Initialization done. Sleeping for %dms before first update occurs.", config.getInterval()));

		sleep(config.getInterval());

		Logger.info("Starting visualization update loop.");

		while(!isStopRequested()) {
			visualization.update(jenkins.retrieveStatus());
			connectionRetry = 0;

			// it would be nice to have some kind of event trigger on status change in Jenkins instead of periodically polling the status. maybe
			// in a future release.
			sleep(config.getInterval());
		}
	}

	private boolean isStopRequested() {
		return stopRequested;
	}
}
