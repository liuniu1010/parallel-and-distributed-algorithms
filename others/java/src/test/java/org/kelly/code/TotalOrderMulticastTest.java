package org.kelly.code;

import org.kelly.code.util.SimulateUtil;
import org.kelly.code.model.*;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.List;
import java.util.ArrayList;
import java.util.Vector;

/**
 * Unit test for simple App.
 */
public class TotalOrderMulticastTest 
    extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public TotalOrderMulticastTest( String testName ) {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite( TotalOrderMulticastTest.class );
    }

    public void testMulticast() {
        // construct the group and participants
        int participantNumber = 8;
        Group group = new Group();
        for(int i = 0;i < participantNumber;i++) {
            TotalOrderParticipant participant = new TotalOrderParticipant();
            participant.setId(i);
            participant.setGroup(group);
            group.addParticipant(participant);
        }

        // multicast messages for each participant
        int messageNumber = 5;
        for(Participant sender: group.getParticipants()) {
            for(int i = 0;i < messageNumber;i++) {
                Message message = new Message();
                message.setInformation("message" + i);
                sender.multiCastMessage(message);
            }
        }

/*
        try {
            Thread.sleep(5000);
        }
        catch(InterruptedException ie) {
            ie.printStackTrace();
        }
*/

        for(Participant participant: group.getParticipants()) {
            participant.waitUntilTerminate();
        }
 
        // check if the delivering order are the same for any two pair of participants
        List<Participant> participants = group.getParticipants();
        for(int i = 0;i < participants.size();i++) {
            Participant par1 = participants.get(i);
            for(int j = i + 1; j < participants.size();j++) {
                Participant par2 = participants.get(j);
                Vector<Message> deliveredMessages1 = par1.getDeliveredMessages();
                Vector<Message> deliveredMessages2 = par2.getDeliveredMessages();
                assertTrue(deliveredMessages1 != null);
                assertTrue(deliveredMessages2 != null);
                assertTrue(deliveredMessages1.size() == deliveredMessages2.size());
                for(int k = 0;k < deliveredMessages1.size();k++) {
                    TotalOrderMessage totalOrderMessage1 = (TotalOrderMessage)deliveredMessages1.get(k);
                    TotalOrderMessage totalOrderMessage2 = (TotalOrderMessage)deliveredMessages2.get(k);
                    assertTrue(totalOrderMessage1.getId() == totalOrderMessage2.getId());
                }
            }
        }


/*
        System.out.println("print participants:");
        for(Participant participant: group.getParticipants()) {
            System.out.println(participant);
        }
*/
    }
}
