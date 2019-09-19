package com.alice.utils;


import java.math.BigInteger;

public class UnitUtlis {

    public static String hex2decimal(String hex) {
        BigInteger integer = new BigInteger(hex, 16);
        return integer.toString();
    }
}
