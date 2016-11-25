package org.kelly.code.model;

/***
 * PaxosParticipant simulte participants who try
 * to achieve and agreement by paxos algorithm.
 * The main step in paxos algorithm includes
 * phase 1: prepare request
 *          ack
 * phase 2: accept request
 *          accept
 */
public class PaxosParticipant extends Participant {
    /***
     * this is an async method which should create a new thread to handle
     * the received message
     */
    @Override
    public void receiveMessage(Participant sender, Message message) {
        PaxosParticipantReceiveThread receiveThread = new PaxosParticipantReceiveThread((PaxosParticipant)sender, message, this);
        receiveThread.start();
    }

    @Override
    public void deliverMessage(Message message) {
        
    }

    @Override
    public void multiCastMessage(Message message) {
    }

    
}

class PaxosParticipantReceiveThread extends Thread {
    private PaxosParticipant sender;
    private Message message;
    private PaxosParticipant receiver;

    public PaxosParticipantReceiveThread(PaxosParticipant inputSender, Message inputMessage, PaxosParticipant inputReceiver) {
        sender = inputSender;
        message = inputMessage;
        receiver = inputReceiver;
    }

    @Override
    public void run() {
    }
}
