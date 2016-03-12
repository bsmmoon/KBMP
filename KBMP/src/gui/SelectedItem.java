package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

public class SelectedItem extends JPanel {
	private SelectedItemsPanel panel;
	private SelectedItem item;
	private JLabel label;
	private JButton remove;
	
	public SelectedItem(SelectedItemsPanel panel, String text, boolean hasDate) {
		item = this;
		setName(text);
		this.panel = panel;
		
		//setLayout(new FlowLayout(FlowLayout.LEFT));
		this.setOpaque(false);
		
		label = new JLabel(text);
	
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
		
		add(label);
		add(remove);
		setPreferredSize(getPreferredSize());
	}
	public void paint(Graphics g) {
		Point pos = label.getLocation();
		int width = label.getWidth() + remove.getWidth() + 12;
		int height = 20;
		g.setColor(Color.lightGray);
		g.fillRoundRect(pos.x-5, pos.y-1, width, height, 10, 10);
		
		super.paint(g);
	}
}
