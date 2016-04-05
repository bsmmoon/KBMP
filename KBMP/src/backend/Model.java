package backend;/*
 * Facet
 */

import java.util.ArrayList;

import common.Module;
import common.ModulePlan;

public class Model {
	private ClipsWrapper clips;
	private ArrayList<Module> modules;
	private ArrayList<Module> availableModules;
	private ModulePlan plan;
	private int numberOfSemesterLeft;
	private int semester;

	public Model(ArrayList<Module> modules) {
		this.clips = new ClipsWrapper();
		this.plan = new ModulePlan();
		this.plan.createNewSemester();
		this.semester = 1;
		this.modules = modules;
		this.availableModules = new ArrayList<>();
	}

	public ModulePlan getModulePlan() { return plan; }

	public ArrayList<Module> getAvailableModules() { return availableModules; }

	public int getSemester() { return semester; }

	public void setNumberOfSemesterLeft(int semester) { this.numberOfSemesterLeft = semester; }

	public void execute(String command) { clips.execute(command); }

	public void reset(String condition) {
		clips.init(condition);
		clips.reset();
		clips.run();
	}

	public void update() { availableModules = clips.getAvailableModules(); }

	public void updatePlan(ArrayList<Module> modules) {
		modules.forEach((module) -> plan.addNewModule(module, semester));
	}

	public boolean isDone() {
		return semester == numberOfSemesterLeft;
	}
}
