package com.tarantula.test.integration;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


public class LoadResult {


    static AtomicInteger totalSuccessRegister = new AtomicInteger(0);
    static AtomicInteger totalFailureRegister = new AtomicInteger(0);

    static AtomicInteger totalSuccessLogin = new AtomicInteger(0);
    static AtomicInteger totalFailureLogin = new AtomicInteger(0);

    static AtomicInteger totalSuccessLeave = new AtomicInteger(0);
    static AtomicInteger totalFailureLeave = new AtomicInteger(0);
    static AtomicInteger totalSuccessJoin = new AtomicInteger(0);
    static AtomicInteger totalFailureJoin = new AtomicInteger(0);

    static AtomicInteger totalSuccessPlay = new AtomicInteger(0);
    static AtomicInteger totalFailurePlay = new AtomicInteger(0);

    static AtomicInteger totalFailureOther = new AtomicInteger(0);

    static AtomicLong totalUDPBytesSent = new AtomicLong(0);
    static AtomicLong totalUDPBytesReceived = new AtomicLong(0);

    static AtomicInteger totalSuccessUDPSent = new AtomicInteger(0);
    static AtomicInteger totalSuccessUDPReceived = new AtomicInteger(0);

    static AtomicInteger totalUDPReceiveTimeout = new AtomicInteger(0);


    static AtomicInteger totalRounds = new AtomicInteger(0);

    static AtomicInteger totalHttpRequestCount = new AtomicInteger(0);

    static AtomicLong totalHttpRequestTime = new AtomicLong(0);

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
            bw.write("###### Load Test Summary ######\n");
            bw.write("Server Host ["+host+"]\n");
            bw.write("Batch Size ["+batch+"]\n");
            bw.write("Pool Size ["+poolSize+"]\n");
            bw.write("Player Prefix ["+playerPrefix+"]\n");
            bw.write("Test UDP Enabled ["+udpTested+"]\n");
            bw.write("Test UDP Receive Timeout ["+udpReceiveTimeout+"]\n");
            bw.write("Test UDP Duration ["+udpTestDuration+"]\n");
            bw.write("Start time ["+startTime.format(DateTimeFormatter.ISO_DATE_TIME)+"]\n");
            bw.write("End time ["+localDateTime.format(DateTimeFormatter.ISO_DATE_TIME)+"]\n\n");
            bw.write("###### HTTP Operation Summary ######\n");
            bw.write("Total Rounds ["+totalRounds.get()+"]\n");
            bw.write("Total Failure Other ["+totalFailureOther.get()+"]\n");
            bw.write("Total Success Register Count ["+totalSuccessRegister.get()+"]\n");
            bw.write("Total Failure Register Count ["+totalFailureRegister.get()+"]\n");
            bw.write("Total Success Login Count ["+totalSuccessLogin.get()+"]\n");
            bw.write("Total Failure Login Count ["+totalFailureLogin.get()+"]\n");
            bw.write("Total Success Join Count ["+totalSuccessJoin.get()+"]\n");
            bw.write("Total Failure Join Count ["+totalFailureJoin.get()+"]\n");
            bw.write("Total Success Leave Count ["+totalSuccessLeave.get()+"]\n");
            bw.write("Total Failure Leave Count ["+totalFailureLeave.get()+"]\n");
            bw.write("Total Http Request Count ["+totalHttpRequestCount.get()+"]\n");
            bw.write("Average HTTP Request Duration (ms) ["+(totalHttpRequestTime.get()/totalHttpRequestCount.get())+"]\n\n");
            bw.write("###### UDP Operation Summary ######\n");
            bw.write("Total Success Play Count ["+totalSuccessPlay.get()+"]\n");
            bw.write("Total Failure Play Count ["+totalFailurePlay.get()+"]\n");
            bw.write("Total Success UDP Sent Count ["+totalSuccessUDPSent.get()+"]\n");
            bw.write("Total Success UDP Received Count ["+totalSuccessUDPReceived.get()+"]\n");
            bw.write("Total Bytes UDP Sent ["+totalUDPBytesSent.get()+"]\n");
            bw.write("Total Bytes UDP Received ["+totalUDPBytesReceived.get()+"]\n");
            bw.write("Total UDP Receive Timeout Count ["+totalUDPReceiveTimeout.get()+"]\n");
            bw.close();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
}

