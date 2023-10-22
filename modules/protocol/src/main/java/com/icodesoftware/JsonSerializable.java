package com.icodesoftware;

import com.google.gson.JsonObject;

import java.util.Map;

public interface JsonSerializable {

    Map<String,Object> toMap();
    void fromMap(Map<String,Object> properties);
    JsonObject toJson();
}
