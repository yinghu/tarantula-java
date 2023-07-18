package com.tarantula.platform.configuration;

import com.google.gson.JsonObject;

public class FacebookLogin implements VendorValidator{

    private JsonObject header;
    public FacebookLogin(JsonObject payload){
        this.header = payload;
    }
    public String appName(){
        return header.get("AppName").getAsString();
    }
    public String appId(){
        return header.get("AppId").getAsString();
    }
    public String secretKey(){
        return header.get("SecretKey").getAsString();
    }


}
