package backend;

import gui.GuiFrame;

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

		frame.iterate();
	}
}
