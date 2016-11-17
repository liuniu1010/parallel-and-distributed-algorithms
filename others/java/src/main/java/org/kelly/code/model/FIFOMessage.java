package org.kelly.code.model;

public class FIFOMessage extends Message {
    private TimeStamp timeStamp;

    public FIFOMessage(String inputInformation, TimeStamp inputTimeStamp) {
        this.setInformation(inputInformation);
        timeStamp = inputTimeStamp;
    }

    public TimeStamp getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(TimeStamp inputTimeStamp) {
        timeStamp = inputTimeStamp;
    }

    @Override
    public String toString() {
        String str = "FIFOMessage: information = " + super.getInformation();
        str += ", timeStamp: " + timeStamp;
        return str;
    }
}
