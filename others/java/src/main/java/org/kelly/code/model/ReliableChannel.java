package org.kelly.code.model;


/***
 * this class simulate a reliable channel to send message
 * but the message might be delayed for a random time
 */
public class ReliableChannel extends Channel {
    @Override
    public void sendMessage(Participant sender, Message message, Participant receiver) {
    }
}
