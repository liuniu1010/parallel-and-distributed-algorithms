package org.kelly.code.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;
/***
 * FIFOParticipant simulate participant who always multicast messages
 * according to FIFO rule
 */
public class FIFOParticipant extends Participant {
    /***
     * this is an async method which should create a new thread to handle
     * the received message
     */
    @Override
    public void receiveMessage(Participant sender, Message message) {
        this.recordReceivedMessage(message);
        FIFOParticipantReceiveThread receiveThread = new FIFOParticipantReceiveThread((FIFOParticipant)sender, message, this);
        receiveThread.start();
    }

    @Override
    public void deliverMessage(Message message) {
        // recording it is enough
        this.recordDeliveredMessage(message);
    }

    private Hashtable<Integer, Vector<Message>> waitQueuesToDeliver = new Hashtable<Integer, Vector<Message>>();
    private Hashtable<Integer, Integer> lastSentTickCache = new Hashtable<Integer, Integer>();

    public Vector<Message> getWaitQueueMessagesToDeliver(int senderId) {
        return waitQueuesToDeliver.get(senderId);
    }

    public void putIntoWaitQueuesToDeliver(int senderId, Message message) {
        Vector<Message> messages = waitQueuesToDeliver.get(senderId);
        if(messages == null) {
            messages = new Vector<Message>();
        }

        messages.add(message);
        waitQueuesToDeliver.put(senderId, messages);
    }

    public void removeFromWaitQueuesToDeliver(int senderId, Message message) { 
        Vector<Message> messages = waitQueuesToDeliver.get(senderId);
        if(messages == null) {
            messages = new Vector<Message>();
        }

        messages.remove(message);
        waitQueuesToDeliver.put(senderId, messages);
    }

    public int getLastSentTick(int senderId) {
        int tick = 0;
        if(lastSentTickCache.containsKey(senderId)) {
            tick = lastSentTickCache.get(senderId);
        }

        return tick;
    }

    public void updateLastSentTick(int senderId, int tick) {
        lastSentTickCache.put(senderId, tick);
    }


    private TimeStamp baseTimeStamp = null;
    private int CONST_POWERVALUE_INDEX = 100;
    @Override
    public void multiCastMessage(Message message) {
        // init the time stamp
        if(baseTimeStamp == null) {
            baseTimeStamp = new TimeStamp(this.getId(), 0);
        }

        // each time of multicast, increase the tick generate
        baseTimeStamp.tickIncrease();
 
        FIFOMessage fifoMessage = new FIFOMessage(message.getInformation(), baseTimeStamp);
        Probe probe = new Probe(new PowerValue(CONST_POWERVALUE_INDEX));
        probe.addHopId(this.getId());
        fifoMessage.setProbe(probe);
        // get all participants and send message to all of them
        List<Participant> receivers = this.getGroup().getParticipants();
        Channel reliableChannel = Channel.getReliableChannelInstance();
        for(Participant receiver: receivers) {
            reliableChannel.sendMessage(this, fifoMessage, receiver);
            this.recordGeneratedProbes(probe);
        }

        // record the message as sent
        this.recordSentMessage(fifoMessage);
    }
}

class FIFOParticipantReceiveThread extends Thread {
    private FIFOParticipant sender;
    private Message message;
    private FIFOParticipant receiver;

    public FIFOParticipantReceiveThread(FIFOParticipant inputSender, Message inputMessage, FIFOParticipant inputReceiver) {
        sender = inputSender;
        message = inputMessage;
        receiver = inputReceiver;
    }

    @Override
    public void run() {
        synchronized(receiver) {
            if(message instanceof FIFOMessage) {
                handleReceivedMessage();
            }
            else if(message instanceof ReturnProbeMessage) {
                handleReturnProbeMessage();
            }
        }
    }

    private void handleReturnProbeMessage() {
        ReturnProbeMessage returnProbeMessage = (ReturnProbeMessage)message;
        receiver.recordReturnedProbes(returnProbeMessage.getProbe());
    }

    /***
     * the implementation of this method is the core of the
     * FIFO multicast algorithm
     * the received order is random, but the delivering
     * order must be the same as sending order
     */
    private void handleReceivedMessage() {
        // get the cached messages from the participant
        receiver.putIntoWaitQueuesToDeliver(sender.getId(), message);
        Vector<Message> messages = receiver.getWaitQueueMessagesToDeliver(sender.getId());

        ReliableChannel reliableChannel = (ReliableChannel)Channel.getReliableChannelInstance();
        // loop the cached messages to find the message which 
        // should be delivered
        while(!messages.isEmpty()) {
            FIFOMessage messageToFind = null;
            int lastSentTick = receiver.getLastSentTick(sender.getId());
            for(Message message: messages) {
                FIFOMessage fifomessage = (FIFOMessage)message;
                if(fifomessage.getTimeStamp().getTick() == (lastSentTick + 1)) {
                    messageToFind = fifomessage;
                    break;
                }
            }
            if(messageToFind != null) {
                // the message is found, deliver it and update the receiving buffer
                receiver.deliverMessage(messageToFind);
                receiver.updateLastSentTick(sender.getId(), messageToFind.getTimeStamp().getTick());
                receiver.removeFromWaitQueuesToDeliver(sender.getId(), messageToFind);
                messages = receiver.getWaitQueueMessagesToDeliver(sender.getId());

                // send back the probe
                ReturnProbeMessage returnProbeMessage = new ReturnProbeMessage();
                returnProbeMessage.setProbe(messageToFind.getProbe());
                reliableChannel.sendMessage(receiver, returnProbeMessage, sender);

                // the last send tick was increased, so the cached messages
                // should be loop again to find the next one 
                // to be deliver
                continue;
            }
            else {
                // the message didn't be found
                // so it has to wait for new received messages
                // current thread can be terminated, if there are
                // any new received message, a new thread
                // will be started and handle with the same logic
                break;
            }
        }
    }
}
