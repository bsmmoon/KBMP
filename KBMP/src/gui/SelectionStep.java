package gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import common.Module;

@SuppressWarnings("serial")
public class SelectionStep extends JPanel implements ItemListener {
	private STEP step;
    private ArrayList<Module> availableModules;
	private JLabel question;
	private JScrollPane scroller;
	private JComboBox<String> dropdownList;
	private boolean isAddingOrRemovingItem = false;
	private SelectedItemsPanel selected;
	private JButton next;
	
	enum STEP {
		NUM_SEM_LEFT, MOD_TAKEN, MOD_WANT, MOD_DONT_WANT
	}

	public SelectionStep(final GuiFrame frame, STEP step, boolean hasDate) {
		this.step = step;

		question = new JLabel();
		dropdownList = new JComboBox<String>();
		//dropdownList.setEditable(true);
		
		selected = new SelectedItemsPanel(this, frame, hasDate);
		scroller = new JScrollPane(selected,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroller.getViewport().setPreferredSize(new Dimension(200,200));
		next = new JButton(new AbstractAction("Next"){
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.nextStep();
				submit(frame);
			}
		});
		
		setLocation(20, 20);
		setLayout(new BoxLayout(this,BoxLayout.PAGE_AXIS));
		add(question);
		dropdownList.setAlignmentX(Component.LEFT_ALIGNMENT);
		dropdownList.setMaximumSize(new Dimension(300,20));
		dropdownList.addItemListener(this);
		add(dropdownList);
		selected.setAlignmentX(LEFT_ALIGNMENT);
		add(scroller);
		add(next);
		
		setAlignmentX(Component.LEFT_ALIGNMENT);
		setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		
		setBackground(Color.WHITE);
	}

	public void init() {
		switch (step) {
			case NUM_SEM_LEFT:
				setQuestion("How many semesters have you left?");
				break;
			case MOD_TAKEN:
				setQuestion("Please select modules that you have already taken.");
				break;
			case MOD_WANT:
				setQuestion("Please select modules that you want to take.");
				break;
			case MOD_DONT_WANT:
				setQuestion("Please select modules that you don't want to take.");
				break;
			default:
				System.out.println("Step " + step.name() + " doesn't exist.");
		}
	}
	private void submit(final GuiFrame frame) {
		switch (step) {
			case MOD_TAKEN:
				frame.getLogic().assertTaken(getSelectedModules());
				break;
			case MOD_WANT:
				frame.getLogic().assertWant(getSelectedModules());
				break;
			case MOD_DONT_WANT:
				frame.getLogic().assertDontWant(getSelectedModules());
				break;

		}
	}

	public STEP getStep() {
		return step;
	}

	private ArrayList<Module> getSelectedModules() {
		ArrayList<SelectedItem> modules = selected.getSelectedItems();
		ArrayList<Module> returnList = new ArrayList<Module>();
		for (SelectedItem module : modules) {
			returnList.add(module.getModule());
		}
		return returnList;
	}

	public void setQuestion(String question) {
		this.question.setText(question);
		this.question.setForeground(Color.BLACK);
	}
	
	public void setDropdownItems(ArrayList<Module> modules) {
		availableModules = modules;
		for (Module module : modules) {
			insertItem(module.getCode() + " " + module.getName());
		}
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
			String moduleCode = event.getItem().toString().split(" ")[0];
			for (Module module: availableModules) {
				if (module.getCode().compareTo(moduleCode) == 0) {
					selected.addItem(module);
					break;
				}
			}
			isAddingOrRemovingItem = true;
			dropdownList.removeItem(event.getItem());
			dropdownList.setSelectedItem(null);
			isAddingOrRemovingItem = false;
			dropdownList.revalidate();
		}

		scroller.revalidate();
	}
}
