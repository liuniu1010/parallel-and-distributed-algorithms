package org.kelly.code;

import org.kelly.code.util.SimulateUtil;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class SimulateUtilTest 
    extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public SimulateUtilTest( String testName ) {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite( SimulateUtilTest.class );
    }

    public void testRandom() {
        int from = 10;
        int to = 20;
        for(int i = 0;i < 20;i ++) {
            int random = SimulateUtil.getRandomIntBetween(from, to);
            assertTrue(random >= from);
            assertTrue(random < to);
        }
    }
}
