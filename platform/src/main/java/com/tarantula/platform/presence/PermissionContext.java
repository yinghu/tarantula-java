package com.tarantula.platform.presence;

import com.google.gson.JsonObject;
import com.tarantula.platform.ResponseHeader;

public class PermissionContext extends ResponseHeader {
    public int maxGameClusterCount;
    public int currentCount;
    public String role;
    public String accessKey;
    public PermissionContext(int maxGameClusterCount,int currentCount){
        this.maxGameClusterCount = maxGameClusterCount;
        this.currentCount = currentCount;
        this.successful = true;
    }
    public PermissionContext(String role,boolean suc){
        this.role = role;
        this.successful = suc;
    }
    public PermissionContext(String accessKey){
        this.accessKey = accessKey;
        this.successful = accessKey!=null;
        if(!successful){
            message = "try again later";
        }
    }
    public JsonObject toJson(){
        JsonObject jo = new JsonObject();
        jo.addProperty("successful",successful);
        if(!successful){
            jo.addProperty("message",message);
        }
        jo.addProperty("maxGameClusterCount",maxGameClusterCount);
        jo.addProperty("currentCount",currentCount);
        if(role!=null){
            jo.addProperty("role",role);
        }
        if(accessKey!=null){
            jo.addProperty("accessKey",accessKey);
        }
        return jo;
    }
}
