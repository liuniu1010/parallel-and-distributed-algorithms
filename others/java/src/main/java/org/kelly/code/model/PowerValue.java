package org.kelly.code.model;

/***
 * power value represent a value of base^2
 * it can be used in weighted reference
 */ 
public class PowerValue {
    private int base;

    public PowerValue(int inputBase) {
        base = inputBase;
    }

    public int getBase() {
        return base;
    }

    public PowerValue[] split() {
        PowerValue[] splits = new PowerValue[2];
        PowerValue split1 = new PowerValue(base - 1);
        PowerValue split2 = new PowerValue(base - 1);
        splits[0] = split1;
        splits[1] = split2;

        return splits;
    }
}
