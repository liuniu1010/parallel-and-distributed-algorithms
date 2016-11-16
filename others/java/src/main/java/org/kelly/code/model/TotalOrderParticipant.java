package org.kelly.code.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Collections;
import java.util.Comparator;

import org.kelly.code.util.SimulateUtil;

/***
 * TotalParticipant simulate participant who always multicast messages
 * according to Total ordered rule
 *
 * the algorithm to implement total ordered messages
 * contains three step of sending message
 *
 * the first step, sender multicast message
 * 
 * the second step, on receiving the step one's 
 * message, the receiver assign a priority for the message,
 * mark is as undeliverable, then send it back to the sender
 * 
 * the third step, on receiving step two's backing
 * message, the sender re set the priority as the max one
 * and multicast it again. when receiver got step three's message
 * it will mark it as deliverable and put it into deliver queue
 * and deliver it
 */
public class TotalOrderParticipant extends Participant {
    /***
     * this is an async method which should create a new thread to handle
     * the received message
     */
    @Override
    public void receiveMessage(Participant sender, Message message) {
        this.recordReceivedMessage(message);
        TotalOrderParticipantReceiveThread receiveThread = new TotalOrderParticipantReceiveThread((TotalOrderParticipant)sender, (TotalOrderMessage)message, this);
        receiveThread.start();
    }

    @Override
    public void deliverMessage(Message message) {
        // recording it is enough
        this.recordDeliveredMessage(message);
    }
    private Hashtable<Integer, Vector<Message>> waitQueuesToSend = new Hashtable<Integer, Vector<Message>>();

    public Vector<Message> getWaitQueueMessagesToSend(int messageId) {
        return waitQueuesToSend.get(messageId);
    }

    public void putIntoWaitQueuesToSend(TotalOrderMessage message) {
        Vector<Message> messages = waitQueuesToSend.get(message.getId());
        if(messages == null) {
            messages = new Vector<Message>();
        }

        messages.add(message);
        waitQueuesToSend.put(message.getId(), messages);
    }

    public int getSizeOfWaitQueuesToSend(int messageId) {
        Vector<Message> messages = waitQueuesToSend.get(messageId);
        return (messages == null)?0:messages.size(); 
    }
    
    public int getMaxPriorityInWaitQueuesToSend(int messageId) {
        int maxPriority = 0;
        Vector<Message> messages = waitQueuesToSend.get(messageId);
        if(messages != null) {
            for(Message message: messages) {
                TotalOrderMessage totalOrderMessage = (TotalOrderMessage)message;
                maxPriority = (maxPriority > totalOrderMessage.getPriority())?maxPriority:totalOrderMessage.getPriority();
            }
        }
        return maxPriority;
    }

    private Vector<Message> waitQueuesToDeliver = new Vector<Message>();

    public Vector<Message> getWaitQueueMessagesToDeliver() {
        return waitQueuesToDeliver;
    }

    public void putIntoWaitQueuesToDeliver(Message message) {
        waitQueuesToDeliver.add(message);
    }

    public void removeFromWaitQueuesToDeliver(Message message) { 
        waitQueuesToDeliver.remove(message);
    }

    public int getMaxPriorityInWaitQueuesToDeliver() {
        int maxPriority = 0;
        for(Message messageInDeliverQueue: waitQueuesToDeliver) {
            TotalOrderMessage totalOrderMessage = (TotalOrderMessage)messageInDeliverQueue;
            maxPriority = (maxPriority > totalOrderMessage.getPriority())?maxPriority:totalOrderMessage.getPriority();
        }
        return maxPriority;
    }

    @Override
    public void multiCastMessage(Message message) {
        TotalOrderMessage totalOrderMessage = new TotalOrderMessage(message.getInformation());
        totalOrderMessage.setId(SimulateUtil.getRandomIntBetween(0, Integer.MAX_VALUE));
        totalOrderMessage.setStep(1);
        totalOrderMessage.setSenderId(this.getId());

        // get all participants and send message to all of them
        List<Participant> receivers = this.getGroup().getParticipants();
        Channel reliableChannel = Channel.getReliableChannelInstance();
        for(Participant receiver: receivers) {
            reliableChannel.sendMessage(this, totalOrderMessage, receiver);
        }

        // record the message as sent
        this.recordSentMessage(totalOrderMessage);
    }
}

class TotalOrderParticipantReceiveThread extends Thread {
    private TotalOrderParticipant sender;
    private TotalOrderMessage message;
    private TotalOrderParticipant receiver;

    public TotalOrderParticipantReceiveThread(TotalOrderParticipant inputSender, TotalOrderMessage inputMessage, TotalOrderParticipant inputReceiver) {
        sender = inputSender;
        message = inputMessage;
        receiver = inputReceiver;
    }

    @Override
    public void run() {
        /***
         * in the handle method, all action of sending message through Channel
         * are asynchronous calling. So dead lock will never happen since 
         * since the logic in handle method will not wait on another synchronized
         * action
         */
        synchronized(receiver) {
            handleReceivedMessage();
        }
    }

    /***
     * the implementation of this method is the core of the
     * total ordered multicast algorithm
     * the received order is random, but the delivering
     * order must be the same as all other receivers
     */
    private void handleReceivedMessage() {
        if(message.getStep() == 1) {
            handleStep1Message();
        }
        else if(message.getStep() == 2) {
            handleStep2Message();
        }
        else if(message.getStep() == 3) {
            handleStep3Message();
        }
    }

    private void handleStep1Message() {
        // set the priority and mark is as undeliverable then add into the queues
        int newPriority = receiver.getMaxPriorityInWaitQueuesToDeliver() + 1;

        // this message is a clone from the channel, so
        // don't need to worry about that multi threads
        // modify on the same instance
        message.setPriority(newPriority);
        message.setDeliverable(false);
        receiver.putIntoWaitQueuesToDeliver(message);

        // send it back to sender
        message.setStep(2);
        Channel reliableChannel = Channel.getReliableChannelInstance();
        reliableChannel.sendMessage(receiver, message, sender);
    }

    private void handleStep2Message() {
        receiver.putIntoWaitQueuesToSend(message);

        // check if all messages are feedback
        if(receiver.getSizeOfWaitQueuesToSend(message.getId()) == receiver.getGroup().getNumberOfParticipants()) {
            // current thread is the final thread which get the last feedback of messages 
            message.setPriority(receiver.getMaxPriorityInWaitQueuesToSend(message.getId()));
            message.setStep(3);
            // get all participants and send message to all of them
            List<Participant> receivers = receiver.getGroup().getParticipants();
            Channel reliableChannel = Channel.getReliableChannelInstance();
            for(Participant receiver: receivers) {
                reliableChannel.sendMessage(receiver, message, sender); // this step, the receiver is the original sender
            }
        }
    }

    private void handleStep3Message() {
        Vector<Message> messages = receiver.getWaitQueueMessagesToDeliver();
        // find the message in queue of the same id
        TotalOrderMessage messageToFind = null;
        for(Message messageInQueue: messages) {
            if(((TotalOrderMessage)messageInQueue).getId() == message.getId()) {
                messageToFind = (TotalOrderMessage)messageInQueue;
                break;
            }
        }

        if(messageToFind == null) {
            /***
             * this case should not happen, it is not scientific
             */
            throw new RuntimeException("no messages in the wait to deliver queue with the id of the new received message which step = 3");
        }

        messageToFind.setPriority(message.getPriority());
        messageToFind.setDeliverable(true);

        Collections.sort(messages, new Comparator<Message>() {
            public int compare(Message message1, Message message2) {
                TotalOrderMessage totalOrderMessage1 = (TotalOrderMessage)message1;
                TotalOrderMessage totalOrderMessage2 = (TotalOrderMessage)message2;
                int priority1 = totalOrderMessage1.getPriority();
                int priority2 = totalOrderMessage2.getPriority();
                if(priority1 != priority2) {
                    return Integer.valueOf(priority1).compareTo(priority2);
                }
                else {
                    // tie breaking by sender id
                    return Integer.valueOf(totalOrderMessage1.getSenderId()).compareTo(totalOrderMessage2.getSenderId());
                }
            }
        });

        while(!messages.isEmpty()) {
            TotalOrderMessage firstMessage = (TotalOrderMessage)messages.get(0);
            if(firstMessage.isDeliverable()){
                receiver.deliverMessage(firstMessage);
                receiver.removeFromWaitQueuesToDeliver(firstMessage);
                continue;
            }
            else{
                // the first message is not deliverable 
                // so we have to continue to wait for new 
                // incoming message
                break;
            }
        }
    }
}
