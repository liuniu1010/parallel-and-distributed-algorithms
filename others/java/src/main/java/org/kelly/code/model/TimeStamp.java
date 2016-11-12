package org.kelly.code.model;

/***
 * this class simulate the logic clock timestamp which
 * contains id and tick represent the participant id 
 * and time tick of the participant
 */
public class TimeStamp {
    private int id;
    private int tick;

    public int getId() {
        return id;
    }

    public void setId(int inputId) {
        id = inputId;
    }

    public int getTick() {
        return tick;
    }

    public void setTick(int inputTick) {
        tick = inputTick;
    }
}
