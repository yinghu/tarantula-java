package com.tarantula.admin;

import com.google.gson.JsonObject;
import com.icodesoftware.Configurable;
import com.icodesoftware.util.RecoverableObject;

public class SubscriptionItem extends RecoverableObject implements Configurable {

    public String name;
    public String description;
    public double price;
    public boolean recurring;

    public SubscriptionItem(String oid,String name,String description,double price,boolean recurring){
        //this.oid = oid;
        this.name = name;
        this.description = description;
        this.price = price;
        this.recurring = recurring;
    }


    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("oid",distributionId);
        jsonObject.addProperty("name",name);
        jsonObject.addProperty("description",description);
        jsonObject.addProperty("price",price);
        jsonObject.addProperty("recurring",recurring);
        return jsonObject;
    }
}
