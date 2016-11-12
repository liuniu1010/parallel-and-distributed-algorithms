package org.kelly.code;

import org.kelly.code.util.SimulateUtil;
import org.kelly.code.model.*;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class FIFOMulticastTest 
    extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public FIFOMulticastTest( String testName ) {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite( FIFOMulticastTest.class );
    }

    public void testMulticast() {
        Group group = new Group();
        for(int i = 0;i < 5;i++) {
            FIFOParticipant participant = new FIFOParticipant();
            participant.setId(i);
            participant.setGroup(group);
            group.addParticipant(participant);
        }

        for(Participant sender: group.getParticipants()) {
            for(int i = 0;i < 8;i++) {
                Message message = new Message();
                message.setInformation("message" + i);
                sender.multiCastMessage(message);
            }
        }

        try {
            Thread.sleep(5000);
        }
        catch(InterruptedException ie) {
            ie.printStackTrace();
        }

        System.out.println("print participants:");
        for(Participant participant: group.getParticipants()) {
            System.out.println(participant);
        }

    }
}
