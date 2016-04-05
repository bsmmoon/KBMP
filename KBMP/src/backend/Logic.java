package backend;

import java.util.ArrayList;

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

		ArrayList<Module> modules;
		try {
			modules = this.storage.readModules();
		} catch (Exception e) {
			e.printStackTrace();
			modules = new ArrayList<>();
		}

		this.model = new Model(modules);
	}

	public Model getModel() { return model; }

	public void reset() {
		String condition = storage.readCondition();
		model.reset(condition);
	}
	
	public void execute(String command) {
		System.out.println("CLIPS>> " + command);
		model.execute(command);
		model.update();
	}

	public void setNumberOfSemesterLeft(int semester) { model.setNumberOfSemesterLeft(semester); }

	public void assertTaken(ArrayList<Module> modules) { modules.forEach((module) -> execute("(assert (taken " + module.getCode() + "))")); }

	public void assertWant(ArrayList<Module> modules) { modules.forEach((module) -> execute("(assert (want " + module.getCode() + "))")); }

	public void assertDontWant(ArrayList<Module> modules) { modules.forEach((module) -> execute("(assert (dontwant " + module.getCode() + "))")); }

	public void selectModules(ArrayList<Module> modules) {
		modules.forEach((module) -> execute("(assert (selected " + module.getCode() + " " + model.getSemester() + "))"));
		model.updatePlan(modules);
	}
}
