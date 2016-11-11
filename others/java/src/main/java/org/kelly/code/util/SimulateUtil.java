package org.kelly.code.util;

import java.util.Random;

public class SimulateUtil {

    private static Random random = new Random(); 

    // generate a random number n which satisfy from <= n < to
    public static int getRandomIntBetween(int from, int to) {
        int rawRandom = Math.abs(random.nextInt());
        return (rawRandom)%(to - from) + from;
    }
}
