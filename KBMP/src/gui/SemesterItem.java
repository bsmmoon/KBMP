package gui;

import common.FocusArea;
import common.Module;
import common.Semester;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class SemesterItem extends JPanel {
	private JLabel semester;
	//private ArrayList<JLabel> moduleCodes;

	public SemesterItem(Semester sem) {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		setOpaque(false);
		//semester = new JLabel(sem.getName());
		semester = new JLabel(sem.getName());
		add(semester);

		ArrayList<Module> modules = sem.getModules();
		for (Module mod : modules) {
			JLabel moduleCode = new JLabel(mod.getCode());
			moduleCode.setToolTipText(mod.getName());
			add(moduleCode);
		}
		add(new JLabel(sem.getSummary()));
		setPreferredSize(new Dimension(200,170));
	}

	public void paint(Graphics g) {
		g.setColor(Color.lightGray);
		g.fillRoundRect(semester.getX()-10, semester.getY()-10, 250, getHeight()+10, 10, 10);

		super.paint(g);
	}
}