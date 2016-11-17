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
public class PowerValueTest 
    extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public PowerValueTest( String testName ) {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite( PowerValueTest.class );
    }

    public void testPowerValue() {
        PowerValue powerValue1 = new PowerValue(100);

        List<PowerValue> powerValues = new ArrayList<PowerValue>();
        powerValues.add(powerValue1);

        // split the powerValues in random times
        int randomTime = SimulateUtil.getRandomIntBetween(5, 10);
        while(randomTime > 0) {
            // random select one element to split
            int randomItem = SimulateUtil.getRandomIntBetween(0, powerValues.size());
            PowerValue toSplit = powerValues.get(randomItem);
            PowerValue[] splitted = toSplit.split();
            powerValues.remove(toSplit);
            powerValues.add(splitted[0]);
            powerValues.add(splitted[1]);
            randomTime--;
        }
/*
        System.out.println("original powerValue.index = " + powerValue1.getIndex());
        for(PowerValue powerValue: powerValues) {
            System.out.println("splittedowerValue.index = " + powerValue.getIndex());
        }
*/
        assertTrue(SimulateUtil.isEquals(powerValue1, powerValues));
    }
}
