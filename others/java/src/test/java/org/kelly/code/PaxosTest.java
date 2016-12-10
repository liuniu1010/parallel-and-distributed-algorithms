package org.kelly.code;

import java.util.List;
import java.util.ArrayList;

import org.kelly.code.util.SimulateUtil;
import org.kelly.code.model.*;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class PaxosTest 
    extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public PaxosTest( String testName ) {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite( PaxosTest.class );
    }

    /***
     * we should test if the paxos algorihtm
     * can always acheive agreement even under
     * unreliable channel.
     * the achieve time cannot be gaurantee under a value
     * for some special case, it might take a long time
     * to achieve the agreement.
     */
    public void testPaxos() {
        // construct the group and participants
        int participantNumber = 19;
        Group group = new Group();
        for(int i = 0;i < participantNumber;i++) {
            PaxosParticipant participant = new PaxosParticipant();
            participant.setId(i);
            participant.setProposedValue(i);
            participant.setGroup(group);
            group.addParticipant(participant);
        }

        // select the first 3 of them to start propose
        for(int i = 0;i < 5;i++) {
            PaxosParticipant participant = (PaxosParticipant)group.getParticipants().get(i);
            participant.startPropose();
        }

        // check all pariticpant's status periodically
        try {
            while(true) {
                Thread.sleep(1500);
                int endNumber = check(group);
                if(endNumber == group.getNumberOfParticipants()) {
                    break;
                }
            }
        }
        catch(InterruptedException ie) {
            throw new RuntimeException(ie);
        }

        PaxosParticipant participant = (PaxosParticipant)group.getParticipants().get(0);
        System.out.println("agreement achieved, the chosen value = " + participant.getChosenValue());
    }

    private int check(Group group) {
        int chosenValue = -1;
        int endNumber = 0;
        for(Participant par: group.getParticipants()) {
            PaxosParticipant participant = (PaxosParticipant)par;
            if(participant.getChosenValue() >= 0) {
                // current participant has a chosen value, it is in end stage
                endNumber ++;

                // compare if there are conflict
                if(chosenValue >= 0) {
                    if(chosenValue != participant.getChosenValue()) {
                        throw new RuntimeException("there are two participant get two different chosen value, failed!");
                    }
                }
                else {
                    chosenValue = participant.getChosenValue();
                }
            }
        }

        return endNumber;
    }
}
