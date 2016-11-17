package org.kelly.code.model;

import java.io.Serializable;

public class Message implements Serializable {
    private String information;
    private Probe probe;

    public String getInformation() {
        return information;
    }

    public void setInformation(String inputInformation) {
        information = inputInformation;
    }

    public Probe getProbe() {
        return probe;
    }

    public void setProbe(Probe inputProbe) {
        probe = inputProbe;
    }
}
