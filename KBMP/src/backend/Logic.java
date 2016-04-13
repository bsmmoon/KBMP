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
		model.setAllFocusAreas(focus);
		model.setModules(modules);

		String condition = storage.readCondition();
		model.reset(condition);

		iterate();
	}

	public void iterate() { model.iterate(); }
	
	public void execute(String command) { model.execute(command); }

	public void setStartTime(int year, int semester) { model.setStartingSemester((year - 1) * 2 + semester); }

	public void assertTaken(ArrayList<Module> modules) { model.assertTaken(modules); }

	public void assertWant(ArrayList<Module> modules) { model.assertWant(modules); }

	public void assertDontWant(ArrayList<Module> modules) { model.assertDontWant(modules); }

	public void assertFocus(ArrayList<FocusArea> focusAreas) { model.setSelectedFocusAreas(focusAreas); }

	public void selectModules(ArrayList<Module> modules) { model.selectModules(modules); }

	public void assertGoodMath() { model.assertSymbolFact("goodmath"); }

	public void assertNormalMath() { model.assertSymbolFact("normalmath"); }

	public void assertCommunicationException() { model.assertSymbolFact("commnotexcempted"); }

	public void assertCommunicationNotExcepted() { model.assertSymbolFact("commnotexcempted"); }

	public void assertH2Maths() { model.assertTaken("H2Math"); }

	public void assertH2Physics() { model.assertTaken("H2Physics"); }

	public void assertSIP() { model.assertSymbolFact("SIP"); }

	public void asertATAP(int semester) { model.assertSymbolFact("ATAP" + semester); }

	public void assertNOC1Sem(int semester) { model.assertSymbolFact("NOCSem" + semester); }

	public void assertNOC1Year(int semester) { model.assertSymbolFact("NOCYear " + semester + (semester + 1)); }

	public void assertCS3201() { model.assertSymbolFact("softwareprojectnormal"); }

	public void assertCS3216() { model.assertSymbolFact("softwareprojectthematic"); }

	public void assertCS3281() { model.assertSymbolFact("softwareprojectmedia"); }

	public void assertCS3283() { model.assertSymbolFact("softwareprojectmodern"); }


}
