package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import common.Module;

public class ModulePanel extends JPanel{
	JLabel moduleTitle;
	JLabel moduleDescription;
	JButton removeButton;
	
	public ModulePanel(Module module) {
		setLayout(new BorderLayout());
		
		moduleTitle = new JLabel(module.getCode() + " " + module.getName());
		add(moduleTitle,BorderLayout.LINE_START);
		
		removeButton = new JButton("x");
		removeButton.setOpaque(false);
		removeButton.setContentAreaFilled(false);
		removeButton.setBorderPainted(false);
		removeButton.setMargin(new Insets(0,0,0,0));
		removeButton.setVisible(false);
		add(removeButton,BorderLayout.LINE_END);
		
		moduleDescription = new JLabel("...");
		//moduleDescription.setPreferredSize(new Dimension(200,100));
		add(moduleDescription,BorderLayout.PAGE_END);
	}
	
	public void setEditable() {
		removeButton.setVisible(true);
		validate();
	}
}
