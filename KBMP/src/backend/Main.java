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
//		logic.execute("(assert (flag (type test)))");
		logic.execute("(focus RANK SELECT)");
		logic.execute("(run)");

		logic.setNumberOfSemesterLeft(8);
		logic.assertTaken(makeModuleList(new String[]{"CS1010"}));
		logic.assertWant(makeModuleList(new String[]{"CS2020"}));
		logic.assertDontWant(makeModuleList(new String[]{"CS4244"}));

		ArrayList<Module> list = makeModuleList(new String[]{"CS1020", "CS1231"});
		logic.selectModules(list);

		frame.iterate();
	}

	public static ArrayList<Module> makeModuleList(String[] modules) {
		return new ArrayList<Module>(){{ for (String module : modules) add(new Module.Builder().setCode(module).build()); }};
	}
}
