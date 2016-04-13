package backend;

import gui.GuiFrame;

import java.util.ArrayList;

import common.Module;

public class Main {
	public static void main(String[] args) {
		Logic logic = new Logic();
		logic.reset();

		GuiFrame frame = new GuiFrame("KBMP", logic);
		frame.init();
		
		frame.setVisible(true);

		logic.execute("(watch facts)");
		logic.execute("(watch focus)");
		
		// user preference for maths/communication mod
		// the alternative:
		logic.execute("(assert (goodmath) (commexempted))");
//		logic.execute("(assert (normalmath) (commnotexempted))");
		// H2 subjects
		logic.execute("(assert-taken \"H2Math\")");
		logic.execute("(assert-taken \"H2Physics\")");
		logic.iterate();

//		logic.iterate();
//
//		logic.setNumberOfSemesterLeft(8);
//		logic.assertTaken(makeModuleList(new String[]{"CS1010"}));
//		logic.assertWant(makeModuleList(new String[]{"CS2020"}));
//		logic.assertDontWant(makeModuleList(new String[]{"CS4244"}));
//
//		ArrayList<Module> list = makeModuleList(new String[]{"CS1010", "CS1020", "CS1231"});
//		logic.selectModules(list);
//
//		logic.iterate();
//
//		FocusArea focus = new FocusArea();
//		focus.setName("Artificial Intelligence");
//		logic.assertFocus(new ArrayList<FocusArea>(){{ add(focus); }});
//
//		logic.iterate();
//
//		logic.execute("(facts)");

		//frame.iterate();
	}

	public static ArrayList<Module> makeModuleList(String[] modules) {
		return new ArrayList<Module>(){{ for (String module : modules) add(new Module.Builder().setCode(module).build()); }};
	}
}
