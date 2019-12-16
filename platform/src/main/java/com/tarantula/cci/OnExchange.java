package com.tarantula.cci;

import com.tarantula.EventListener;

import java.io.InputStream;

/**
 * Updated by yinghu lu on 12/16/2019.
 */
public interface OnExchange extends EventListener{
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

    default boolean streaming(){
        return false;
    }
    default boolean oneWay(){
        return false;
    }
    default void onError(Exception ex,String message){}

    default InputStream onStream(){ return null;}
}
