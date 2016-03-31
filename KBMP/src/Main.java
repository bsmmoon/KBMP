import gui.GuiFrame;

public class Main {
	public static void main(String[] args) {
		GuiFrame frame = new GuiFrame("KBMP");
		Logic logic = new Logic();
		logic.reset();
		
		frame.setVisible(true);
//		logic.execute("(assert (flag (type test)))");
		logic.execute("(focus RANK SELECT)");
		logic.execute("(run)");
	}
}
