package com.icodesoftware;


public interface JsonStreamingHandler {

    default void onBeginObject(String tag,int index){}
    default boolean onEndObject(String tag,int index){ return false;}

    default void onBeginArray(String tag,int index){}
    default boolean onEndArray(String tag,int index){ return false;}

    default boolean onNumber(String tag,Number value,int index){return false;}
    default boolean onString(String tag,String value,int index){ return false;}
    default boolean onBoolean(String tag,boolean value,int index){ return false;}
    default boolean onNull(String tag,int index){ return false;}
}
