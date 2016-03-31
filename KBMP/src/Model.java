/*
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
	
	public void update() {
		availableModules = clips.getAvailableModules();

		System.out.println("Modules Available: (" + availableModules.size() + ")");
		availableModules.forEach(System.out::println);
	}
}
