package org.kelly.code.model;

/***
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
public class TotalOrderMessage extends Message {
    private int id;  // identify the unique id, we can generate a random id for each message
    private int senderId;
    private int step; // to indicate the message is in which step for the Total ordered algorithm
    private int priority;
    private boolean deliverable;

    public TotalOrderMessage(String inputInformation) {
        this.setInformation(inputInformation);
    }

    public int getId() {
        return id;
    }

    public void setId(int inputId) {
        id = inputId;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int inputSenderId) {
        senderId = inputSenderId;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int inputStep) {
        step = inputStep;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int inputPriority) {
        priority = inputPriority;
    }

    public boolean isDeliverable() {
        return deliverable;
    }

    public void setDeliverable(boolean inputDeliverable) {
        deliverable = inputDeliverable;
    }

    @Override
    public String toString() {
        String str = "FIFOMessage: information = " + super.getInformation();
        str += ", senderId = " + senderId;
        str += ", step = " + step;
        str += ", priority = " + priority;
        str += ", deliverable = " + deliverable;
        return str;
    }
}
