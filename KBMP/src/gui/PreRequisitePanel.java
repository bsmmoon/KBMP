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
    private JTextField numSemesterLeft;
    private ArrayList<PrePlanCheckBox> checkBoxes;

    public PreRequisitePanel(final GuiFrame frame) {
        this.frame = frame;

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setBackground(Color.WHITE);

        addLabel("Please enter the number of semester left:");
        numSemesterLeft = addTextField("8");

        checkBoxes = new ArrayList<PrePlanCheckBox>();

        addLabel("Have you passed the following subjects?");
        checkBoxes.add(addCheckBox(PrePlanCheckBox.INFO.H2_MATHS,"O Level H2 Maths or equivalent",null));
        checkBoxes.add(addCheckBox(PrePlanCheckBox.INFO.H2_PHYSICS,"O Level H2 Physics or equivalent",null));

        ArrayList<String> semesters = new ArrayList<String>(Arrays.asList(new String[]{"1","2","3","4","5","6","7","8"}));
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
        try {
            int number = Integer.parseInt(numSemesterLeft.getText());
            if (number < 1 || number > 10) {
                JOptionPane.showMessageDialog(this, "Please enter a number between 1 and 10.");
            }

            frame.getLogic().setNumberOfSemesterLeft(number);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, numSemesterLeft.getText() + " is not a valid number.");
        }

        for (PrePlanCheckBox checkBox : checkBoxes) {
            switch (checkBox.getInfoType()) {
                case H2_MATHS:
                    frame.getLogic().assertH2Maths();
                    break;
                case H2_PHYSICS:
                    frame.getLogic().assertH2Physics();
                    break;
                case SIP:
                case ATAP:
                case NOC_SEM:
                case NOC_YEAR:
            }
        }
    }
}
