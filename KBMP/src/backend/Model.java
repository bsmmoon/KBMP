package backend;/*
 * Facet
 */

import java.util.ArrayList;

import common.FocusArea;
import common.Module;
import common.ModulePlan;

public class Model {
	private ClipsWrapper clips;
	private ArrayList<FocusArea> focusAreas;
	private ArrayList<FocusArea> selectedFocusAreas;
	private ArrayList<Module> modules;
	private ArrayList<Module> availableModules;
	private ModulePlan plan;
	private int numberOfSemesterLeft;
	private int semester;

	public Model() {
		this.clips = new ClipsWrapper();
		this.plan = new ModulePlan();
		this.semester = 1;
		this.focusAreas = new ArrayList<>();
		this.selectedFocusAreas = new ArrayList<>();
		this.modules = new ArrayList<>();
		this.availableModules = new ArrayList<>();
	}

	public void setModules(ArrayList<Module> modules) { this.modules = modules; }

	public ArrayList<FocusArea> getAllFocusAreas() { return focusAreas; }

	public void setAllFocusAreas(ArrayList<FocusArea> focusAreas) { this.focusAreas = focusAreas; }

	public ArrayList<FocusArea> getSelectedFocusAreas() { return selectedFocusAreas; }

	public void setSelectedFocusAreas(ArrayList<FocusArea> selectedFocusAreas) {
		this.selectedFocusAreas = selectedFocusAreas;
		selectedFocusAreas.forEach((selectedFocusArea) -> execute("(assert-focus-on \"" + selectedFocusArea.getName() + "\")"));
	}

	public ModulePlan getModulePlan() { return plan; }

	public ArrayList<Module> getAvailableModules() { return availableModules; }

	public void incrementSemester() { semester++; }

	public int getSemester() { return semester; }

	public void setNumberOfSemesterLeft(int numberOfSemesterLeft) {
		this.numberOfSemesterLeft = numberOfSemesterLeft;
		this.plan.setSemesters(numberOfSemesterLeft);
	}

	public void execute(String command) {
		System.out.println("CLIPS>> " + command);
		clips.execute(command);
	}

	public void reset(String condition) {
		clips.init(condition);
		clips.reset();
		clips.saveModules(modules);
		clips.saveFocusAreas(focusAreas);
		clips.run();
	}

	public void update() { availableModules = clips.getAvailableModules(); }

	public void updatePlan(ArrayList<Module> modules) {
		modules.forEach((module) -> plan.addNewModule(module, semester));
	}

	public boolean isDone() {
		return semester > numberOfSemesterLeft;
	}
}
