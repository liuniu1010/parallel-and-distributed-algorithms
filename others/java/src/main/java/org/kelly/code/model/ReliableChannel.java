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
        // always generate a clone message to send to receiver
        // to prevent different threads modify on the same message instance
        // this simulate the real case in network environment
        Message cloneMessage = (Message)SimulateUtil.clone(message);
        ReliableChannelThread handleThread = new ReliableChannelThread(sender, cloneMessage, receiver);
        handleThread.start();
    }
}

class ReliableChannelThread extends Thread {
    private Participant sender;
    private Participant receiver;
    private Message message;

    public ReliableChannelThread(Participant inputSender, Message inputMessage, Participant inputReceiver) {
        sender = inputSender;
        message = inputMessage;
        receiver = inputReceiver;
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
