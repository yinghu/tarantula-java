package com.tarantula.platform.presence;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tarantula.platform.ResponseHeader;

import java.util.List;

public class PermissionContext extends ResponseHeader {
    public int maxGameClusterCount;
    public int currentCount;
    public int maxLobbyCount;
    public int maxLevelCount;
    public String role;
    public String accessKey;
    public boolean subscriptionExpired;
    public List<String> accessKeyList;


    public PermissionContext(int maxGameClusterCount,int currentCount,boolean subscriptionExpired){
        this.maxGameClusterCount = maxGameClusterCount;
        this.currentCount = currentCount;
        this.subscriptionExpired = subscriptionExpired;
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
            message = "access key not available";
        }
    }
    public PermissionContext(List<String> accessKeyList){
        this.accessKeyList = accessKeyList;
        this.successful = true;
    }
    public JsonObject toJson(){
        JsonObject jo = new JsonObject();
        jo.addProperty("successful",successful);
        if(!successful){
            jo.addProperty("message",message);
        }
        jo.addProperty("maxGameClusterCount",maxGameClusterCount);
        jo.addProperty("currentCount",currentCount);
        jo.addProperty("maxLobbyCount",maxLobbyCount);
        jo.addProperty("maxLevelCount",maxLevelCount);
        jo.addProperty("subscriptionExpired",subscriptionExpired);
        if(role!=null){
            jo.addProperty("role",role);
        }
        if(accessKey!=null){
            jo.addProperty("accessKey",accessKey);
        }
        if(accessKeyList!=null){
            JsonArray klist = new JsonArray();
            accessKeyList.forEach(k->klist.add(k));
            jo.add("accessKeyList",klist);
        }
        return jo;
    }
}
