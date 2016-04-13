package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

/**
 * Created by Ruofan on 13/4/2016.
 */
public class PrePlanCheckBox extends JPanel {
    private PreRequisitePanel panel;

    private JLabel text;
    private JCheckBox checkBox;
    private JComboBox<String> dropdownList;

    public PrePlanCheckBox(final PreRequisitePanel panel, String text, ArrayList<String> semesters) {
        this.panel = panel;
        setBackground(Color.WHITE);

        setAlignmentX(LEFT_ALIGNMENT);
        checkBox = new JCheckBox();
        checkBox.setBackground(Color.WHITE);
        checkBox.setAlignmentX(LEFT_ALIGNMENT);

        this.text = new JLabel(text);

        add(checkBox);
        add(this.text);

        if (semesters != null) {
            dropdownList = new JComboBox<String>();
            for (String sem : semesters) {
                this.dropdownList.addItem(sem);
            }
            dropdownList.setMaximumSize(new Dimension(50, 20));
            dropdownList.setVisible(false);
            add(dropdownList);

            checkBox.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        dropdownList.setVisible(true);
                        setMaximumSize(getPreferredSize());
                        revalidate();
                    }
                }
            });
        }
        setMaximumSize(getPreferredSize());
    }

    public boolean isChecked() {
        return checkBox.isSelected();
    }

    public String getSemesterSelected() {
        return dropdownList.getSelectedItem().toString();
    }
}
