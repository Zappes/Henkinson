package net.bluephod.henkinson.visualization;

import com.diozero.ws281xj.LedDriverInterface;

/**
 * Visualizations implementing this interface will be shown on startup.
 */
public interface StartUpVisualization {
	void showStartUp(LedDriverInterface driver);
}
