package gui;

import java.awt.*;
import java.util.ArrayList;
import java.util.Stack;

import common.FocusArea;
import common.Module;
import common.Semester;

import javax.swing.*;

public class SelectedItemsPanel extends JPanel {
	private boolean isEditable;
	private SelectionStep selectionStep;
	private ArrayList<SelectedItem> selectedItems;
	private Stack<SelectedItem> removedItems;
	
	public SelectedItemsPanel(final SelectionStep selectionStep, final GuiFrame frame) {
		this.selectionStep = selectionStep;
		selectedItems = new ArrayList<SelectedItem>();
		removedItems = new Stack<SelectedItem> ();
		this.isEditable = isEditable;
		setLayout(new WrapLayout(WrapLayout.LEFT));
		//setLayout(new BorderLayout());
		//setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		//setAlignmentX(LEFT_ALIGNMENT);
		//setOpaque(false);
	}

	public ArrayList<SelectedItem> getSelectedItems() {
		return selectedItems;
	}

	public void addItem(Module module,boolean isEditable) {
		SelectedItem item = new SelectedItem(this,module,isEditable);
		addItem(item);
	}

	public void addSemester(Semester sem) {
		add(new SemesterItem(sem));
		validate();
	}

	public void addLabel(String text) {
		JLabel label = new JLabel(text);
		label.setAlignmentX(LEFT_ALIGNMENT);
		label.setPreferredSize(new Dimension(getParent().getWidth()-20,20));
		add(label);
		setPreferredSize(getLayout().preferredLayoutSize(this));
		validate();
	}
	public void addItem(FocusArea focusArea) {
		SelectedItem item = new SelectedItem(this,focusArea);
		addItem(item);
	}

	public void addItem(SelectedItem item) {
		while (!removedItems.isEmpty()) {
			SelectedItem toBeRemoved = removedItems.pop();
			remove(toBeRemoved);
		}
		selectedItems.add(item);

		item.setAlignmentX(LEFT_ALIGNMENT);
		add(item);
		setPreferredSize(getLayout().preferredLayoutSize(this));
		setAlignmentX(LEFT_ALIGNMENT);

		validate();
	}

	public void addItem(Semester semester) {
		SelectedItem item = new SelectedItem(this, semester);
		addItem(item);
	}

	public void removeItem(SelectedItem item) {
		selectedItems.remove(item);
		removedItems.push(item);
		selectionStep.insertItem(item.toString());
	}

	public void clearAllItems() {
		while (!selectedItems.isEmpty()) {
			SelectedItem item = selectedItems.remove(0);
			item.setVisible(false);
			remove(item);
		}
		removedItems.clear();
	}
}
