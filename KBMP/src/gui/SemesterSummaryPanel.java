package gui;

import common.Semester;

import javax.swing.*;

/**
 * Created by bsmmo on 14/4/2016.
 */
public class SemesterSummaryPanel extends JPanel {
    private Semester semester;
    private JLabel label;

    public SemesterSummaryPanel(Semester semester) {
        this.semester = semester;
        // planned.addLabel(frame.getModel().getModulePlan().getSemester(frame.getModel().getCumulativeSemester() - 1).getSummary());
        this.label = new JLabel(semester.getSummary());
    }

    public JLabel getLabel() { return label; }
}
