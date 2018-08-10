package net.bluephod.henkinson;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.diozero.ws281xj.LedDriverInterface;
import com.diozero.ws281xj.rpiws281x.WS281x;
import net.bluephod.henkinson.config.Configuration;
import net.bluephod.henkinson.jenkins.Jenkins;
import net.bluephod.henkinson.jenkins.RemoteJenkins;
import net.bluephod.henkinson.visualization.BuildStatusVisualization;
import net.bluephod.henkinson.visualization.VuMeterBuildStatusVisualization;
import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;

public class Henkinson {
	public static final int EXIT_CODE_CON_TIME_OUT = 1;
	public static final int EXIT_CODE_OK = 0;

	private final Configuration config;
	private int connectionRetry = 0;

	public Henkinson() throws IOException {
		config = Configuration.getInstance();

		Configurator.currentConfig()
				.level(Level.valueOf(config.getLoglevel()))
				.activate();
	}

	public static void main(String[] args) throws Exception {
		int returnValue = new Henkinson().run();
		Logger.info("Exiting Henkinson.");
		System.exit(returnValue);
	}

	private int run() throws IOException, InterruptedException {
		boolean serviceStopped = false;

		do {
			try(LedDriverInterface ledDriver = new WS281x(config.getGpio(), config.getBrightness(), config.getPixels())) {
				executeVisualizations(ledDriver);
				serviceStopped = true;
			}
			catch(SocketTimeoutException e) {
				if(connectionRetry >= config.getConnectionRetries()) {
					Logger.warn("Maximum number of connection retries (" + config.getConnectionRetries() + " exceeded, exiting.");
					return EXIT_CODE_CON_TIME_OUT;
				}
				else {
					connectionRetry++;
					Logger.info("Connection to Jenkins server timed out, retrying.");
					Thread.sleep(config.getConnectionRetryDelay());
				}
			}
		}
		while(!serviceStopped);

		return EXIT_CODE_OK;
	}

	/**
	 * Shows the actual visualizations until the service is stopped through the creation of the killfile.
	 *
	 * @param ledDriver The LED driver to use for visualizations.
	 *
	 * @throws IOException If something goes wrong while reading data from Jenkins.
	 */
	private void executeVisualizations(final LedDriverInterface ledDriver) throws IOException {
		Jenkins jenkins = new RemoteJenkins(config.getJenkinsBaseUrl(), config.getUsername(), config.getPassword());
		BuildStatusVisualization visualization = new VuMeterBuildStatusVisualization();

		visualization.init(ledDriver, jenkins.retrieveStatus());
		connectionRetry = 0;
		sleep(config.getInterval());

		while(!serviceShouldStop()) {
			visualization.update(jenkins.retrieveStatus());
			connectionRetry = 0;

			// it would be nice to have some kind of event trigger on status change in Jenkins instead of periodically polling the status. maybe
			// in a future release.
			sleep(config.getInterval());
		}
		Logger.info(String.format("Kill file (%s) found, deleting and exiting.", config.getKillfile()));

		visualization.shutDown();
	}

	/**
	 * Pause without having to deal with an InterruptedException.
	 *
	 * @param sleepMillis The number of millseconds for which to pause.
	 *
	 * @see Thread#sleep(long)
	 */
	private void sleep(int sleepMillis) {
		try {
			Thread.sleep(sleepMillis);
		}
		catch(InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	private boolean serviceShouldStop() throws IOException {
		return Files.deleteIfExists(Paths.get(config.getKillfile()));
	}
}
