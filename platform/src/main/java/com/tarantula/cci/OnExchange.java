package com.tarantula.cci;

import com.tarantula.EventListener;

import java.io.InputStream;

/**
 * Created by yinghu lu on 4/29/2018.
 */
public interface OnExchange extends EventListener{
    String id();
    String path();
    String method();
    String header(String name);

    byte[] payload();
    String query();
    String remoteAddress();

    boolean streaming();
    void onError(Exception ex,String message);

    default InputStream onStream(){ return null;}
}
