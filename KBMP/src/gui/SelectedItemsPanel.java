package gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Stack;

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
		setOpaque(false);
	}
	
	public void addItem(String text) {
		while (!removedItems.isEmpty()) {
			SelectedItem toBeRemoved = removedItems.pop();
			remove(toBeRemoved);
		}
		
		SelectedItem item = new SelectedItem(this,text,hasDate);
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
}
