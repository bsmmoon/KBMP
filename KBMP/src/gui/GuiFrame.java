package gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import common.Module;
import common.ModulePlan;

import backend.Logic;
import backend.Model;

@SuppressWarnings("serial")
public class GuiFrame extends JFrame {
	private JPanel cards;	//a panel that uses CardLayout
	private int currentStep = 1;
	String[] selections = {"CS1101","CS2101","CS2102","CS3342"};

	Logic logic;
	Model model;

	public GuiFrame(String title, Logic logic) {
		super(title);
		this.logic = logic;
		this.model = logic.getModel();

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		setSize(500,500);
	}

	public void init() {
		SelectionStep selection1 = new SelectionStep(this, false);
		selection1.setQuestion("Please select modules that you have already taken.");
		selection1.setDropdownItems(selections);

		SelectionStep selection2 = new SelectionStep(this, true);
		selection2.setQuestion("Please select modules that you want to take.");
		selection2.setDropdownItems(selections);

		SelectionStep selection3 = new SelectionStep(this, false);
		selection3.setQuestion("Please select modules that you don't want to take.");
		selection3.setDropdownItems(selections);

		SelectionStep selection4 = new SelectionStep(this, false);
		selection4.setQuestion("Please select your focus area.");
		selection4.setDropdownItems(selections);

		SelectionStep selection5 = new SelectionStep(this, false);
		selection5.setQuestion("Please select your program.");
		selection5.setDropdownItems(selections);

		ModulePlan plan = new ModulePlan();
		plan.createNewSemester();
		plan.createNewSemester();
		plan.addNewModule(new Module("CS1010","Programming Methodology"), 1);
		plan.addNewModule(new Module("CS2020","Data XXX"), 1);
		plan.addNewModule(new Module("CS1231","Discrete Maths"), 2);
		plan.addNewModule(new Module("CS3245","Information Retrieval"), 2);

		PlanPanel planPanel = new PlanPanel(plan);

		cards = new JPanel(new CardLayout());
		cards.add(selection1);
		cards.add(selection2);
		cards.add(selection3);
		cards.add(selection4);
		cards.add(selection5);
		cards.add(planPanel);

		add(cards);

		getContentPane().setBackground(Color.WHITE);
	}
	
	public void nextStep() {
		CardLayout c = (CardLayout)(cards.getLayout());
		if (currentStep != 6) {
			c.next(cards);
			currentStep++;
		}
		
	}

	public void iterate() {
		// sample code that pulls available modules from the model
		ArrayList<Module> availableModules = model.getAvailableModules();
		System.out.println("Modules Available: (" + availableModules.size() + ")");
		availableModules.forEach(System.out::println);
	}
}
