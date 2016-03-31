/*
 * Facet
 */

import java.util.ArrayList;

import common.Module;

public class Model {
	private ClipsWrapper clips;
	
	private ArrayList<Module> modules;

	public Model(ClipsWrapper clips) {
		this.clips = clips;
		this.modules = new ArrayList<Module>();
	}
	
	public void update() {
		this.modules = this.clips.getAvailableModules();
		
		System.out.println("Modules Available: (" + modules.size() + ")");
		for (Module module : modules) {
			System.out.println(module);
		}
	}
}
