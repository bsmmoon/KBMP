package gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class GuiFrame extends JFrame {
	private JPanel cards;	//a panel that uses CardLayout
	private int currentStep = 1;
	public GuiFrame(String title) {
		super(title);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		setSize(500,500);
		
		SelectionPanel selection1 = new SelectionPanel(this);
		selection1.setQuestion("Please select modules that you have already taken.");
		
		SelectionPanel selection2 = new SelectionPanel(this);
		selection2.setQuestion("Please select modules that you want to take.");
		
		SelectionPanel selection3 = new SelectionPanel(this);
		selection3.setQuestion("Please select modules that you don't want to take.");
		
		SelectionPanel selection4 = new SelectionPanel(this);
		selection4.setQuestion("Please select your focus area.");
		
		SelectionPanel selection5 = new SelectionPanel(this);
		selection5.setQuestion("Please select your program.");
		
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
