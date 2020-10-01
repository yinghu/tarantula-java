package com.tarantula.admin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.Subscription;
import com.tarantula.platform.ResponseHeader;

import java.util.List;

public class SubscriptionContext extends ResponseHeader {
    public List<Subscription> subscriptionList;

    public SubscriptionContext(){
        this.successful = true;
    }
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("successful",successful);
        JsonArray ja = new JsonArray();
        for(Subscription acc : subscriptionList){
            JsonObject xv = new JsonObject();
            //xv.addProperty("name",acc.login());
            //xv.addProperty("role",acc.role());
            ja.add(xv);
        }
        jsonObject.add("subscriptionList",ja);
        return jsonObject;
    }

}
