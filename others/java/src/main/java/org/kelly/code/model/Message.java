package org.kelly.code.model;

import java.io.Serializable;

public class Message implements Serializable {
    private String information;

    public String getInformation() {
        return information;
    }

    public void setInformation(String inputInformation) {
        information = inputInformation;
    }
}
