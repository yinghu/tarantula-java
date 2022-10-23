package com.tarantula.test.integration;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


public class LoadResult {


    static AtomicInteger totalSuccessPresence = new AtomicInteger(0);
    static AtomicInteger totalFailurePresence = new AtomicInteger(0);

    static AtomicInteger totalSuccessJoin = new AtomicInteger(0);
    static AtomicInteger totalFailureJoin = new AtomicInteger(0);

    static AtomicInteger totalSuccessPlay = new AtomicInteger(0);
    static AtomicInteger totalFailurePlay = new AtomicInteger(0);

    static AtomicInteger totalFailureOther = new AtomicInteger(0);

    static AtomicLong totalUDPBytesSent = new AtomicLong(0);
    static AtomicLong totalUDPBytesReceived = new AtomicLong(0);

    static AtomicInteger totalRounds = new AtomicInteger(0);


    static int batch;
    static int poolSize;

    static String host;

    static boolean udpTested;
    static int udpReceiveTimeout;
    static long udpTestDuration;
    static String playerPrefix;
    static LocalDateTime startTime;


    public static void print(){
        try{
            LocalDateTime localDateTime = LocalDateTime.now();
            FileOutputStream fos = new FileOutputStream(localDateTime.format(DateTimeFormatter.ISO_DATE_TIME)+".txt");
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
            bw.write("Server Host ["+host+"]\n");
            bw.write("Batch Size ["+batch+"]\n");
            bw.write("Pool Size ["+poolSize+"]\n");
            bw.write("Player Prefix ["+playerPrefix+"]\n");
            bw.write("Test UDP Enabled ["+udpTested+"]\n");
            bw.write("Test UDP Receive Timeout ["+udpReceiveTimeout+"]\n");
            bw.write("Test UDP Duration ["+udpTestDuration+"]\n");
            bw.write("Start time ["+startTime.format(DateTimeFormatter.ISO_DATE_TIME)+"]\n");
            bw.write("End time ["+localDateTime.format(DateTimeFormatter.ISO_DATE_TIME)+"]\n");
            bw.write("Total Rounds ["+totalRounds.get()+"]\n");
            bw.write("Total Failure Other ["+totalFailureOther.get()+"]\n");
            bw.write("Total Success Presence ["+totalSuccessPresence.get()+"]\n");
            bw.write("Total Failure Presence ["+totalFailurePresence.get()+"]\n");
            bw.write("Total Success Join ["+totalSuccessJoin.get()+"]\n");
            bw.write("Total Failure Join ["+totalFailureJoin.get()+"]\n");
            bw.write("Total Success Play ["+totalSuccessPlay.get()+"]\n");
            bw.write("Total Failure Play ["+totalFailurePlay.get()+"]\n");
            bw.write("Total Bytes UDP Sent ["+totalUDPBytesSent.get()+"]\n");
            bw.write("Total Bytes UDP Received["+totalUDPBytesReceived.get()+"]\n");
            bw.close();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
}

