package org.kelly.code.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;

import org.kelly.code.model.*;

public class SimulateUtil {

    private static Random random = new Random(); 

    // generate a random number n which satisfy from <= n < to
    public static int getRandomIntBetween(int from, int to) {
        int rawRandom = Math.abs(random.nextInt());
        return (rawRandom)%(to - from) + from;
    }

    public static byte[] objToBytes(Object obj) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(obj);
        return bos.toByteArray();
    }

    public static Object bytesToObj(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInputStream in = new ObjectInputStream(bis);
        return in.readObject();
    }

    public static Object clone(Object origObject) {
        try {
            return bytesToObj(objToBytes(origObject));
        }
        catch(IOException ioe) {
            ioe.printStackTrace();
            throw new RuntimeException(ioe);
        }
        catch(ClassNotFoundException cfe) {
            cfe.printStackTrace();
            throw new RuntimeException(cfe);
        }
    }

    public static List<PowerValue> shrinkPowerValues(List<PowerValue> inputPowerValues) {
        // generate a copy to prevent the original List being changed
        List<PowerValue> powerValues = new ArrayList<PowerValue>();
        powerValues.addAll(inputPowerValues);

        return innerShrinkPowerValues(powerValues);
    }

    private static List<PowerValue> innerShrinkPowerValues(List<PowerValue> powerValues) {
        if(powerValues.size() <= 1) {
            return powerValues;
        }

        Collections.sort(powerValues, new Comparator<PowerValue>() {
            public int compare(PowerValue powerValue1, PowerValue powerValue2) {
                return Integer.valueOf(powerValue1.getIndex()).compareTo(powerValue2.getIndex());
            }
        });

        PowerValue powerValue0 = powerValues.get(0);
        PowerValue powerValue1 = powerValues.get(1);
        if(powerValue0.getIndex() != powerValue1.getIndex()) {
            // nothink to shink, return directly
            return powerValues;
        }
        else{
            // merge the two powerValue as one
            PowerValue mergedPowerValue = new PowerValue(powerValue0.getIndex() + 1);
            powerValues.remove(powerValue0);
            powerValues.remove(powerValue1);
            powerValues.add(0, mergedPowerValue);

            return innerShrinkPowerValues(powerValues);
        }
    }

    public static boolean isEquals(List<PowerValue> powerValues1, List<PowerValue> powerValues2) {
        List<PowerValue> shrinked1 = shrinkPowerValues(powerValues1);
        List<PowerValue> shrinked2 = shrinkPowerValues(powerValues2);

        if(shrinked1.size() != shrinked2.size()) {
            return false;
        }

        boolean isEquals = true;
        for(int i = 0;i < shrinked1.size();i++) {
            PowerValue powerValue1 = shrinked1.get(i);
            PowerValue powerValue2 = shrinked2.get(i);

            if(powerValue1.getIndex() != powerValue2.getIndex()) {
                isEquals = false;
                break;
            }
        }
        return isEquals;
    }

    public static boolean isEquals(PowerValue powerValue, List<PowerValue> powerValues) {
        List<PowerValue> powerValues1 = new ArrayList<PowerValue>();
        powerValues1.add(powerValue);

        return isEquals(powerValues1, powerValues);
    }
}
