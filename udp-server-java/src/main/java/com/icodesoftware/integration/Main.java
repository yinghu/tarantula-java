package com.icodesoftware.integration;

import com.icodesoftware.integration.udp.UDPReceiver;

public class Main {
    public static void main(String[] args) throws Exception{
        UDPReceiver udpReceiver = new UDPReceiver("10.0.0.192",16393);
        udpReceiver.start();
        Thread t = new Thread(udpReceiver);
        t.start();
        System.out.println("UDP STARTED");
        t.join();
        udpReceiver.stop();
        System.out.println("UDP STOPPED");
    }
}
