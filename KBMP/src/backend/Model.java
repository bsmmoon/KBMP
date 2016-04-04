package backend;/*
 * Facet
 */

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Hashtable;

import common.Module;
import common.ModulePlan;

public class Model {
	private ClipsWrapper clips;
	private ArrayList<Module> availableModules = populateModules();
	private ModulePlan plan;

	private int numberOfSemesterLeft;
	private int semester;

	public Model(ClipsWrapper clips) {
		this.clips = clips;
		this.availableModules = new ArrayList<>();
		this.plan = new ModulePlan();
		this.plan.createNewSemester();
		this.semester = 1;
	}

	public ModulePlan getModulePlan() { return plan; }

	public ArrayList<Module> getAvailableModules() { return availableModules; }

	public int getSemester() { return semester; }

	public void setNumberOfSemesterLeft(int semester) { this.numberOfSemesterLeft = semester; }

	public void update() { availableModules = clips.getAvailableModules(); }

	public ArrayList<Module> populateModules() {
		ArrayList<Module> modules = new ArrayList<>();
		// if file containing collated modules exists, just populate from file

		// else if raw files exist, generate collated modules
		Path sem1ModulesJson = Paths.get("data/AY1516_S1_modules.json");
		// Path sem2ModulesJson = Paths.get("data/AY1516_S2_modules.json");
		try {
//			ArrayList<Module> sem1 = ModulesParser.updateModulesFromPath(sem1ModulesJson, new Hashtable<>(),
//					Module.Semester.ONE);
			return ModulesParser.updateModulesFromPath(sem1ModulesJson, new Hashtable<>(), Module.Semester.ONE);
		} catch (IOException e) {
			return modules;
		}

		// else throw error

//		return modules;
	}

	public void updatePlan(ArrayList<Module> modules) {
		modules.forEach((module) -> plan.addNewModule(module, semester));
	}

	public boolean isDone() {
		return semester == numberOfSemesterLeft;
	}
}
