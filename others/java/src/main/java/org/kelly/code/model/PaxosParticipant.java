package org.kelly.code.model;

import java.util.List;
import java.util.Vector;
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

    public int getProposedValue() {
        return proposedValue;
    }

    public void setProposedValue(int inputValue) {
        proposedValue = inputValue;
    }

    private int currentN;
    public int getCurrentN() {
        return currentN;
    }

    public void startPropose() {
        currentN = SimulateUtil.acquireNForPaxos();
        PaxosMessage paxosMessage = new PaxosMessage(PaxosMessage.MESSAGE_TYPE_PREPARE_REQUEST);
        paxosMessage.setN(currentN);
        // at this step of prepare request, the proposed value does
        // not be set in the message, only initialN be set
        multiCastMessage(paxosMessage);
    }

    private Vector<PaxosMessage> receivedAcks = new Vector<PaxosMessage>();
    public Vector<PaxosMessage> getReceivedAcks() {
        return receivedAcks;
    }

    public void addReceivedAck(PaxosMessage message) {
        receivedAcks.add(message);
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
            if(paxosMessage.getN() > messageAccepted.getN()) {
                // new prepare request has a bigger N than the accepted one
                if(messageAccepted.getV() < 0) {
                    // the previous accepted message does not contain a V
                    // so just ack the recevied message and update the 
                    // accepted message as the new message
                    PaxosMessage ackMessage = new PaxosMessage(PaxosMessage.MESSAGE_TYPE_ACK);
                    ackMessage.setN(paxosMessage.getN());
                    channel.sendMessage(receiver, ackMessage, sender);

                    // update new accepted message
                    receiver.setMessageAccepted(paxosMessage);
                }
                else {
                    // the previous accepted message contain a valid V
                    // so the sender should be notified the value
                    PaxosMessage ackMessage = new PaxosMessage(PaxosMessage.MESSAGE_TYPE_ACK);
                    ackMessage.setN(paxosMessage.getN());
                    ackMessage.setAnotherN(messageAccepted.getN());
                    ackMessage.setAnotherV(messageAccepted.getV());
                    channel.sendMessage(receiver, ackMessage, sender);
                    
                    // update new accepted message
                    paxosMessage.setV(messageAccepted.getV()); // the V should be updated as the previous accepted one
                    receiver.setMessageAccepted(paxosMessage);
                }
            }
            else { // paxosMessage.getN() < messageAccepted.getN(), the two N is impossbile to be equal
                // nothing to do in this case
            }
        } 
    }

    private void handleAck() {
        PaxosMessage paxosMessage = (PaxosMessage)message;
        receiver.addReceivedAck(paxosMessage);

        // check if the number of acks exceed half of all participants
        if(receiver.getReceivedAcks().size() * 2 > receiver.getGroup().getParticipants().size()) {
            // find out the accepted value from the highest N in acks
            int ackValue = -1;
            int highestNinAcks = -1;
            Vector<PaxosMessage> receivedAcks = receiver.getReceivedAcks();
            PaxosMessage messageToFind = null; // try to find out the message which contains value and the n is highest
            for(PaxosMessage ack: receivedAcks) {
                if(ack.getAnotherV() < 0) {
                    continue;
                }
                ackValue = (ack.getAnotherN() > highestNinAcks)?ack.getAnotherV():ackValue;
                highestNinAcks = (ack.getAnotherN() > highestNinAcks)?ack.getAnotherN():highestNinAcks;
            }
            if(ackValue >= 0) {
                // there exist accepted value, so change the participant's proposed value to the accepted value
                receiver.setProposedValue(ackValue); 
            }
            else {
                // there does not exist accepted value, use the partiicpant's own proposed value
            }

            // send out accept request
            PaxosMessage acceptRequest = new PaxosMessage(PaxosMessage.MESSAGE_TYPE_ACCEPT_REQUEST);
            acceptRequest.setN(receiver.getCurrentN());
            acceptRequest.setV(receiver.getProposedValue());

            receiver.multiCastMessage(acceptRequest);
        }
    }

    private void handleAcceptRequest() {
        PaxosMessage paxosMessage = (PaxosMessage)message;
        PaxosMessage accept = new PaxosMessage(PaxosMessage.MESSAGE_TYPE_ACCEPT);
        accept.setN(paxosMessage.getN());
        accept.setV(paxosMessage.getV());

        receiver.multiCastMessage(accept);
    }
}
