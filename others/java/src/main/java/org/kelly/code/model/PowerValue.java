package org.kelly.code.model;

import java.io.Serializable;

/***
 * power value represent a value of 2^index
 * it can be used in weighted reference
 */ 
public class PowerValue implements Serializable {
    private int index;

    public PowerValue(int inputIndex) {
        index = inputIndex;
    }

    public int getIndex() {
        return index;
    }

    public PowerValue[] split() {
        PowerValue[] splits = new PowerValue[2];
        PowerValue split1 = new PowerValue(index - 1);
        PowerValue split2 = new PowerValue(index - 1);
        splits[0] = split1;
        splits[1] = split2;

        return splits;
    }
}
