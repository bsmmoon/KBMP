package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

/**
 * Created by Ruofan on 13/4/2016.
 */
public class PrePlanOption extends JPanel {
    boolean isRadio;
    private PrePlanPanel panel;
    private INFO infoType;
    private JLabel text;
    private JCheckBox checkBox;
    private JRadioButton radioButton;
    private JComboBox<String> dropdownList;

    enum INFO {
        H2_MATHS,H2_PHYSICS,GOOD_MATHS,COMMUNICATION_EXEMPT,CS3201,CS3216,CS3281,CS3283,SIP,ATAP,NOC_SEM,NOC_YEAR
    }

    public PrePlanOption(final PrePlanPanel panel, INFO infoType, String text, ArrayList<String> semesters, boolean isRadio) {
        this.isRadio = isRadio;
        this.panel = panel;
        this.infoType = infoType;

        setBackground(Color.WHITE);

        setAlignmentX(LEFT_ALIGNMENT);
        if (isRadio) {
            radioButton = new JRadioButton(text);
            radioButton.setBackground(Color.WHITE);
            radioButton.setAlignmentX(LEFT_ALIGNMENT);
            add(radioButton);
        } else {
            checkBox = new JCheckBox(text);
            checkBox.setBackground(Color.WHITE);
            checkBox.setAlignmentX(LEFT_ALIGNMENT);
            add(checkBox);
        }
        //this.text = new JLabel(text);

        //add(this.text);

        if (semesters != null) {
            dropdownList = new JComboBox<String>();
            for (String sem : semesters) {
                this.dropdownList.addItem(sem);
            }
            dropdownList.setMaximumSize(dropdownList.getPreferredSize());
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

    public boolean isSelected() {
        if (isRadio) {
            return radioButton.isSelected();
        } else {
            return checkBox.isSelected();
        }
    }

    public void setRadioSelected() { radioButton.setSelected(true);}

    public void setButtonGroup(ButtonGroup group) {
        group.add(radioButton);
    }

    public int getSemesterSelected() {
        String[] tokens = dropdownList.getSelectedItem().toString().split(" ");
        int year = Integer.parseInt(tokens[1]) - 1;
        int sem = Integer.parseInt(tokens[3]);

        return year*2 + sem;
    }

    public INFO getInfoType() {
        return infoType;
    }
}
