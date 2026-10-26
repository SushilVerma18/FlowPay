package com.cfs.FlowPay.common;


import java.util.UUID;

public class Ids {

    private Ids()
    {

    }

    public static String next(String prefix)
    {
        return prefix+"_"+ UUID.randomUUID().toString().replace("-","");
    }
}
