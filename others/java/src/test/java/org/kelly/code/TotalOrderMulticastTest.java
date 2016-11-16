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
        int participantNumber = 2;
        Group group = new Group();
        for(int i = 0;i < participantNumber;i++) {
            TotalOrderParticipant participant = new TotalOrderParticipant();
            participant.setId(i);
            participant.setGroup(group);
            group.addParticipant(participant);
        }

        // multicast messages for each participant
        int messageNumber = 4;
        for(Participant sender: group.getParticipants()) {
            for(int i = 0;i < messageNumber;i++) {
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

/*        // check if the delivering order is the same as sending order
        for(Participant receiver: group.getParticipants()) {
            for(Participant sender: group.getParticipants()) {
                // get all deliveredMessage from this sender
                List<Message> deliveredMessagesOfTheSender = new ArrayList<Message>();

                Vector<Message> allDeliveredMessages = receiver.getDeliveredMessages();
                for(Message message: allDeliveredMessages) {
                    FIFOMessage fifoMessage = (FIFOMessage)message;
                    if(fifoMessage.getTimeStamp().getId() == sender.getId()) {
                        deliveredMessagesOfTheSender.add(fifoMessage);
                    }
                }
                // assert the deliver orders are right
                for(int i = 0;i < messageNumber;i++) {
                    Message message = deliveredMessagesOfTheSender.get(i);
                    assertTrue(message.getInformation().equals("message" + i));
                } 
            } 
        }
*/
        System.out.println("print participants:");
        for(Participant participant: group.getParticipants()) {
            System.out.println(participant);
        }

    }
}
