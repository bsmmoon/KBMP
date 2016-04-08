package gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.swing.*;

import common.Module;

@SuppressWarnings("serial")
public class SelectionStep extends JPanel implements ItemListener {
    private GuiFrame frame;
    private STEP step;
    private ArrayList<Module> availableModules;
    private JLabel question;
    private JScrollPane scroller;
    private JTextField textField;
    private JComboBox<String> dropdownList;
    private boolean isAddingOrRemovingItem = false;
    private SelectedItemsPanel selected;
    private JButton next;

    private final SelectionStep.STEP[] STEPS = SelectionStep.STEP.values();

    enum STEP {
        NUM_SEM_LEFT, MOD_TAKEN, MOD_WANT, MOD_DONT_WANT, FOCUS_AREA, PLANNING
    }

    public SelectionStep(final GuiFrame frame, boolean hasDate) {
        this.frame = frame;

        question = new JLabel();

        next = new JButton(new AbstractAction("Next") {
            @Override
            public void actionPerformed(ActionEvent e) {
                submit(frame);
                if (step.ordinal() < STEPS.length - 1) {
                    step = STEPS[step.ordinal()+1];
                    init();
                }
            }
        });

        setLocation(20, 20);
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        textField = new JTextField(20);
        textField.setMaximumSize(new Dimension(50, 20));
        textField.setAlignmentX(Component.LEFT_ALIGNMENT);
        textField.setVisible(false);

        selected = new SelectedItemsPanel(this, frame, hasDate);
        selected.setAlignmentX(LEFT_ALIGNMENT);
        selected.setVisible(false);

        scroller = new JScrollPane(selected, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroller.getViewport().setPreferredSize(new Dimension(200, 200));

        dropdownList = new JComboBox<String>();
        dropdownList.setAlignmentX(Component.LEFT_ALIGNMENT);
        dropdownList.setMaximumSize(new Dimension(300, 20));
        dropdownList.addItemListener(this);
        dropdownList.setVisible(false);

        add(question);
        add(textField);
        add(dropdownList);
        add(scroller);
        add(next);

        setAlignmentX(Component.LEFT_ALIGNMENT);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        setBackground(Color.WHITE);
    }

    public void init() {
        switch (step) {
            case NUM_SEM_LEFT:
                setQuestion("How many semesters have you left?");
                textField.setVisible(true);
                break;
            case MOD_TAKEN:
                setQuestion("Please select modules that you have already taken.");
                setDropdownItems(frame.getModel().getAvailableModules());
                textField.setVisible(false);
                dropdownList.setVisible(true);
                selected.setVisible(true);
                break;
            case MOD_WANT:
                setQuestion("Please select modules that you want to take.");
                setDropdownItems(frame.getModel().getAvailableModules());
                break;
            case MOD_DONT_WANT:
                setQuestion("Please select modules that you don't want to take.");
                setDropdownItems(frame.getModel().getAvailableModules());
                break;
            case FOCUS_AREA:
                setQuestion("Please select your focus area.");
                break;
            case PLANNING:
                setQuestion("Please select the modules you want to take from the list.");
                setDropdownItems(frame.getModel().getAvailableModules());
                break;
            default:
                System.out.println("Step " + step.name() + " doesn't exist.");
        }

        question.setVisible(true);

        frame.revalidate();
        revalidate();
    }

    private void submit(final GuiFrame frame) {
        switch (step) {
            case NUM_SEM_LEFT:
                try {
                    frame.getLogic().setNumberOfSemesterLeft(Integer.parseInt(textField.getText()));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                break;
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
        frame.getLogic().iterate();
    }

    public void clearSelectedArea() {

    }
    public  void setStep(STEP step) { this.step = step; }

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

        for (int i = 0; i < dropdownList.getItemCount(); i++) {
            if (added) break;

            switch (dropdownList.getItemAt(i).compareTo(item)) {
                case -1:    // item to be inserted is after the current item
                    added = false;
                    break;
                case 0:        // item to be inserted = current item
                    added = true;
                    break;
                case 1:        // item to be inserted is before the current item
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
            for (Module module : availableModules) {
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
