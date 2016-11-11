package org.kelly.code.model;

public abstract class Channel {

    /***
     * Participant use this method to simulate the sending message process
     * the message might be blocked, delayed, or discarded in different simulation
     */
    abstract public void sendMessage(Participant sender, Message message, Participant receiver);
}
