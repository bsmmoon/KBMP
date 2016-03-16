package gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class GuiFrame extends JFrame {
	private JPanel cards;	//a panel that uses CardLayout
	private int currentStep = 1;
	String[] selections = {"CS1101","CS2101","CS2102","CS3342"};
	
	public GuiFrame(String title) {
		super(title);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		setSize(500,500);
		
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

		cards = new JPanel(new CardLayout());
		cards.add(selection1);
		cards.add(selection2);
		cards.add(selection3);
		cards.add(selection4);
		cards.add(selection5);
		
		add(cards,BorderLayout.CENTER);
		
		getContentPane().setBackground(Color.WHITE);
	}
	
	public void nextStep() {
		CardLayout c = (CardLayout)(cards.getLayout());
		if (currentStep != 5) {
			c.next(cards);
			currentStep++;
		}
		
	}
}
