package com.icodesoftware;

import com.google.gson.JsonObject;

import java.util.Map;

public interface JsonSerializable {

    default Map<String,Object> toMap(){ return null;}
    default void fromMap(Map<String,Object> properties){}
    JsonObject toJson();
    default Recoverable.DataBuffer toDataBuffer(){ return null;}
}
