package backend;/*
 * Facet
 */

import java.util.ArrayList;
import java.util.Hashtable;

import common.AvailableModule;
import common.FocusArea;
import common.Module;
import common.ModulePlan;
import common.Semester;

public class Model {
	private ClipsWrapper clips;
	private ArrayList<FocusArea> focusAreas;
	private ArrayList<FocusArea> selectedFocusAreas;
	private ArrayList<Module> preplanModules;
	private ArrayList<Module> modules;
	private ArrayList<AvailableModule> availableModules;
	private ModulePlan plan;
	private int totalSemesters;
	private int semester;

	Hashtable<Module.WorkloadTypes, Float> STANDARD_WORKLOADS;

	public Model() {
		this.clips = new ClipsWrapper();
		this.plan = new ModulePlan();
		this.totalSemesters = 8;
		this.focusAreas = new ArrayList<>();
		this.selectedFocusAreas = new ArrayList<>();
		this.modules = new ArrayList<>();
		this.availableModules = new ArrayList<>();

		STANDARD_WORKLOADS = new Hashtable<>();
		for (Module.WorkloadTypes type : Module.WorkloadTypes.values()) {
			STANDARD_WORKLOADS.put(type, 2.0f);
		}
	}

	public ArrayList<FocusArea> getAllFocusAreas() { return focusAreas; }
	public ArrayList<FocusArea> getSelectedFocusAreas() { return selectedFocusAreas; }
	public ModulePlan getModulePlan() { return plan; }

	public ArrayList<AvailableModule> getRecommendedModules() {
		ArrayList<AvailableModule> result = new ArrayList<>();
		int count = 5;
		for (AvailableModule availableModule : availableModules) {
			if (availableModule.isNotRecommended()) continue;
			result.add(availableModule);
			count--;
			if (count == 0) break;
		}
		return result;
	}

	public ArrayList<AvailableModule> getAvailableModules() { return availableModules; }
	public ArrayList<Module> getPreplanModules() { return preplanModules; }
	public ArrayList<Module> getModules() { return modules; }
	public int getYear() { return semester % 2 == 0 ? Math.floorDiv(semester, 2) : Math.floorDiv(semester, 2) + 1; }
	public int getSemester() { return semester % 2 == 0 ? 2 : 1; }
	public int getCumulativeSemester() { return semester; }
	public boolean isDone() {
		return semester > totalSemesters;
	}

	public void setModules(ArrayList<Module> modules) {
		this.modules = modules;
		this.preplanModules = modules;
		this.modules.sort((a,b) -> a.getCode().compareTo(b.getCode()));
		addPlaceHolderModules();
	}

	public void setAllFocusAreas(ArrayList<FocusArea> focusAreas) {
		this.focusAreas = focusAreas;

		ArrayList<String> modulesInFocusAreas = new ArrayList<>();
		for (FocusArea focus : focusAreas) {
			for (String primary : focus.getPrimaries()) modulesInFocusAreas.add(primary);
			for (String elective : focus.getElectives()) modulesInFocusAreas.add(elective);
			for (String elective : focus.getUnrestrictedElectives()) modulesInFocusAreas.add(elective);
		}
		clips.saveModulesInFocusAreas(modulesInFocusAreas);
	}

	public void setSelectedFocusAreas(ArrayList<FocusArea> selectedFocusAreas) {
		this.selectedFocusAreas = selectedFocusAreas;
		selectedFocusAreas.forEach((selectedFocusArea) -> execute("(assert-focus-on \"" + selectedFocusArea.getName() + "\")"));

		String primaryfocus = "(assert-primaryfocus";
		String electivefocus = "(assert-electivefocus";
		for (FocusArea focus : selectedFocusAreas) {
			for (String primary : focus.getPrimaries()) primaryfocus += " \"" + primary + "\"";
			for (String elective : focus.getElectives()) electivefocus += " \"" + elective + "\"";
			for (String elective : focus.getUnrestrictedElectives()) electivefocus += " \"" + elective + "\"";
		}
		primaryfocus += ")";
		electivefocus += ")";

		execute(primaryfocus);
		execute(electivefocus);
	}

	public void setStartingSemester(int semester) {
		this.semester = semester;
		this.plan.setSemesters(totalSemesters, semester);
		execute("(assert (current-semester (number " + semester + ")))");
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

	public boolean isSkipSemester() {
		ArrayList<String> moduleCodes = new ArrayList<>();
		ArrayList<Module> modules = new ArrayList<>();
		if (clips.isSkipSemester(semester, moduleCodes)) {
			moduleCodes.forEach((code) -> modules.add(new Module.Builder().setCode(code).setName("").setCredits(4).setWorkload(STANDARD_WORKLOADS).build()));
			plan.addNewModules(modules, semester);
			return true;
		}
		return false;
	}

	public ArrayList<AvailableModule> getSkipModules() {
		Semester sem = plan.getSemester(semester);

		ArrayList<AvailableModule> availableModules = new ArrayList<>();
		AvailableModule availableModule;
		for (Module module : sem.getModules()) {
			availableModule = new AvailableModule(module.getCode(), 0, "");
			availableModule.setModule(module);
			availableModules.add(availableModule);
		}

		return availableModules;
	}

	public void assertTaken(ArrayList<Module> modules) { modules.forEach((module) -> execute("(assert-taken \"" + module.getCode() + "\")")); }

	public void assertTaken(String module) { execute("(assert-taken \"" + module + "\")"); }

	public void assertWant(ArrayList<Module> modules) { modules.forEach((module) -> execute("(assert-want \"" + module.getCode() + "\")")); }

	public void assertDontWant(ArrayList<Module> modules) { modules.forEach((module) -> execute("(assert-dontwant \"" + module.getCode() + "\")")); }

	public void selectModules(ArrayList<Module> modules) { updatePlan(modules); }

	public void updateModulesInClips() {
		ArrayList<Module> modules = plan.getSemester(semester).getModules();
		modules.forEach((module) -> execute("(assert-selected \"" + module.getCode() + "\")"));
		execute("(increment-semester)");
		updateSemester();
	}

	public void assertSymbolFact(String fact) { execute("(assert (" + fact + "))"); }

	public void iterate() {
		execute("(focus SELECT RANK)");
		execute("(refresh RANK::mark-available-no-prerequisites-level-1)");
		execute("(refresh RANK::mark-available-no-prerequisites-level-2)");
		execute("(refresh RANK::mark-available-no-prerequisites-level-3)");
		execute("(refresh RANK::mark-available-no-prerequisites-level-3-higher)");
		execute("(run)");
		update();
	}

	private void update() {
		availableModules = clips.getAvailableModules();
		availableModules.sort(AvailableModule::compareTo);
		availableModules.forEach((availableModule) -> availableModule.setModule(findModuleByCode(availableModule.getCode())));
	}

	private void updatePlan(ArrayList<Module> modules) {
		plan.addNewModules(modules, semester);
		int[] workloads = plan.getWorkloads(semester);
		for (int workload : workloads) {
			System.out.print(workload + " ");
		}
		System.out.println();
	}

	private void updateSemester() { semester = clips.getCurrentSemester(); }

	private Module findModuleByCode(String code) {
		for (Module module : modules) {
			if (module.getCode().equals(code)) return module;
		}
		return new Module("", "");
	}


	private void addPlaceHolderModules() {
		Module.Semester sem = Module.Semester.values()[2];

		this.modules.add(new Module.Builder().setCode("SS0123").setName("Singapore Study").setCredits(4).setWorkload(STANDARD_WORKLOADS).setPrerequisites("").setPreclusions("").setSemesters(sem).build());

		this.modules.add(new Module.Builder().setCode("GEM0123").setName("GEM / GE").setCredits(4).setWorkload(STANDARD_WORKLOADS).setPrerequisites("").setPreclusions("").setSemesters(sem).build());
		this.modules.add(new Module.Builder().setCode("GEM0123").setName("GEM / GE").setCredits(4).setWorkload(STANDARD_WORKLOADS).setPrerequisites("").setPreclusions("").setSemesters(sem).build());

		this.modules.add(new Module.Builder().setCode("BR0123").setName("Breadth / GE").setCredits(4).setWorkload(STANDARD_WORKLOADS).setPrerequisites("").setPreclusions("").setSemesters(sem).build());
		this.modules.add(new Module.Builder().setCode("BR0123").setName("Breadth / GE").setCredits(4).setWorkload(STANDARD_WORKLOADS).setPrerequisites("").setPreclusions("").setSemesters(sem).build());

		this.modules.add(new Module.Builder().setCode("SC0123").setName("Science 1").setCredits(4).setWorkload(STANDARD_WORKLOADS).setPrerequisites("").setPreclusions("").setSemesters(sem).build());
		this.modules.add(new Module.Builder().setCode("SC0123").setName("Science 2").setCredits(4).setWorkload(STANDARD_WORKLOADS).setPrerequisites("").setPreclusions("").setSemesters(sem).build());
		this.modules.add(new Module.Builder().setCode("SC0123").setName("Science 3").setCredits(4).setWorkload(STANDARD_WORKLOADS).setPrerequisites("").setPreclusions("").setSemesters(sem).build());
	}
}
