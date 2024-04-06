package com.tarantula.admin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.OnAccess;
import com.icodesoftware.util.ResponseHeader;

import java.util.List;

public class LabeledAccessKeyContext extends ResponseHeader {

    public List<OnAccess> accessKeyList;


    public LabeledAccessKeyContext(List<OnAccess> accessKeyList){
        this.accessKeyList = accessKeyList;
        this.successful = true;
    }
    public JsonObject toJson(){
        JsonObject jo = new JsonObject();
        jo.addProperty("successful",successful);
        if(!successful){
            jo.addProperty("message",message);
        }
        if(accessKeyList!=null){
            JsonArray klist = new JsonArray();
            accessKeyList.forEach(k->{
                JsonObject key = new JsonObject();
                key.addProperty("accessKey",k.distributionKey());
                key.addProperty("label",k.typeId());
                klist.add(key);
            });
            jo.add("accessKeyList",klist);
        }
        return jo;
    }
}
