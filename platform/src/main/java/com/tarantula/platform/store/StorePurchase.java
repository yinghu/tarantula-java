package com.tarantula.platform.store;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tarantula.platform.ResponseHeader;
import com.tarantula.platform.item.Commodity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StorePurchase extends ResponseHeader {

    public String transactionId;
    public List<Commodity> commodities = new ArrayList<>();


    @Override
    public Map<String,Object> toMap(){
        this.properties.put("transactionId",transactionId);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.transactionId = (String) properties.getOrDefault("transactionId","n/a");
    }

    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("successful",true);
        jsonObject.addProperty("transactionId",transactionId);
        JsonArray commodities = new JsonArray();
        JsonObject comm = new JsonObject();
        comm.addProperty("type","currency");
        comm.addProperty("amount",100);
        commodities.add(comm);
        jsonObject.add("commodities",commodities);
        return jsonObject;
    }
}
