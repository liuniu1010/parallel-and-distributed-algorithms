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
        Channel unReliableChannel = Channel.getUnReliableChannelInstance();
        for(Participant receiver: receivers) {
            unReliableChannel.sendMessage(this, message, receiver);
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

    public void setCurrentN(int inputN) {
        currentN = inputN;
    }

    public void startPropose() {
        PaxosParticipantMonitorThread monitorThread = new PaxosParticipantMonitorThread(this);
        monitorThread.start();
    }

    private Vector<PaxosMessage> receivedFeedbacks = new Vector<PaxosMessage>();
    public Vector<PaxosMessage> getReceivedFeedbacks() {
        return receivedFeedbacks;
    }

    public void addReceivedFeedback(PaxosMessage message) {
        receivedFeedbacks.add(message);
    }


    private PaxosMessage messageAccepted;
    public PaxosMessage getMessageAccepted() {
        return messageAccepted;
    }

    public void setMessageAccepted(PaxosMessage inputPaxosMessage) {
        messageAccepted = inputPaxosMessage;
    }

    private int chosenValue = -1;
    public int getChosenValue() {
        return chosenValue;
    }

    public void setChosenValue(int inputValue) {
        chosenValue = inputValue;
    }

    public static int STAGE_PREPARE_REQUEST_SENT = 1;
    public static int STAGE_ACCEPT_REQUEST_SENT = 2;
    public static int STAGE_END = 3;
    private int stage = -1;

    public int getStage() {
        return stage;
    }

    public void setStage(int inputStage) {
        stage = inputStage;
    }
}

class PaxosParticipantMonitorThread extends Thread {
    private int timeout = 2000;  // every 2 seconds check once
    private PaxosParticipant self;

    public PaxosParticipantMonitorThread(PaxosParticipant inputSelf) {
        self = inputSelf;
    }

    @Override
    public void run() {
        try {
            synchronized(self) {
                checkAndRun();
            }
            Thread.sleep(timeout);
        }
        catch(InterruptedException iex) {
            iex.printStackTrace();
            throw new RuntimeException(iex);
        }
    }

    private void checkAndRun() {
        if(self.getStage() < 0) {
            startPropose();
        }
        else if(self.getStage() == PaxosParticipant.STAGE_PREPARE_REQUEST_SENT) {
            checkAndStartNewRequest();
        }
        else if(self.getStage() == PaxosParticipant.STAGE_ACCEPT_REQUEST_SENT) {
            checkAndStartLearning();
        }
    }

    private void startPropose() {
        self.setCurrentN(SimulateUtil.acquireNForPaxos());
        PaxosMessage prepareRequest = new PaxosMessage(PaxosMessage.MESSAGE_TYPE_PREPARE_REQUEST);
        prepareRequest.setN(self.getCurrentN());
        // at this step of prepare request, the proposed value does
        // not be set in the message, only initialN be set
        self.multiCastMessage(prepareRequest);
        self.setStage(PaxosParticipant.STAGE_PREPARE_REQUEST_SENT);
    }

    private void startLearning() {
        PaxosMessage learningMessage = new PaxosMessage(PaxosMessage.MESSAGE_TYPE_LEARNING);
        learningMessage.setV(self.getProposedValue());
        self.multiCastMessage(learningMessage);
    }

    private void checkAndStartNewRequest() {
        // check if the number of acks exceed half of all participants
        Vector<PaxosMessage> validAcks = new Vector<PaxosMessage>();

        // only acks with the same N is valid acks
        // because some very slow acks with a lower N might arrived just now
        // these acks should be ignored.
        for(PaxosMessage feedbackMessage: self.getReceivedFeedbacks()) {
            if(feedbackMessage.getN() == self.getCurrentN()
                && feedbackMessage.getType() == PaxosMessage.MESSAGE_TYPE_ACK) {

                validAcks.add(feedbackMessage);
            }
        }

        if(validAcks.size() * 2 <= self.getGroup().getParticipants().size()) {
            // the received number does not exceed the half, prepare to 
            // start new prepare request again
            startPropose();
        }
    }

    private void checkAndStartLearning() {
        // check if the number of accepts exceed half of all participants
        Vector<PaxosMessage> validAccepts = new Vector<PaxosMessage>();

        // only accepts with the same N is avlid accepts
        // because some very slow acks with a lower N might arrived just now
        // these accepts should be ignored.
        for(PaxosMessage feedbackMessage: self.getReceivedFeedbacks()) {
            if(feedbackMessage.getN() == self.getCurrentN()
                && feedbackMessage.getType() == PaxosMessage.MESSAGE_TYPE_ACCEPT) {
                validAccepts.add(feedbackMessage);
            }
        }

        if(validAccepts.size() * 2 <= self.getGroup().getParticipants().size()) {
            // the received number does not exceed the half, prepare to 
            // start new prepare request again
            startPropose();
        }
        else {
            self.setChosenValue(self.getProposedValue());
            self.setStage(PaxosParticipant.STAGE_END);
            startLearning();
        }
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
            else if(paxosMessage.getType() == PaxosMessage.MESSAGE_TYPE_ACCEPT) {
                handleAccept();
            }
            else if(paxosMessage.getType() == PaxosMessage.MESSAGE_TYPE_LEARNING) {
                handleLearning();
            }
        }
    }

    private void handlePrePareRequest() {
        PaxosMessage paxosMessage = (PaxosMessage)message;
        PaxosMessage messageAccepted = receiver.getMessageAccepted();
        UnReliableChannel channel = (UnReliableChannel)Channel.getUnReliableChannelInstance();

        if(messageAccepted == null) {
            // no message accepted before, so current message will be 
            // accepted
            PaxosMessage ackMessage = new PaxosMessage(PaxosMessage.MESSAGE_TYPE_ACK);
            ackMessage.setN(paxosMessage.getN());
            ackMessage.setSenderId(receiver.getId());
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
                    ackMessage.setSenderId(receiver.getId());
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
                    ackMessage.setSenderId(receiver.getId());
                    channel.sendMessage(receiver, ackMessage, sender);
                    
                    // update new accepted message
                    paxosMessage.setV(messageAccepted.getV()); // the V should be updated as the previous accepted one
                    receiver.setMessageAccepted(paxosMessage);
                }
            }
            else { // paxosMessage.getN() < messageAccepted.getN(), the two N is impossbile to be equal
                // nothing to do in this case
                // a reject message can also be sent back to the proposer
                // this can improve the performance, but the protocol is a bit more complex
                // so in this implementation, just ignore the prepare request
            }
        } 
    }

    private void handleAck() {
        PaxosMessage paxosMessage = (PaxosMessage)message;
        receiver.addReceivedFeedback(paxosMessage);

        // check if the number of acks exceed half of all participants
        Vector<PaxosMessage> validAcks = new Vector<PaxosMessage>();

        // only acks with the same N is valid acks
        // because some very slow acks with a lower N might arrived just now
        // these acks should be ignored.
        for(PaxosMessage feedbackMessage: receiver.getReceivedFeedbacks()) {
            if(feedbackMessage.getN() == receiver.getCurrentN()
                && feedbackMessage.getType() == PaxosMessage.MESSAGE_TYPE_ACK) {

                validAcks.add(feedbackMessage);
            }
        }

        if(validAcks.size() * 2 > receiver.getGroup().getParticipants().size()) {
            UnReliableChannel channel = (UnReliableChannel)Channel.getUnReliableChannelInstance();
            // find out the accepted value from the highest N in acks
            int ackValue = -1;
            int highestNinAcks = -1;
            PaxosMessage messageToFind = null; // try to find out the message which contains value and the n is highest
            for(PaxosMessage ack: validAcks) {
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

            // send out accept request to those accepted pariticpant
            PaxosMessage acceptRequest = new PaxosMessage(PaxosMessage.MESSAGE_TYPE_ACCEPT_REQUEST);
            acceptRequest.setN(receiver.getCurrentN());
            acceptRequest.setV(receiver.getProposedValue());
            for(PaxosMessage ack: validAcks) {
                Participant destParticipant = receiver.getGroup().getParticipant(ack.getSenderId());
                channel.sendMessage(receiver, acceptRequest, destParticipant);
            }

            receiver.setStage(PaxosParticipant.STAGE_ACCEPT_REQUEST_SENT);
        }
    }

    private void handleAcceptRequest() {
        PaxosMessage paxosMessage = (PaxosMessage)message;
        PaxosMessage messageAccepted = receiver.getMessageAccepted();
        UnReliableChannel channel = (UnReliableChannel)Channel.getUnReliableChannelInstance();

        if(messageAccepted == null) {
            // it is impossible! it must be wrong at somewhere
            throw new RuntimeException("didn't accept any prepare request, why does the accept request be sent to me: " + receiver.getId());
        }

        if(paxosMessage.getN() >= messageAccepted.getN()) {
            // it is possible that the two N are equal
            // because the two messages might be from the same sender
            // so here we use >=

            if(paxosMessage.getV() == messageAccepted.getV()) {
                PaxosMessage acceptMessage = new PaxosMessage(PaxosMessage.MESSAGE_TYPE_ACCEPT);
                acceptMessage.setN(paxosMessage.getN());
                acceptMessage.setV(paxosMessage.getV());
                channel.sendMessage(receiver, acceptMessage, sender);
            }
        }
    }

    private void handleAccept() {
        PaxosMessage paxosMessage = (PaxosMessage)message;
        receiver.addReceivedFeedback(paxosMessage);
    }

    private void handleLearning() {
        PaxosMessage paxosMessage = (PaxosMessage)message;
        if(receiver.getChosenValue() < 0 && receiver.getChosenValue() != paxosMessage.getV()) {
            receiver.setChosenValue(paxosMessage.getV());
            receiver.setStage(PaxosParticipant.STAGE_END);
        }
        else {
            throw new RuntimeException("current receiver has another chosen value, this implementation of paxos algorithm failed!");
        }
    }
}
