package org.kelly.code.model;

import org.kelly.code.util.SimulateUtil;
/***
 * this class simulate an unreliable channel to send message
 * the message might be delayed for a random time
 * the message might also be lost with a probability
 */
public class UnReliableChannel extends Channel {
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
        UnReliableChannelSendMessageThread handleThread = new UnReliableChannelSendMessageThread(sender, cloneMessage, receiver);
        handleThread.start();
    }
}

class UnReliableChannelSendMessageThread extends Thread {
    private Participant sender;
    private Participant receiver;
    private Message message;

    public UnReliableChannelSendMessageThread(Participant inputSender, Message inputMessage, Participant inputReceiver) {
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

        // to simulate the possible lost, we generate a random
        // number between 0~9, if the random number is 0
        // then end the thread without doing any thing
        // that means a message has 10% possibility to be lost

        if(SimulateUtil.getRandomIntBetween(0, 10) != 0) {
            receiver.receiveMessage(sender, message);
        }
    }
}
