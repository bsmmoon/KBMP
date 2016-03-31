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
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

@SuppressWarnings("serial")
public class SelectionStep extends JPanel implements ItemListener {
	private JLabel question;
	private JScrollPane scroller;
	private JComboBox<String> dropdownList;
	private boolean isAddingOrRemovingItem = false;
	private SelectedItemsPanel selected;
	private JButton next;
	
	
	public SelectionStep(final GuiFrame frame, boolean hasDate) {
		question = new JLabel();
		dropdownList = new JComboBox<String>();
		//dropdownList.setEditable(true);
		
		selected = new SelectedItemsPanel(this, frame, hasDate);
		scroller = new JScrollPane(selected,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroller.getViewport().setPreferredSize(new Dimension(200,200));
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
		add(scroller);
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
		//dropdownList.addListDataListener(this);
		dropdownList.addItemListener(this);
	}
	
	public void insertItem(String item) {
		isAddingOrRemovingItem = true;
		boolean added = false;
		
		for (int i=0; i<dropdownList.getItemCount(); i++) {
			if (added) break;
			
			switch (dropdownList.getItemAt(i).compareTo(item)) {
			case -1:	// item to be inserted is after the current item
				added = false;
				break;
			case 0:		// item to be inserted = current item
				added = true;
				break;
			case 1:		// item to be inserted is before the current item
				dropdownList.insertItemAt(item, i);
				added = true;
				break;
			}
		}
		
		if (!added) {
			dropdownList.addItem(item);
		}
		
		dropdownList.setSelectedItem(null);
		dropdownList.revalidate();
		scroller.revalidate();
		
		isAddingOrRemovingItem = false;
	}
	
	@Override
	public void itemStateChanged(ItemEvent event) {
		if (isAddingOrRemovingItem == true) {
			return;
		}
		
		if (event.getStateChange() == ItemEvent.SELECTED) {
			selected.addItem(event.getItem().toString());
			isAddingOrRemovingItem = true;
			dropdownList.removeItem(event.getItem());
			dropdownList.setSelectedItem(null);
			isAddingOrRemovingItem = false;
			dropdownList.revalidate();
		}

		scroller.revalidate();
	}
}
