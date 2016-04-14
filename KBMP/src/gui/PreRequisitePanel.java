package gui;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * Created by Ruofan on 13/4/2016.
 */
public class PreRequisitePanel extends JPanel {
    private GuiFrame frame;
    private JComboBox<String> startingSem;
    private JTextField yearField;
    private JTextField semesterField;
    private ArrayList<PrePlanCheckBox> checkBoxes;

    public PreRequisitePanel(final GuiFrame frame) {
        ArrayList<String> semesters = new ArrayList<String>(Arrays.asList(new String[]
                {"Year 1 Semester 1", "Year 1 Semester 2", "Year 2 Semester 1", "Year 2 Semester 2",
                        "Year 3 Semester 1","Year 3 Semester 2","Year 4 Semester 1","Year 4 Semester 2"}));

        this.frame = frame;

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setBackground(Color.WHITE);

        addLabel("Please select the year and semester you wish to start planning:");
        startingSem = new JComboBox<String>();
        for (String sem : semesters) {
            startingSem.addItem(sem);
        }
        startingSem.setSelectedItem(semesters.get(0));
        startingSem.setAlignmentX(LEFT_ALIGNMENT);
        startingSem.setMaximumSize(new Dimension(160,20));
        add(startingSem);
        //yearField = addTextField("1");
        //semesterField = addTextField("1");

        checkBoxes = new ArrayList<PrePlanCheckBox>();

        addLabel("Have you passed the following subjects?");
        checkBoxes.add(addCheckBox(PrePlanCheckBox.INFO.H2_MATHS,"O Level H2 Maths or equivalent",null));
        checkBoxes.add(addCheckBox(PrePlanCheckBox.INFO.H2_PHYSICS,"O Level H2 Physics or equivalent",null));

        addLabel("Do you have a strong background in the following subjects?");
        checkBoxes.add(addCheckBox(PrePlanCheckBox.INFO.GOOD_MATHS,"Mathematics",null));

        addLabel("Are you exempted from the following modules?");
        checkBoxes.add(addCheckBox(PrePlanCheckBox.INFO.COMMUNICATION_EXEMPT,"CS2101 (CS communication module)",null));

        addLabel("Which pair of CS 8MC project modules do you intend to take?");
        checkBoxes.add(addCheckBox(PrePlanCheckBox.INFO.CS3201,"Software Engineering Project (CS3201 and CS3202)",null));
        checkBoxes.add(addCheckBox(PrePlanCheckBox.INFO.CS3216,"Software Development on Evolving Platforms and Software Engineering on Modern Application Platforms (CS3216 and CS3217)",null));
        checkBoxes.add(addCheckBox(PrePlanCheckBox.INFO.CS3281,"Thematic Systems Project (CS3281 and CS3282)",null));
        checkBoxes.add(addCheckBox(PrePlanCheckBox.INFO.CS3283,"Media Technology Project (CS3283 and CS3284)",null));

        addLabel("Do you intend to take the following programs?");
        checkBoxes.add(addCheckBox(PrePlanCheckBox.INFO.SIP,"Student Internship Program (SIP)",null));
        checkBoxes.add(addCheckBox(PrePlanCheckBox.INFO.ATAP,"Advanced Technology Attachment Programme (ATAP)",semesters));
        checkBoxes.add(addCheckBox(PrePlanCheckBox.INFO.NOC_SEM,"1-semester NUS Overseas College (NOC)",semesters));
        checkBoxes.add(addCheckBox(PrePlanCheckBox.INFO.NOC_YEAR,"1-year NUS Overseas College (NOC)",semesters));
    }

    private void addLabel(String text) {
        JLabel label = new JLabel(text);
        label.setAlignmentX(LEFT_ALIGNMENT);
        add(label);
    }

    private JTextField addTextField(String defaultText) {
        JTextField textField = new JTextField(20);
        textField.setText(defaultText);
        textField.setMaximumSize(new Dimension(50, 20));
        textField.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(textField);
        return textField;
    }

    private PrePlanCheckBox addCheckBox(PrePlanCheckBox.INFO info, String text, ArrayList<String> semesters) {
        PrePlanCheckBox checkBox = new PrePlanCheckBox(this,info,text,semesters);
        checkBox.setAlignmentX(LEFT_ALIGNMENT);
        add(checkBox);
        return checkBox;
    }

    public void submit() {
        String yearSem = startingSem.getSelectedItem().toString();
        String[] tokens = yearSem.split(" ");

        frame.getLogic().setStartTime(Integer.parseInt(tokens[1]),Integer.parseInt(tokens[3]));

        for (PrePlanCheckBox checkBox : checkBoxes) {
            switch (checkBox.getInfoType()) {
                case H2_MATHS:
                    if (checkBox.isChecked()) {frame.getLogic().assertH2Maths(); }
                    break;
                case H2_PHYSICS:
                    if (checkBox.isChecked()) {frame.getLogic().assertH2Physics(); }
                    break;
                case GOOD_MATHS:
                    if (checkBox.isChecked()) {frame.getLogic().assertGoodMath(); }
                    else {frame.getLogic().assertNormalMath(); }
                    break;
                case COMMUNICATION_EXEMPT:
                    if (checkBox.isChecked()) {frame.getLogic().assertCommunicationExemption(); }
                    else {frame.getLogic().assertCommunicationNotExempted(); }
                    break;
                case CS3201:
                    if (checkBox.isChecked()) {frame.getLogic().assertCS3201(); }
                    break;
                case CS3216:
                    if (checkBox.isChecked()) {frame.getLogic().assertCS3216(); }
                    break;
                case CS3281:
                    if (checkBox.isChecked()) {frame.getLogic().assertCS3281(); }
                    break;
                case CS3283:
                    if (checkBox.isChecked()) {frame.getLogic().assertCS3283(); }
                    break;
                case SIP:
                    if (checkBox.isChecked()) {frame.getLogic().assertSIP(); }
                    break;
                case ATAP:
                    if (checkBox.isChecked()) {frame.getLogic().asertATAP(checkBox.getSemesterSelected()); }
                    break;
                case NOC_SEM:
                    if (checkBox.isChecked()) {frame.getLogic().assertNOC1Sem(checkBox.getSemesterSelected()); }
                    break;
                case NOC_YEAR:
                    if (checkBox.isChecked()) {frame.getLogic().assertNOC1Year(checkBox.getSemesterSelected()); }
                    break;
            }
        }
    }
}
