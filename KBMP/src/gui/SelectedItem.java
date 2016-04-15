package gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.Map;

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

		Iterator<Map.Entry<Module.WorkloadTypes,Float>> workload = module.getWorkload().entrySet().iterator();
		String workloadSummary = "";
		String workloadInfo = module.getTooltip();
		while (workload.hasNext()) {
			Map.Entry<Module.WorkloadTypes,Float> entry = workload.next();
			workloadSummary += Math.round(entry.getValue()) + " ";
			//workloadInfo += entry.getKey() + " ";
		}

		String module_info = "<html>" + module.getCode() + " " + module.getName() +
				"<br>" + module.getCredits() + "MC Workload: " + workloadSummary;
		label = new JLabel(module_info);
		label.setToolTipText(workloadInfo);

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
		pos.y -= 4; // margin
		int width = label.getWidth() + 10;
		if (isEditable) {
			width += remove.getWidth();
		}
		int height = label.getHeight();
		if (semester != null) {
			width += semester.getWidth() + 5;
			height = semester.getHeight();
			pos.y = semester.getLocation().y;
		}

		Color color = Color.white;
		if (isModule) {
			switch (module.getType()) {
				case FOUNDATION:
					color = Color.pink;
					break;
				case BREADTH_AND_DEPTH:
					color = new Color(201, 255, 255); // cyan but less painful to eye
					break;
				case SOFTWARE_ENG_1617_PROJECT:
                    color = Color.orange;
                    break;
				case THEMATIC_SYSTEMS_PROJECT:
                    color = Color.orange;
                    break;
				case MEDIA_TECH_PROJECT:
                    color = Color.orange;
                    break;
				case SOFTWARE_ENG_PROJECT:
                    color = Color.orange;
                    break;
				case OTHER_REQUIRED:
                    color = Color.MAGENTA;
                    break;
                default:
                    color = Color.white;
			}
		}
		g.setColor(color);
		height = height + 8; // margin
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
