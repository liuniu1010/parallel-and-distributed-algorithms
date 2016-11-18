package org.kelly.code.model;

import java.util.Vector;
import java.util.List;
import java.util.ArrayList;

import org.kelly.code.util.SimulateUtil;

public abstract class Participant {
    abstract public void receiveMessage(Participant sender, Message message);

    abstract public void deliverMessage(Message message);

    abstract public void multiCastMessage(Message message);

    private int id;
    private Group group;

    public int getId() {
        return id;
    }

    public void setId(int inputId) {
        id = inputId;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group inputGroup) {
        group = inputGroup;
    }

    /***
     * these three variables are to record all sent messages, received messages
     * and delivered messages, the order in Vector will indicates the real 
     * happened order in running time
     */
    private Vector<Message> sentMessages = new Vector<Message>();
    private Vector<Message> receivedMessages = new Vector<Message>();
    private Vector<Message> deliveredMessages = new Vector<Message>();

    public Vector<Message> getSentMessages() {
        return sentMessages;
    }

    public void recordSentMessage(Message inputMessage) {
        sentMessages.add(inputMessage);
    }

    public Vector<Message> getReceivedMessages() {
        return receivedMessages;
    }

    public void recordReceivedMessage(Message inputMessage) {
        receivedMessages.add(inputMessage);
    }

    public Vector<Message> getDeliveredMessages() {
        return deliveredMessages;
    }

    public void recordDeliveredMessage(Message inputMessage) {
        deliveredMessages.add(inputMessage);
    }

    /***
     * this two variables are to record all probes generated by this 
     * participant and probes finally returned to this participant.
     * probes can be use to collect necessary information
     */
    private Vector<Probe> generatedProbes = new Vector<Probe>();
    private Vector<Probe> returnedProbes = new Vector<Probe>();
    
    public Vector<Probe> getGeneratedProbes() {
        return generatedProbes;
    }
    
    public void recordGeneratedProbes(Probe probe) {
        generatedProbes.add(probe);
    }

    public Vector<Probe> getReturnedProbes() {
        return returnedProbes;
    }

    public void recordReturnedProbes(Probe probe) {
        returnedProbes.add(probe);
    }

    /***
     * this method is to check if all tasks initiated
     * by this participant have been done.
     * The background logic is based on total weight of probes
     * if all weight in generated probes are the same as
     * all weight in returned probes, that means all
     * tasks initiated by this probe have been completed.
     */
    public boolean isTerminate() {
        List<PowerValue> generatedPowerValues = new ArrayList<PowerValue>();
        List<PowerValue> returnedPowerValues = new ArrayList<PowerValue>();
        
        for(Probe probe: generatedProbes) {
            generatedPowerValues.add(probe.getWeight());
        }

        for(Probe probe: returnedProbes) {
            returnedPowerValues.add(probe.getWeight());
        }

        return SimulateUtil.isEquals(generatedPowerValues, returnedPowerValues);
    }

    /***
     * this method can be used for elegant termination
     * the caller just wait until the tasks of this
     * participant are all completed.
     */
    public void waitUntilTerminate() {
        while(!isTerminate()) {
            try {
                Thread.sleep(1000);
            }
            catch(InterruptedException ie) {
                ie.printStackTrace();
                throw new RuntimeException(ie);
            }
        }
    }

    @Override
    public String toString() {
        String str = "id = " + id;
        str += "\nmulticast messages";
        for(Message message: sentMessages) {
            str += "\n" + message;
        }

        str += "\nreceived messages";
        for(Message message: receivedMessages) {
            str += "\n" + message;
        }

        str += "\ndelivered messages";
        for(Message message: deliveredMessages) {
            str += "\n" + message;
        }
        return str;
    }
}
