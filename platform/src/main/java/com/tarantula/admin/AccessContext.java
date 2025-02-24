package com.tarantula.admin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.Access;
import com.icodesoftware.util.TRResponse;

import java.util.List;

public class AccessContext extends TRResponse {
    public List<Access> userList;
    public List<SubscriptionItem> subscriptionList;
    public AccessContext(){
        this.successful = true;
    }
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("successful",successful);
        if(userList!=null) {
            JsonArray ja = new JsonArray();
            for (Access acc : userList) {
                JsonObject xv = new JsonObject();
                xv.addProperty("oid", acc.distributionKey());
                xv.addProperty("name", acc.login());
                xv.addProperty("role", acc.role());
                ja.add(xv);
            }
            jsonObject.add("userList", ja);
        }
        if(subscriptionList!=null){
            JsonArray ja = new JsonArray();
            for (SubscriptionItem acc : subscriptionList) {
                ja.add(acc.toJson());
            }
            jsonObject.add("subscriptionList", ja);
        }
        return jsonObject;
    }

}
