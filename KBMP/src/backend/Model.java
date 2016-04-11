package backend;/*
 * Facet
 */

import java.util.ArrayList;
import java.util.Hashtable;

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

	public ArrayList<FocusArea> getAllFocusAreas() { return focusAreas; }
	public ArrayList<FocusArea> getSelectedFocusAreas() { return selectedFocusAreas; }
	public ModulePlan getModulePlan() { return plan; }
	public ArrayList<Module> getRecommendedModules() { return new ArrayList<>(availableModules.subList(0, 5)); }
	public ArrayList<Module> getAvailableModules() { return availableModules; }
	public ArrayList<Module> getModules() { return modules; }
	public int getSemester() { return semester; }
	public boolean isDone() {
		return semester > numberOfSemesterLeft;
	}

	public void setModules(ArrayList<Module> modules) {
		this.modules = modules;
		addPlaceHolderModules();
		pruneNonFocusHighLevelModules();
	}

	public void setAllFocusAreas(ArrayList<FocusArea> focusAreas) { this.focusAreas = focusAreas; }

	public void setSelectedFocusAreas(ArrayList<FocusArea> selectedFocusAreas) {
		this.selectedFocusAreas = selectedFocusAreas;
		selectedFocusAreas.forEach((selectedFocusArea) -> execute("(assert-focus-on \"" + selectedFocusArea.getName() + "\")"));
	}

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

	public void assertTaken(ArrayList<Module> modules) { modules.forEach((module) -> execute("(assert-taken \"" + module.getCode() + "\")")); }

	public void assertWant(ArrayList<Module> modules) { modules.forEach((module) -> execute("(assert-want \"" + module.getCode() + "\")")); }

	public void assertDontWant(ArrayList<Module> modules) { modules.forEach((module) -> execute("(assert-dontwant \"" + module.getCode() + "\")")); }

	public void selectModules(ArrayList<Module> modules) {

		modules.forEach((module) -> execute("(assert-selected \"" + module.getCode() + "\")"));
		updatePlan(modules);
		incrementSemester();
	}

	public void iterate() {
		execute("(focus SELECT RANK)");
		execute("(refresh RANK::mark-available-no-prerequisites-level-1)");
		execute("(refresh RANK::mark-available-no-prerequisites-level-2)");
		execute("(refresh RANK::mark-available-no-prerequisites-level-3)");
		execute("(refresh RANK::mark-available-no-prerequisites-level-3-higher)");
		execute("(run)");
		update();
//		execute("(facts)");
	}

	private void update() {
		availableModules = new ArrayList<>();
		ArrayList<String> availableModulesCodes = clips.getAvailableModules();
		availableModulesCodes.forEach((code) -> availableModules.add(findModuleByCode(code)));
	}

	private void updatePlan(ArrayList<Module> modules) {
		modules.forEach((module) -> plan.addNewModule(module, semester));
		Float[] workloads = plan.getWorkloads(semester);
		for (Float workload : workloads) {
			System.out.print(workload + " ");
		}
		System.out.println();
	}

	private void incrementSemester() { semester++; }

	private Module findModuleByCode(String code) {
		for (Module module : modules) {
			if (module.getCode().equals(code)) return module;
		}
		return new Module("", "");
	}


	private void addPlaceHolderModules() {
		Hashtable<Module.WorkloadTypes, Float> standardWorkloads = new Hashtable<>();
		for (Module.WorkloadTypes type : Module.WorkloadTypes.values()) {
			standardWorkloads.put(type, 2.0f);
		}
		int num = 5;
		while (num-- > 1) {
			this.modules.add(new Module.Builder().setCode("SC0123").setName("Science " + num).setCredits(4).setWorkload(standardWorkloads).setPrerequisites("").setPreclusions("").build());
			this.modules.add(new Module.Builder().setCode("SS0123").setName("Singapore Study " + num).setCredits(4).setWorkload(standardWorkloads).setPrerequisites("").setPreclusions("").build());
			this.modules.add(new Module.Builder().setCode("GEM0123").setName("General Education Module " + num).setCredits(4).setWorkload(standardWorkloads).setPrerequisites("").setPreclusions("").build());
			this.modules.add(new Module.Builder().setCode("BR0123").setName("Breadth " + num).setCredits(4).setWorkload(standardWorkloads).setPrerequisites("").setPreclusions("").build());
		}
	}

	private void pruneNonFocusHighLevelModules() {
		for (Module module : modules) {

		}
	}
}
