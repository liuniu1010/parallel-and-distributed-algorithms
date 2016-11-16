package org.kelly.code.model;

import java.io.Serializable;

/***
 * this class simulate the logic clock timestamp which
 * contains id and tick represent the participant id 
 * and time tick of the participant
 */
public class TimeStamp implements Serializable {
    private int id;
    private int tick;

    public TimeStamp(int inputId, int inputTick) {
        id = inputId;
        tick = inputTick;
    }

    public int getId() {
        return id;
    }

    public int getTick() {
        return tick;
    }

    public void tickIncrease() {
        tick++;
    }

    public TimeStamp clone() {
        TimeStamp clone = new TimeStamp(id, tick);
        return clone;
    }

    @Override
    public String toString() {
        String str = "id = " + id;
        str += ", tick = " + tick;
        return str;
    }
}
