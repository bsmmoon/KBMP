package backend;/*
 * Facet
 */

import java.util.ArrayList;

import common.Module;

public class Model {
	private ClipsWrapper clips;
	
	private ArrayList<Module> availableModules;

	public Model(ClipsWrapper clips) {
		this.clips = clips;
		this.availableModules = new ArrayList<Module>();
	}

	public ArrayList<Module> getAvailableModules() { return availableModules; }

	public void update() { availableModules = clips.getAvailableModules(); }
}
