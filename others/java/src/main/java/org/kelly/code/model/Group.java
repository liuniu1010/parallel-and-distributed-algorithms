package org.kelly.code.model;

import java.util.List;
import java.util.ArrayList;

public class Group {
    List<Participant> participants;

    public List<Participant> getParticipants() {
        return participants;
    }

    public void setParticipant(List<Participant> inputParticipants) {
        participants = inputParticipants;
    }

    public void addParticipant(Participant inputParticipant) {
        if(participants == null) {
            participants = new ArrayList<Participant>();
        }

        participants.add(inputParticipant);
    }

    public void removeParticipant(Participant inputParticipant) {
        if(participants != null) {
            participants.remove(inputParticipant);
        }
    }

    public int getNumberOfParticipants() {
        return (participants == null)?0:participants.size();
    }
}
