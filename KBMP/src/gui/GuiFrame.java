package gui;

import java.awt.*;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

import backend.Logic;
import backend.Model;

import common.Module;
import common.ModulePlan;

@SuppressWarnings("serial")
public class GuiFrame extends JFrame {
	private JPanel cards;	//a panel that uses CardLayout
	private ArrayList<SelectionStep> steps;
	private SelectionStep.STEP currentStep;
	private final SelectionStep.STEP[] STEPS = SelectionStep.STEP.values();

	Logic logic;
	Model model;

	public GuiFrame(String title, Logic logic) {
		super(title);
		this.logic = logic;
		this.model = logic.getModel();

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		setSize(new Dimension(900,700));
		//setResizable(false);
	}

	public void init() {
		/*cards = new JPanel(new CardLayout());
	    steps = new ArrayList<SelectionStep> ();

		currentStep = SelectionStep.STEP.NUM_SEM_LEFT;

		for (SelectionStep.STEP stepNum : SelectionStep.STEP.values()) {
			SelectionStep step = new SelectionStep(this, false);
			steps.add(step);
			cards.add(step);
		}

		add(cards);
*/
		getContentPane().setBackground(Color.WHITE);

		SelectionStep step = new SelectionStep(this,false);
		step.setStep(SelectionStep.STEP.PRE_PLAN);
		step.init();
		add(step);
	}
/*
	public void nextStep() {
		CardLayout c = (CardLayout)(cards.getLayout());
		if (currentStep.ordinal() < STEPS.length-1) {
			c.next(cards);
			currentStep = STEPS[currentStep.ordinal()+1];
			iterate();
		}
		
	}

	public void iterate() {

		ArrayList<Module> availableModules = model.getAvailableModules();
		System.out.println("Modules Available: (" + availableModules.size() + ")");
		SelectionStep step = steps.get(currentStep.ordinal());
		step.init();
        step.setDropdownItems(model.getAvailableModules());
	}
*/
	public Logic getLogic() {
		return logic;
	}

	public Model getModel() { return model; }

	public SelectionStep.STEP getCurrentStep() {
		return currentStep;
	}
}
