package com.icodesoftware;


public interface JsonStreamingHandler {

    void onBeginObject(String tag,int index);
    void onEndObject(String tag,int index);

    void onBeginArray(String tag,int index);
    void onEndArray(String tag,int index);

    void onNumber(String tag,Number value,int index);
    void onString(String tag,String value,int index);
    void onBoolean(String tag,boolean value,int index);
    void onNull(String tag,int index);
}
