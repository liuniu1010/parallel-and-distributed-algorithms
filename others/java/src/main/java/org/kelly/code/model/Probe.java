package org.kelly.code.model;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

/***
 * this is the probe with appended with the Message
 * when each participant send a message to another 
 * participant to collect some necessary information
 */
public class Probe implements Serializable {
    // record the hop id list who init and forward the probe
    private List<Integer> sendPath = new ArrayList<Integer>();
    private PowerValue weight;

    public Probe(PowerValue inputWeight) {
        weight = inputWeight;
    }

    public PowerValue getWeight() {
        return weight;
    }

    public void addHopId(int hopId) {
        sendPath.add(hopId); 
    }

    public int getHopNumber() {
        return sendPath.size();
    }

    public int getLastHopId() {
        return sendPath.get(sendPath.size() - 1);
    }

    public void removeLastHop() {
        sendPath.remove(getLastHopId());
    }
}
