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

    private int proposedValue = -1;
    public void startPropose(int inputProposedValue) {
        proposedValue = inputProposedValue; // record it but not set in message in the first step
        int initialN = SimulateUtil.getRandomIntBetween(0, 5);
        PaxosMessage paxosMessage = new PaxosMessage(PaxosMessage.MESSAGE_TYPE_PREPARE_REQUEST);
        paxosMessage.setN(initialN);
        // at this step of prepare request, the proposed value does
        // not be set in the message, only initialN be set
        multiCastMessage(paxosMessage);
    }

    private int maxNReceived = 0;
    public int getMaxNReceived() {
        return maxNReceived;
    }

    public void setMaxNReceived(int inputMaxNReceived) {
        maxNReceived = inputMaxNReceived;
    }

    private PaxosMessage messageAccepted;
    public PaxosMessage getMessageAccepted() {
        return messageAccepted;
    }

    public void setMessageAccepted(PaxosMessage inputPaxosMessage) {
        messageAccepted = inputPaxosMessage;
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
        PaxosMessage messageAccepted = receiver.getMessageAccepted();
        ReliableChannel channel = (ReliableChannel)Channel.getReliableChannelInstance();

        if(messageAccepted == null) {
            // no message accepted before, so current message will be 
            // accepted
            PaxosMessage ackMessage = new PaxosMessage(PaxosMessage.MESSAGE_TYPE_ACK);
            ackMessage.setN(paxosMessage.getN());
            channel.sendMessage(receiver, ackMessage, sender); // send the ack back

            receiver.setMessageAccepted(paxosMessage); // record it as accepted message
        }
        else {
            // there is message accepted before, so compare them first
            // add code here next time
        } 
    }

    private void handleAck() {
    }

    private void handleAcceptRequest() {
    }
}
