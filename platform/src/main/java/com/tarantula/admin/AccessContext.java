package com.tarantula.admin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tarantula.Access;
import com.tarantula.Statistics;
import com.tarantula.platform.ResponseHeader;
import com.tarantula.platform.service.Metrics;

import java.util.List;

public class AccessContext extends ResponseHeader {
    public List<Access> userList;

    public AccessContext(){
        this.successful = true;
    }
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("successful",successful);
        JsonArray ja = new JsonArray();
        for(Access acc : userList){
            JsonObject xv = new JsonObject();
            xv.addProperty("oid",acc.distributionKey());
            xv.addProperty("name",acc.login());
            xv.addProperty("role",acc.role());
            ja.add(xv);
        }
        jsonObject.add("userList",ja);
        return jsonObject;
    }

}
