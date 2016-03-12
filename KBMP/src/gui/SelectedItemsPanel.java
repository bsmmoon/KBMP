package gui;

import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Stack;

import javax.swing.JPanel;

public class SelectedItemsPanel extends JPanel {
	private ArrayList<SelectedItem> selectedItems;
	private Stack<SelectedItem> removedItems;
	
	public SelectedItemsPanel() {
		selectedItems = new ArrayList<SelectedItem>();
		removedItems = new Stack<SelectedItem> ();
		
		setLayout(new FlowLayout(FlowLayout.LEFT));
		setOpaque(false);
	}
	
	public void addItem(String text) {
		while (!removedItems.isEmpty()) {
			SelectedItem toBeRemoved = removedItems.pop();
			remove(toBeRemoved);
		}
		
		SelectedItem item = new SelectedItem(this,text,false);
		selectedItems.add(item);
		
		item.setAlignmentX(LEFT_ALIGNMENT);
		add(item);
		setPreferredSize(getPreferredSize());
		
		validate();
	}
	
	public void removeItem(SelectedItem item) {
		selectedItems.remove(item);
		removedItems.push(item);
	}
}
