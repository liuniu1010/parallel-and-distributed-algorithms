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
        FIFOParticipantReceiveThread receiveThread = new FIFOParticipantReceiveThread(sender, message, this);
        receiveThread.start();
    }

    @Override
    public void deliverMessage(Message message) {
        // recording it is enough
        this.recordDeliveredMessage(message);
    }


    private TimeStamp baseTimeStamp = null;
    @Override
    public void multiCastMessage(Message message) {
        // init the time stamp
        if(baseTimeStamp == null) {
            baseTimeStamp = new TimeStamp(this.getId(), 0);
        }

        // each time of multicast, increase the tick generate
        // a clone to append to the message
        baseTimeStamp.tickIncrease();
        TimeStamp cloneStamp = baseTimeStamp.clone();
 
        FIFOMessage fifoMessage = new FIFOMessage(message.getInformation(), cloneStamp);
        // get all participants and send message to all of them
        List<Participant> receivers = this.getGroup().getParticipants();
        Channel reliableChannel = Channel.getReliableChannelInstance();
        for(Participant receiver: receivers) {
            reliableChannel.sendMessage(this, fifoMessage, receiver);
        }

        // record the message as sent
        this.recordSentMessage(fifoMessage);
    }
}

class FIFOParticipantReceiveThread extends Thread {
    private Participant sender;
    private Message message;
    private Participant receiver;

    public FIFOParticipantReceiveThread(Participant inputSender, Message inputMessage, Participant inputReceiver) {
        sender = inputSender;
        message = inputMessage;
        receiver = inputReceiver;
    }

    private static Hashtable<Integer, Vector<Message>> waitQueuesToDeliver = new Hashtable<Integer, Vector<Message>>();
    private static Hashtable<Integer, Integer> lastSentTickCache = new Hashtable<Integer, Integer>();


    /***
     * the implementation of this method is the core of the
     * FIFO multicast algorithm
     */
    @Override
    public void run() {
        FIFOMessage receivedMessage = (FIFOMessage)message;
        int participantId = sender.getId();

        // get the cached messages from the participant
        Vector<Message> messages = waitQueuesToDeliver.get(participantId);
        if(messages == null) {
            messages = new Vector<Message>();
        }
        messages.add(receivedMessage);
        waitQueuesToDeliver.put(participantId, messages);

        // init the last sent tick or get last sent tick
        int lastSentTick = 0;
        if(lastSentTickCache.containsKey(participantId)) {
            lastSentTick = lastSentTickCache.get(participantId);
        }

        // loop the cached messages to find the message which 
        // should be delivered
        while(!messages.isEmpty()) {
            Message messageToFind = null;
            for(Message message: messages) {
                FIFOMessage fifomessage = (FIFOMessage)message;
                if(fifomessage.getTimeStamp().getTick() == (lastSentTick + 1)) {
                    messageToFind = fifomessage;
                    break;
                }
            }
            if(messageToFind != null) {
                // the message is found, send it
                receiver.deliverMessage(messageToFind);
                lastSentTick++;
                lastSentTickCache.put(participantId, lastSentTick);
                messages.remove(messageToFind);
                // the last send tick was increased, so the cached messages
                // should be loop again to find the next one 
                // to be send
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
