package backend;

import java.util.ArrayList;

import common.FocusArea;
import common.Module;

/*
 * Workflow:
 * 		1. Receives user command from view
 * 		2. Parse command into CLIPS command (backend.ClipsParser)
 * 		3. Pass the command to the CLIPS (backend.ClipsWrapper)
 * 		4. Fetch status from CLIPS (backend.ClipsWrapper)
 * 		5. Return status to view
 */
public class Logic {
	private Storage storage;

	private Model model;

	public Logic() {
		this.storage = new Storage();
		this.model = new Model();
	}

	public Model getModel() { return model; }

	public void reset() {
		ArrayList<Module> modules;
		ArrayList<FocusArea> focus;
		try {
			modules = this.storage.readModules();
			focus = this.storage.readFocusArea();
		} catch (Exception e) {
			e.printStackTrace();
			modules = new ArrayList<>();
			focus = new ArrayList<>();
		}
		model.setModules(modules);
		model.setAllFocusAreas(focus);

		String condition = storage.readCondition();
		model.reset(condition);

		iterate();
	}

	public void iterate() {
		execute("(focus SELECT RANK)");
		execute("(run)");
	}
	
	public void execute(String command) {
		model.execute(command);
		model.update();
	}

	public void setNumberOfSemesterLeft(int semester) { model.setNumberOfSemesterLeft(semester); }

	public void assertTaken(ArrayList<Module> modules) { modules.forEach((module) -> execute("(assert-taken \"" + module.getCode() + "\")")); }

	public void assertWant(ArrayList<Module> modules) { modules.forEach((module) -> execute("(assert-want \"" + module.getCode() + "\")")); }

	public void assertDontWant(ArrayList<Module> modules) { modules.forEach((module) -> execute("(assert-dontwant \"" + module.getCode() + "\")")); }

	public void assertFocus(ArrayList<FocusArea> focusAreas) { model.setSelectedFocusAreas(focusAreas); }

	public void selectModules(ArrayList<Module> modules) {
		modules.forEach((module) -> execute("(assert-selected \"" + module.getCode() + "\")"));
		model.updatePlan(modules);
	}
}
