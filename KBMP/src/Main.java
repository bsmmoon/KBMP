import gui.GuiFrame;

public class Main {
	public static void main(String[] args) {
		GuiFrame frame = new GuiFrame("KBMP");
		Logic logic = new Logic();
		
		frame.setVisible(true);
//		logic.execute("(assert (flag (type test)))");
		logic.execute("(run)");
	}
}
