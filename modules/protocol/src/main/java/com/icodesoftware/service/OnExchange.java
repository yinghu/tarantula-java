package com.icodesoftware.service;

import com.icodesoftware.EventListener;

import java.io.InputStream;

public interface OnExchange extends EventListener {
    String id();
    String path();
    String method();
    String header(String name);

    byte[] payload();
    default String query(){
        return null;
    }
    default String remoteAddress(){
        return "0.0.0.0";
    }

    default void onError(Exception ex,String message){}

    default InputStream onStream(){ return null;}
    default void onStream(InputStream inputStream){}
    default void close(){}
}
