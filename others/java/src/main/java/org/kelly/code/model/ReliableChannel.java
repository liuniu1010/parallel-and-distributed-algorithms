package org.kelly.code.model;

import org.kelly.code.util.SimulateUtil;
/***
 * this class simulate a reliable channel to send message
 * but the message might be delayed for a random time
 */
public class ReliableChannel extends Channel {
    /***
     * this is an async method which should create a thread to handle 
     * the sending action and return immediately.
     */
    @Override
    public void sendMessage(Participant sender, Message message, Participant receiver) {
        ReliableChannelThread handleThread = new ReliableChannelThread();
        handleThread.setSender(sender);
        handleThread.setMessage(message);
        handleThread.setReceiver(receiver);
        handleThread.start();
    }
}

class ReliableChannelThread extends Thread {
    private Participant sender;
    private Participant receiver;
    private Message message;

    public Participant getSender() {
        return sender;
    }

    public void setSender(Participant inputSender) {
        sender = inputSender;
    }

    public Participant getReceiver() {
        return receiver;
    }

    public void setReceiver(Participant inputReceiver) {
        receiver = inputReceiver;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message inputMessage) {
        message = inputMessage;
    }

    @Override
    public void run() {
        // delay a random time between 10~100 millisecond to simulate the delay in sending
        int delayMillisecond = SimulateUtil.getRandomIntBetween(10, 100);
        try{
            Thread.sleep(delayMillisecond);
        }
        catch(InterruptedException iex) {
            iex.printStackTrace();
        }

        receiver.receiveMessage(sender, message);
    }
}
