package org.kelly.code.util;

import java.util.Random;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;

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
}
