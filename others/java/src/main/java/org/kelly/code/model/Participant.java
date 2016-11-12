package org.kelly.code.model;

import java.util.Vector;

public abstract class Participant {
    abstract public void receiveMessage(Participant sender, Message message);

    abstract public void deliverMessage(Message message);

    abstract public void multiCastMessage(Message message);

    private int id;
    private Group group;

    public int getId() {
        return id;
    }

    public void setId(int inputId) {
        id = inputId;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group inputGroup) {
        group = inputGroup;
    }

    /***
     * these two variables are to record all sent messages, received messages
     * and delivered messages, the order in Vector will indicates the real 
     * happened order in running time
     */
    private Vector<Message> sentMessages = new Vector<Message>();
    private Vector<Message> receivedMessages = new Vector<Message>();
    private Vector<Message> deliveredMessages = new Vector<Message>();

    public Vector<Message> getSentMessages() {
        return sentMessages;
    }

    public void recordSentMessage(Message inputMessage) {
        if(sentMessages == null) {
            sentMessages = new Vector<Message>();
        }

        sentMessages.add(inputMessage);
    }

    public Vector<Message> getReceivedMessages() {
        return receivedMessages;
    }

    public void recordReceivedMessage(Message inputMessage) {
        if(receivedMessages == null) {
            receivedMessages = new Vector<Message>();
        }

        receivedMessages.add(inputMessage);
    }

    public Vector<Message> getDeliveredMessages() {
        return deliveredMessages;
    }

    public void recordDeliveredMessage(Message inputMessage) {
        if(deliveredMessages == null) {
            deliveredMessages = new Vector<Message>();
        }

        deliveredMessages.add(inputMessage);
    }

    @Override
    public String toString() {
        String str = "id = " + id;
        str += "\nmulticast messages";
        for(Message message: sentMessages) {
            str += "\n" + message;
        }

        str += "\nreceived messages";
        for(Message message: receivedMessages) {
            str += "\n" + message;
        }

        str += "\ndelivered messages";
        for(Message message: deliveredMessages) {
            str += "\n" + message;
        }
        return str;
    }
}
