package gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class SelectionStep extends JPanel implements ItemListener {
	private JLabel question;
	JComboBox<String> dropdownList;
	SelectedItemsPanel selected;
	JButton next;
	
	private GuiFrame frame;
	
	public SelectionStep(final GuiFrame frame, boolean hasDate) {
		this.frame = frame;
		question = new JLabel();
		dropdownList = new JComboBox<String>();
		//dropdownList.setEditable(true);
		
		selected = new SelectedItemsPanel(hasDate);
		
		next = new JButton(new AbstractAction("Next"){
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.nextStep();
			}
		});
		
		setLocation(20, 20);
		setLayout(new BoxLayout(this,BoxLayout.PAGE_AXIS));
		add(question);
		dropdownList.setAlignmentX(Component.LEFT_ALIGNMENT);
		dropdownList.setMaximumSize(new Dimension(300,20));
		add(dropdownList);
		selected.setAlignmentX(LEFT_ALIGNMENT);
		add(selected);
		add(next);
		
		setAlignmentX(Component.LEFT_ALIGNMENT);
		setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		
		setBackground(Color.WHITE);
	}
	
	public void setQuestion(String question) {
		this.question.setText(question);
		this.question.setForeground(Color.BLACK);
	}
	
	public void setDropdownItems(String[] list) {
		for (String item : list) {
			dropdownList.addItem(item);
		}
		dropdownList.setSelectedItem(null);
		dropdownList.addItemListener(this);
	}
	
	@Override
	public void itemStateChanged(ItemEvent event) {
		if (event.getStateChange() == ItemEvent.SELECTED) {
			selected.addItem(event.getItem().toString());
		}
	}
}
