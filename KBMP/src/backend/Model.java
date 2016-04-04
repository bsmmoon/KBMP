package backend;/*
 * Facet
 */

import java.lang.reflect.Array;
import java.util.ArrayList;

import com.sun.org.apache.xpath.internal.operations.Mod;
import common.Module;
import common.ModulePlan;

public class Model {
	private ClipsWrapper clips;

	private ModulePlan plan;
	private ArrayList<Module> availableModules;

	private int numberOfSemesterLeft;
	private int semester;

	public Model(ClipsWrapper clips) {
		this.clips = clips;
		this.plan = new ModulePlan();
		this.plan.createNewSemester();
		this.availableModules = new ArrayList<Module>();

		this.semester = 1;
	}

	public ModulePlan getModulePlan() { return plan; }

	public ArrayList<Module> getAvailableModules() { return availableModules; }

	public int getSemester() { return semester; }

	public void setNumberOfSemesterLeft(int semester) { this.numberOfSemesterLeft = semester; }

	public void update() { availableModules = clips.getAvailableModules(); }

	public void updatePlan(ArrayList<Module> modules) {
		modules.forEach((module) -> plan.addNewModule(module, semester));
	}

	public boolean isDone() {
		return semester == numberOfSemesterLeft;
	}
}
