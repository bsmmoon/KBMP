package gui;

import java.awt.*;
import java.util.ArrayList;
import java.util.Stack;

import common.FocusArea;
import common.Module;
import javax.swing.JPanel;

public class SelectedItemsPanel extends JPanel {
	private SelectionStep selectionStep;
	private boolean hasDate;
	private ArrayList<SelectedItem> selectedItems;
	private Stack<SelectedItem> removedItems;
	
	public SelectedItemsPanel(final SelectionStep selectionStep, final GuiFrame frame, boolean hasDate) {
		this.selectionStep = selectionStep;
		selectedItems = new ArrayList<SelectedItem>();
		removedItems = new Stack<SelectedItem> ();
		this.hasDate = hasDate;
		setLayout(new WrapLayout(WrapLayout.LEFT));
		//setOpaque(false);
	}

	public ArrayList<SelectedItem> getSelectedItems() {
		return selectedItems;
	}

	public void addItem(Module module) {
		SelectedItem item = new SelectedItem(this,module,hasDate);
		addItem(item);
	}

	public void addItem(FocusArea focusArea) {
		SelectedItem item = new SelectedItem(this,focusArea,hasDate);
		addItem(item);
	}

	private void addItem(SelectedItem item) {
		while (!removedItems.isEmpty()) {
			SelectedItem toBeRemoved = removedItems.pop();
			remove(toBeRemoved);
		}
		selectedItems.add(item);

		item.setAlignmentX(LEFT_ALIGNMENT);
		add(item);
		setPreferredSize(getLayout().preferredLayoutSize(this));

		validate();
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
