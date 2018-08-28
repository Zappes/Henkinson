package net.bluephod.henkinson.visualization;

import java.io.Closeable;

import net.bluephod.henkinson.Henkinson;
import net.bluephod.henkinson.config.Configuration;
import net.bluephod.henkinson.jenkins.JenkinsStatus;

/**
 * Interface for build status visualizations.
 * <p>
 * The first version of this software only contains one visualization, and that's a classic VU meter. Many oder visualizations are
 * conceivable, and there'll possibly be a version of the Henkinson in which a list of visualizations can be defined as a "playlist". In
 * order to make that improvement easy, an interface has been extracted early on.
 * <p>
 * (Hint: Creating new visualizations would be the part of the software for which contributions by other people would be the most
 * appreciated.)
 */
public interface BuildStatusVisualization extends Closeable {
	void init(Configuration config, Henkinson henkinson);

	/**
	 * Updates the status to be shown.
	 * <p>
	 * When this method is called, the visualization does whatever it does when showing a changed status. This may just set a certain LED
	 * pattern or it may play some animation.
	 * <p>
	 * Note that this method may be called arbitrarily often  (including not at all), periodically or in response to some change in the status
	 * of
	 * the server between calls to init and shutdown. It should return "within a reasonable time" as service shutdown can only occur between
	 * calls of this method. Think "cooperative multitasking" like in Windows 3.1.
	 *
	 * @param status The status that should be visualized.
	 */
	void update(JenkinsStatus status);
}
