package org.kelly.code.model;

public class PaxosMessage extends Message {
    public final static int MESSAGE_TYPE_PREPARE_REQUEST = 1;
    public final static int MESSAGE_TYPE_ACK = 2;
    public final static int MESSAGE_TYPE_ACCEPT_REQUEST = 3;
    public final static int MESSAGE_TYPE_ACCEPT = 4;

    private int type;
    private int N;
    private int V;

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
}
