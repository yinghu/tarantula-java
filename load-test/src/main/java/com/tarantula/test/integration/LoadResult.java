package com.tarantula.test.integration;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class LoadResult {
    static AtomicLong totalBytesReceived = new AtomicLong(0);
    static AtomicLong totalBytesUDPReceived = new AtomicLong(0);
    static AtomicInteger totalRoundTrip1_10 = new AtomicInteger(0);
    static AtomicInteger totalRoundTrip11_50 = new AtomicInteger(0);
    static AtomicInteger totalRoundTrip51_100 = new AtomicInteger(0);
    static AtomicInteger totalRoundTrip101_500 = new AtomicInteger(0);
    static AtomicInteger totalRoundTripMore500 = new AtomicInteger(0);

    public static String print(){
        return "(Total bytes)"+(totalBytesReceived.get()+totalBytesUDPReceived.get())+"(1-10)"+totalRoundTrip1_10.get()+"(11-50)"+totalRoundTrip11_50+"(51-100)"+totalRoundTrip51_100+"(101-500)"+totalRoundTrip101_500+"(>500)"+totalRoundTripMore500;
    }
}

