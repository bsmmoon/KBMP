package gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class SelectionPanel extends JPanel {
	private JLabel question;
	JComboBox dropdownList;
	JButton next;
	
	public SelectionPanel(GuiFrame frame) {
		question = new JLabel();
		dropdownList = new JComboBox();
		dropdownList.setEditable(true);
		
		next = new JButton(new AbstractAction("Next"){
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.nextStep();
			}
		});
		
		setLayout(new BoxLayout(this,BoxLayout.PAGE_AXIS));
		add(question);
		dropdownList.setAlignmentX(Component.LEFT_ALIGNMENT);
		dropdownList.setMaximumSize(new Dimension(300,20));
		add(dropdownList);
		add(next);
		setAlignmentX(Component.LEFT_ALIGNMENT);
		
		setBackground(Color.WHITE);
	}
	public void setQuestion(String question) {
		this.question.setText(question);
		this.question.setForeground(Color.BLACK);
	}
}
