package org.kelly.code.model;

import java.util.List;
import org.kelly.code.util.SimulateUtil;

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
        // get all participants and send message to all of them
        List<Participant> receivers = this.getGroup().getParticipants();
        Channel reliableChannel = Channel.getReliableChannelInstance();
        for(Participant receiver: receivers) {
            reliableChannel.sendMessage(this, message, receiver);
        }
    }

    public void startPropose(int proposedValue) {
        int initialN = SimulateUtil.getRandomIntBetween(0, 5);
        PaxosMessage paxosMessage = new PaxosMessage(PaxosMessage.MESSAGE_TYPE_PREPARE_REQUEST, initialN, proposedValue);
        multiCastMessage(paxosMessage);
    }

    private int maxNReceived = 0;
    public int getMaxNReceived() {
        return maxNReceived;
    }

    public void setMaxNReceived(int inputMaxNReceived) {
        maxNReceived = inputMaxNReceived;
    }

    private int valueAccepted = -1;
    public int getValueAccepted() {
        return valueAccepted;
    }

    public void setValueAccepted(int inputValueAccepted) {
        valueAccepted = inputValueAccepted;
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
        synchronized(receiver) {
            PaxosMessage paxosMessage = (PaxosMessage)message;
            if(paxosMessage.getType() == PaxosMessage.MESSAGE_TYPE_PREPARE_REQUEST) {
                handlePrePareRequest();
            }
            else if(paxosMessage.getType() == PaxosMessage.MESSAGE_TYPE_ACK) {
                handleAck();
            }
            else if(paxosMessage.getType() == PaxosMessage.MESSAGE_TYPE_ACCEPT_REQUEST) {
                handleAcceptRequest();
            }
        }
    }

    private void handlePrePareRequest() {
        PaxosMessage paxosMessage = (PaxosMessage)message;
        
    }

    private void handleAck() {
    }

    private void handleAcceptRequest() {
    }
}
