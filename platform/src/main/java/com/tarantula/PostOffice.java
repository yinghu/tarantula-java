package com.tarantula;

public interface PostOffice{

    void onNotification(byte[] data,String label);

    void onMessage(String from,String to,byte[] message);

    void registerMessageListener(String subscription,EventListener messageListener);
    void unregisterMessageListener(String subscription);

}
