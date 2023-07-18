package com.tarantula.platform.configuration;

import com.google.gson.JsonObject;

public class S3Client {

    private JsonObject header;
    public S3Client(JsonObject payload){
        this.header = payload;
    }
    public String region(){
        return header.get("Region").getAsString();
    }
    public String bucket(){
        return header.get("Bucket").getAsString();
    }
    public String accessKeyId(){
        return header.get("AccessKeyId").getAsString();
    }
    public String secretAccessKey(){
        return header.get("SecretAccessKey").getAsString();
    }
}
