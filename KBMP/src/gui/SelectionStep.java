package gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.swing.*;

import common.AvailableModule;
import common.FocusArea;
import common.Module;

@SuppressWarnings("serial")
public class SelectionStep extends JPanel implements ItemListener {
    private GuiFrame frame;
    private STEP step;
    private ArrayList<AvailableModule> availableModules;
    private ArrayList<FocusArea> availableFocusAreas;
    private JLabel question;
    private JScrollPane selectedScroller;
    private JScrollPane plannedScroller;
    private JTextField textField;
    private JComboBox<String> dropdownList;
    private boolean isAddingOrRemovingItem = false;
    private SelectedItemsPanel selected;
    private int semester;
    private JTextArea planned;
    private JButton next;

    private final SelectionStep.STEP[] STEPS = SelectionStep.STEP.values();

    enum STEP {
        NUM_SEM_LEFT, MOD_TAKEN, MOD_WANT, MOD_DONT_WANT, FOCUS_AREA, PLANNING, DONE
    }

    public SelectionStep(final GuiFrame frame, boolean hasDate) {
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        setBackground(Color.WHITE);

        this.frame = frame;

        question = new JLabel();

        next = new JButton(new AbstractAction("Next") {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean isSuccessful = submit(frame);
                if (isSuccessful && step.ordinal() < STEP.PLANNING.ordinal()) {
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

        selectedScroller = new JScrollPane(selected, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        selectedScroller.getViewport().setPreferredSize(new Dimension(200, 200));
        selectedScroller.setVisible(false);

        planned = new JTextArea();
        planned.setAlignmentX(LEFT_ALIGNMENT);

        plannedScroller = new JScrollPane(planned, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        plannedScroller.getViewport().setPreferredSize(new Dimension(200, 200));
        plannedScroller.setVisible(false);

        dropdownList = new JComboBox<String>();
        dropdownList.setAlignmentX(Component.LEFT_ALIGNMENT);
        dropdownList.setMaximumSize(new Dimension(300, 20));
        dropdownList.addItemListener(this);
        dropdownList.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
        dropdownList.setVisible(false);

        add(question);
        add(textField);
        add(dropdownList);
        add(selectedScroller);
        add(plannedScroller);
        add(next);
    }

    public void init() {
        textField.setText("");
        dropdownList.removeAllItems();

        switch (step) {
            case NUM_SEM_LEFT:
                setQuestion("How many semesters have you left?");
                textField.setVisible(true);
                break;
            case MOD_TAKEN:
                setQuestion("Please select modules that you have already taken.");
                setAllModules(frame.getModel().getModules());
                textField.setVisible(false);
                dropdownList.setVisible(true);
                selectedScroller.setVisible(true);
                break;
            case MOD_WANT:
                setQuestion("Please select modules that you want to take.");
                setAllModules(frame.getModel().getModules());
                break;
            case MOD_DONT_WANT:
                setQuestion("Please select modules that you don't want to take.");
                setAllModules(frame.getModel().getModules());
                break;
            case FOCUS_AREA:
                setQuestion("Please select your focus area.");
                setAvailableFocusAreas(frame.getModel().getAllFocusAreas());
                break;
            case PLANNING:
                if (!frame.getModel().isDone()) {
                    setQuestion("<html>Semester " + frame.getModel().getSemester() + "<br>Select modules for this semester:");
                    setAvailableModules(frame.getModel().getAvailableModules());
                } else {
                    question.setVisible(false);
                    dropdownList.setVisible(false);
                    selectedScroller.setVisible(false);
                    next.setVisible(false);
                }
                plannedScroller.setVisible(true);
                break;
            default:
                System.out.println("Step " + step.name() + " doesn't exist.");
        }

        question.setVisible(true);

        frame.revalidate();
        revalidate();
    }


    private boolean submit(final GuiFrame frame) {
        boolean isSuccessful = true;
        switch (step) {
            case NUM_SEM_LEFT:
                try {
                    int number = Integer.parseInt(textField.getText());
                    if (number < 1 || number > 10) {
                        JOptionPane.showMessageDialog(frame, "Please enter a number between 1 and 10.");
                        isSuccessful = false;

                        return isSuccessful;
                    }

                    frame.getLogic().setNumberOfSemesterLeft(Integer.parseInt(textField.getText()));
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(frame, textField.getText() + " is not a valid number.");
                    isSuccessful = false;

                    return isSuccessful;
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
            case FOCUS_AREA:
                frame.getLogic().assertFocus(getSelectedFocusAreas());
                break;
            case PLANNING:
                ArrayList<Module> modules = getSelectedModules();
                frame.getLogic().selectModules(modules);
                String text = planned.getText();
                text += "Semester " + (frame.getModel().getSemester() - 1) + "\n";
                for (Module module : modules) {
                    text += module.getCode() + " " + module.getName() + "\n";
                }
                text+= "-------------------\n";
                planned.setText(text);
                break;
        }
        selected.clearAllItems();
        revalidate();
        frame.getLogic().iterate();
        init();

        return isSuccessful;
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

    private ArrayList<FocusArea> getSelectedFocusAreas() {
        ArrayList<SelectedItem> fas = selected.getSelectedItems();
        ArrayList<FocusArea> returnList = new ArrayList<FocusArea>();
        for (SelectedItem fa : fas) {
            returnList.add(fa.getFocusArea());
        }
        return returnList;
    }

    public void setQuestion(String question) {
        this.question.setText(question);
        this.question.setForeground(Color.BLACK);
    }

    private void setAllModules(ArrayList<Module> modules) {
        for (Module module : modules) {
            insertItem(module.getCode() + " " + module.getName());
        }
    }

    private void setAvailableModules(ArrayList<AvailableModule> availableModules) {
        this.availableModules = availableModules;
        for (AvailableModule availableModule : availableModules) {
            insertItem(availableModule.getModule().getCode() + " " + availableModule.getModule().getName() + " (" + availableModule.getScore() + ")");
        }
    }

    private void setAvailableFocusAreas(ArrayList<FocusArea> focusAreas) {
        availableFocusAreas = focusAreas;
        for (FocusArea fa : focusAreas) {
            insertItem(fa.getName());
        }
    }
    public void insertItem(String item) {
        isAddingOrRemovingItem = true;

        dropdownList.addItem(item);

        dropdownList.setSelectedItem(null);
        dropdownList.revalidate();
        selectedScroller.revalidate();

        isAddingOrRemovingItem = false;
    }

    @Override
    public void itemStateChanged(ItemEvent event) {
        if (isAddingOrRemovingItem == true) {
            return;
        }

        if (event.getStateChange() == ItemEvent.SELECTED ) {
            if (step == STEP.FOCUS_AREA) {
                String focusArea = event.getItem().toString();
                for (FocusArea item : availableFocusAreas) {
                    if(item.getName().compareTo(focusArea) == 0) {
                        selected.addItem(item);
                        break;
                    }
                }
            }
            Module module;
            for (AvailableModule availableModule : availableModules) {
                module = availableModule.getModule();
                String moduleCode = event.getItem().toString().split(" ")[0];
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

        selectedScroller.revalidate();
    }
}
