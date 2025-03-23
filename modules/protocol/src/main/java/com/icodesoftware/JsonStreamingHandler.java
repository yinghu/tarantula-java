package com.icodesoftware;


public interface JsonStreamingHandler {

    void onBeginObject(String tag);
    void onEndObject(String tag);

    void onBeginArray(String tag);
    void onEndArray(String tag);

    void onNumber(String tag,Number value);
    void onString(String tag,String value);
    void onBoolean(String tag,boolean value);
    void onNull(String tag);
}
