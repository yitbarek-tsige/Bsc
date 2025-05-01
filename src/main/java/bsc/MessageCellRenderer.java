package bsc;

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.*;
import java.awt.*;


class MessageCellRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof Message) {
            Message message = (Message) value;
            if (!message.isRead()) {
                label.setFont(label.getFont().deriveFont(Font.BOLD));
                label.setForeground(Color.RED); 
            } else {
                label.setFont(label.getFont().deriveFont(Font.PLAIN));
                label.setForeground(Color.BLACK);
            }
        }
        return label;
    }
}



