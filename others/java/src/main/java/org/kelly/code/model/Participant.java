package org.kelly.code.model;

public abstract class Participant {
    abstract public void receiveMessage(Participant sender, Message message);

    abstract public void deliverMessage(Message message);

    private int id;

    public int getId() {
        return id;
    }

    public void setId(int inputId) {
        id = inputId;
    }
}
