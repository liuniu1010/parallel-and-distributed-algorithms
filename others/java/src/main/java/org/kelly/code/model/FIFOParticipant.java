package org.kelly.code.model;

/***
 * FIFOParticipant simulate participant who always multicast messages
 * according to FIFO rule
 */
public class FIFOParticipant extends Participant {
    /***
     * this is an async method which should create a new thread to handle
     * the received message
     */
    @Override
    public void receiveMessage(Participant sender, Message message) {
        FIFOParticipantReceiveThread receiveThread = new FIFOParticipantReceiveThread();
        receiveThread.setSender(sender);
        receiveThread.setMessage(message);
        receiveThread.start();
    }

    @Override
    public void deliverMessage(Message message) {
    }
}

class FIFOParticipantReceiveThread extends Thread {
    private Participant sender;
    private Message message;

    public Participant getSender() {
        return sender;
    }

    public void setSender(Participant inputSender) {
        sender = inputSender;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message inputMessage) {
        message = inputMessage;
    }

    @Override
    public void run() {
    }
}
