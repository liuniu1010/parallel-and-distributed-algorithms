package org.kelly.code.model;

public class PaxosMessage extends Message {
    public final static int MESSAGE_TYPE_PREPARE_REQUEST = 1;
    public final static int MESSAGE_TYPE_ACK = 2;
    public final static int MESSAGE_TYPE_ACCEPT_REQUEST = 3;
    public final static int MESSAGE_TYPE_ACCEPT = 4;
    public final static int MESSAGE_TYPE_LEARNING = 5;

    private int type;
    private int N = -1;  // indicate the proposer's number, -1 means invalid
    private int V = -1;  // indicate the proposer's value, -1 means invalid
    private int anotherN = -1; // indicate another proposer's number which has been accepted by current participant, -1 means invalid
    private int anotherV = -1; // indicate another proposer's value which has been accepted by current participant, -1 means invalid

    private int senderId;

    public PaxosMessage(int inputType) {
        type = inputType;
        N = -1;
        V = -1;
        anotherN = -1;
        anotherV = -1;
    }

    public int getType() {
        return type;
    }

    public void setType(int inputType) {
        type = inputType;
    }

    public int getN() {
        return N;
    }

    public void setN(int inputN) {
        N = inputN;
    }

    public int getV() {
        return V;
    }

    public void setV(int inputV) {
        V = inputV;
    }

    public int getAnotherN() {
        return anotherN;
    }

    public void setAnotherN(int inputAnotherN) {
        anotherN = inputAnotherN;
    }

    public int getAnotherV() {
        return anotherV;
    }

    public void setAnotherV(int inputAnotherV) {
        anotherV = inputAnotherV;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int inputSenderId) {
        senderId = inputSenderId;
    }
}
