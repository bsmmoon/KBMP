package gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import common.FocusArea;
import common.Module;
import common.Semester;

public class SelectedItem extends JPanel {
	boolean isEditable = true;
	private boolean isModule;
	private Module module;
	private FocusArea focusArea;
	private SelectedItemsPanel panel;
	private SelectedItem item;
	private JLabel label;
	private JComboBox<String> semester;
	private JButton remove;
	
	private String[] semesters = {"optional","2016/2017, Semester 1", "2016/2017, Semester 2"};

	public SelectedItem(final SelectedItemsPanel panel, Module module, boolean isEditable) {
		isModule = true;
		this.isEditable = isEditable;
		this.module = module;
		String module_info = "<html>" + module.getCode() + " " + module.getName() +
				"<br>Credit: " + module.getCredits() +
				"<br>Workload: " + module.getWorkload();
		label = new JLabel(module_info);
		//label.setToolTipText("hello");

		init(panel);
	}

	public SelectedItem(final SelectedItemsPanel panel, FocusArea focusArea) {
		isModule = false;
		this.focusArea = focusArea;
		label = new JLabel(focusArea.getName());

		init(panel);
	}

	public SelectedItem(final SelectedItemsPanel panel, Semester semester) {
		isModule = true;
		this.isEditable = false;
		this.label = new JLabel(semester.getSummary());
		init(panel);
	}

	public void init(final SelectedItemsPanel panel) {
		item = this;
		this.panel = panel;
		this.setOpaque(false);

		add(label);
		if (isEditable) {
			remove = new JButton(new AbstractAction("x") {
				@Override
				public void actionPerformed(ActionEvent e) {
					item.setVisible(false);
					panel.removeItem(item);
					validate();
				}
			});
			remove.setOpaque(false);
			remove.setContentAreaFilled(false);
			remove.setBorderPainted(false);
			remove.setMargin(new Insets(0,0,0,0));

			add(remove);
		}
		setMaximumSize(getPreferredSize());
	}

	public Module getModule() {
		return module;
	}

	public FocusArea getFocusArea() {
		return focusArea;
	}

	public void disableRemoveButton() {
		remove.setVisible(false);
		revalidate();
	}

	public void paint(Graphics g) {
		Point pos = label.getLocation();
		pos.x -= 5;
		int width = label.getWidth() + 12 ;
		if (isEditable) {
			width += remove.getWidth();
		}
		int height = label.getHeight();
		if (semester != null) {
			width += semester.getWidth() + 5;
			height = semester.getHeight();
			pos.y = semester.getLocation().y;
		}
		g.setColor(Color.lightGray);
		g.fillRoundRect(pos.x, pos.y, width, height, 10, 10);
		
		super.paint(g);
	}
	
	@Override
	public String toString() {
		if (isModule) {
			return module.getCode();
		} else {
			return focusArea.getName();
		}
	}
}
