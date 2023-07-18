package com.tarantula.platform.configuration;

import com.google.gson.JsonObject;

public class AppleStoreKey implements VendorValidator{

    private JsonObject header;
    public AppleStoreKey(JsonObject payload){
        this.header = payload;
    }

    public boolean isSandbox(){
        return header.get("IsSandbox").getAsBoolean();
    }
    public String secureKey(){
        return header.get("SecretKey").getAsString();
    }
}
