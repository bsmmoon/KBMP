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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import common.AvailableModule;
import common.FocusArea;
import common.Module;
import common.Semester;

@SuppressWarnings("serial")
public class SelectionStep extends JPanel {
    private GuiFrame frame;
    private STEP step;
    private JScrollPane preplanScroller;
    private PrePlanPanel preplanInfo;
    private ArrayList<Module> preplanModules;
    private ArrayList<AvailableModule> availableModules;
    private ArrayList<FocusArea> availableFocusAreas;
    private JLabel question;
    private JScrollPane selectedScroller;
    private JScrollPane plannedScroller;
    private JComboBox<String> dropdownList;
    private boolean isAddingOrRemovingItem = false;
    private SelectedItemsPanel selected;
    //private JTextArea planned;
    private SelectedItemsPanel planned;
    private JButton next;

    private final SelectionStep.STEP[] STEPS = SelectionStep.STEP.values();

    enum STEP {
        PRE_PLAN, MOD_TAKEN, MOD_WANT, MOD_DONT_WANT, FOCUS_AREA, PLANNING, DONE
    }

    public SelectionStep(final GuiFrame frame, boolean hasDate) {
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

        next.setAlignmentX(LEFT_ALIGNMENT);

        preplanInfo = new PrePlanPanel(frame);
        //preplanInfo.setAlignmentX(LEFT_ALIGNMENT);

        preplanScroller = new JScrollPane(preplanInfo, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        preplanScroller.getViewport().setPreferredSize(new Dimension(200, 200));
        preplanScroller.setAlignmentX(LEFT_ALIGNMENT);

        selected = new SelectedItemsPanel(this, frame);
        //selected.setAlignmentX(LEFT_ALIGNMENT);

        selectedScroller = new JScrollPane(selected, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        selectedScroller.getViewport().setPreferredSize(new Dimension(200, 200));
        selectedScroller.setVisible(false);
        selectedScroller.setAlignmentX(LEFT_ALIGNMENT);

        planned = new SelectedItemsPanel(this, frame);
        planned.setAlignmentX(LEFT_ALIGNMENT);

        plannedScroller = new JScrollPane(planned, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        plannedScroller.getViewport().setPreferredSize(new Dimension(200, 200));
        plannedScroller.setVisible(false);
        plannedScroller.setAlignmentX(LEFT_ALIGNMENT);

        setupDropdownList();

        add(question);
        add(preplanScroller);
        add(dropdownList);
        add(selectedScroller);
        add(plannedScroller);
        add(next);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    public void init() {
        dropdownList.removeAllItems();

        switch (step) {
            case PRE_PLAN:

                break;
            case MOD_TAKEN:
                preplanScroller.setVisible(false);
                setQuestion("Please select modules that you have already taken.");
                setAllModules(frame.getModel().getPreplanModules());
                dropdownList.setVisible(true);
                selectedScroller.setVisible(true);
                break;
            case MOD_WANT:
                setQuestion("Please select modules that you want to take.");
                setAllModules(frame.getModel().getPreplanModules());
                break;
            case MOD_DONT_WANT:
                setQuestion("Please select modules that you don't want to take.");
                setAllModules(frame.getModel().getPreplanModules());
                break;
            case FOCUS_AREA:
                setQuestion("Please select your focus area.");
                setAvailableFocusAreas(frame.getModel().getAllFocusAreas());
                break;
            case PLANNING:
                if (!frame.getModel().isDone()) {
                    setQuestion("<html>Year " + frame.getModel().getYear() + " Semester " + frame.getModel().getSemester() + "<br>Select modules for this semester:");
                    if (frame.getLogic().isSkipSemester()) {
                        ArrayList<AvailableModule> availableModules = frame.getLogic().getSkipModules();
                        setAvailableModules(availableModules);
                        setRecommendation(availableModules);
                    }
                    else {
                        setAvailableModules(frame.getModel().getAvailableModules());
                        setRecommendation(frame.getModel().getRecommendedModules());
                    }
                } else {
                    question.setText("Complete Plan");
                    dropdownList.setVisible(false);
                    selectedScroller.setVisible(false);
                    next.setVisible(false);
                }
                plannedScroller.setVisible(true);
                break;
            default:
                System.out.println("Step " + step.name() + " doesn't exist.");
        }

        //question.setVisible(true);
        dropdownList.setMaximumSize(dropdownList.getPreferredSize());
        frame.revalidate();
        revalidate();
    }

    private void setRecommendation(ArrayList<AvailableModule> modules) {
        for (AvailableModule module : modules) {
            selected.addItem(module.getModule(),true);
            dropdownList.removeItem(module.getModule().getCode() + " " + module.getModule().getName() + " (" + module.getScore() + ")");
        }
    }

    private boolean submit(final GuiFrame frame) {
        boolean isSuccessful = true;
        switch (step) {
            case PRE_PLAN:
                preplanInfo.submit();
                /*
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
                */
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
                ArrayList<FocusArea> focusAreas = getSelectedFocusAreas();
                frame.getLogic().assertFocus(focusAreas);
                break;
            case PLANNING:
                if (frame.getLogic().isSkipSemester()) {
                    ArrayList<Module> modules = getSelectedModules();
                    frame.getLogic().selectModules(modules);
                    Semester semester = frame.getModel().getModulePlan().getSemester(frame.getModel().getCumulativeSemester());
                    frame.getLogic().confirmSemester();
                    planned.addSemester(semester);
                    break;
                }

                ArrayList<Module> modules = getSelectedModules();
                frame.getLogic().selectModules(modules);
                Semester semester = frame.getModel().getModulePlan().getSemester(frame.getModel().getCumulativeSemester());
                if (semester.getCredits() < 16) {
                    JOptionPane.showMessageDialog(this,"The total credits are less than 16 MC!");
                    isSuccessful = false;
                } else {
                    frame.getLogic().confirmSemester();
                    //planned.addLabel(semester.getName());
                    //for (Module module : modules) {
                    //    planned.addItem(module,false);
                   // }
                    //planned.addItem(semester);
                    planned.addSemester(semester);
                }
/*
                String text = planned.getText();
                text += "Semester " + (frame.getModel().getSemester() - 1) + "\n";
                for (Module module : modules) {
                    text += module.getCode() + " " + module.getName() + "\n";
                }
                text+= "-------------------\n";
                planned.setText(text);
                */
                break;
        }

        if (isSuccessful) {
            selected.clearAllItems();
            revalidate();
            frame.getLogic().iterate();
            init();
        }

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

    private void setAllModules(ArrayList<Module> preplanModules) {
        isAddingOrRemovingItem = true;
        this.preplanModules = preplanModules;
        for (Module module : preplanModules) {
            dropdownList.addItem(module.getCode() + " " + module.getName());
        }
        isAddingOrRemovingItem = false;
        dropdownList.setSelectedItem(null);
    }

    private void setAvailableModules(ArrayList<AvailableModule> availableModules) {
        isAddingOrRemovingItem = true;
        this.availableModules = availableModules;
        for (AvailableModule availableModule : availableModules) {
            dropdownList.addItem(availableModule.getModule().getCode() + " " + availableModule.getModule().getName() + " (" + availableModule.getScore() + ")");
        }
        isAddingOrRemovingItem = false;
        dropdownList.setSelectedItem(null);
    }

    private void setAvailableFocusAreas(ArrayList<FocusArea> focusAreas) {
        isAddingOrRemovingItem = true;
        availableFocusAreas = focusAreas;

        for (FocusArea fa : focusAreas) {
            dropdownList.addItem(fa.getName());
        }
        isAddingOrRemovingItem = false;
        dropdownList.setSelectedItem(null);
    }

    public void insertItem(String item) {
        isAddingOrRemovingItem = true;

        if (step == STEP.PLANNING) {
            for (int i=0; i<availableModules.size(); i++) {
                AvailableModule module = availableModules.get(i);
                if (module.getCode().compareTo(item) == 0) {
                    dropdownList.insertItemAt(module.getModule().getCode() + " " + module.getModule().getName() + " (" + module.getScore() + ")",i);
                    break;
                }
            }
        } else {
            for (int i=0; i<preplanModules.size(); i++) {
                Module module = preplanModules.get(i);
                if (module.getCode().compareTo(item) == 0) {
                    dropdownList.insertItemAt(module.getCode() + " " + module.getName(), i);
                    break;
                }
            }
        }

        dropdownList.setSelectedItem(null);
        dropdownList.revalidate();
        selectedScroller.revalidate();

        isAddingOrRemovingItem = false;
    }
    private void setupDropdownList() {
        dropdownList = new JComboBox<String>();
        dropdownList.setAlignmentX(Component.LEFT_ALIGNMENT);
        dropdownList.setMaximumSize(new Dimension(300, 20));
        //dropdownList.addItemListener(this);
        dropdownList.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
        dropdownList.setVisible(false);
        dropdownList.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (isAddingOrRemovingItem == true) {
                    return;
                }

                if (e.getStateChange() == ItemEvent.SELECTED ) {
                    if (step == STEP.FOCUS_AREA) {
                        String focusArea = e.getItem().toString();
                        for (FocusArea item : availableFocusAreas) {
                            if(item.getName().compareTo(focusArea) == 0) {
                                selected.addItem(item);
                                break;
                            }
                        }
                    }

                    if (step == STEP.MOD_TAKEN || step == STEP.MOD_WANT || step == STEP.MOD_DONT_WANT) {
                        for (Module module : preplanModules) {
                            String moduleCode = e.getItem().toString().split(" ")[0];
                            if (module.getCode().compareTo(moduleCode) == 0) {
                                selected.addItem(module,true);
                                break;
                            }
                        }
                    }

                    if (step == STEP.PLANNING) {
                        Module module;
                        for (AvailableModule availableModule : availableModules) {
                            module = availableModule.getModule();
                            String moduleCode = e.getItem().toString().split(" ")[0];
                            if (module.getCode().compareTo(moduleCode) == 0) {
                                selected.addItem(module,true);
                                break;
                            }
                        }
                    }

                    isAddingOrRemovingItem = true;
                    dropdownList.removeItem(e.getItem());
                    dropdownList.setSelectedItem(null);
                    isAddingOrRemovingItem = false;
                    dropdownList.revalidate();
                }

                selectedScroller.revalidate();
            }
        });
    }

    private void addLabel(String text) {
        JLabel label = new JLabel(text);
        label.setAlignmentX(LEFT_ALIGNMENT);
        add(label);
        setPreferredSize(getLayout().preferredLayoutSize(this));
        validate();
    }
}
