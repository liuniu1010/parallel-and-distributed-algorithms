package org.cache.code.ui;

import javax.swing.JTextPane;
import org.cache.code.MessageFeedbackIFC;

public class MessageFeedback implements MessageFeedbackIFC {
    private JTextPane textPane;

    public JTextPane getTextPane() {
        return textPane;
    }

    public void setTextPane(JTextPane tPane) {
        textPane = tPane;
    }

    public void insertMessage(String message) {
        String oldMessage = textPane.getText();
        if(oldMessage == null) {
            oldMessage = "";
        }
        if(message == null) {
            message = "";
        }

        oldMessage += message + "\n";
        textPane.setText(oldMessage);
    }
}
