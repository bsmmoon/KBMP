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

public class SelectedItem extends JPanel {
	private SelectedItemsPanel panel;
	private SelectedItem item;
	private JLabel label;
	private JComboBox<String> semester;
	private JButton remove;
	
	private String[] semesters = {"optional","2016/2017, Semester 1", "2016/2017, Semester 2"};
	
	public SelectedItem(final SelectedItemsPanel panel, String text, boolean hasDate) {
		item = this;
		setName(text);
		this.panel = panel;
		
		//setLayout(new FlowLayout(FlowLayout.LEFT));
		this.setOpaque(false);
		
		label = new JLabel(text);
		label.setToolTipText("hello");
		add(label);
		
		if (hasDate) {
			semester = new JComboBox<String>(semesters);
			semester.setAlignmentX(Component.LEFT_ALIGNMENT);
			semester.setMaximumSize(new Dimension(100,20));
			add(semester);
		}
		remove = new JButton(new AbstractAction("x"){
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
		setPreferredSize(getPreferredSize());
	}
	
	public void paint(Graphics g) {
		Point pos = label.getLocation();
		pos.x -= 5;
		int width = label.getWidth() + remove.getWidth() + 12;
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
}
