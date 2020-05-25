package com.tarantula.admin;

import com.google.gson.JsonObject;
import com.tarantula.platform.presence.SubscriptionFee;

public class ShoppingContext {
    public SubscriptionFee monthly;
    public SubscriptionFee yearly;

    public ShoppingContext(SubscriptionFee m, SubscriptionFee y){
        this.monthly = m;
        this.yearly = y;
    }

    public JsonObject toJson(){
        JsonObject jo = new JsonObject();
        jo.addProperty("successful",true);
        JsonObject m = new JsonObject();
        m.addProperty("type",monthly.type);
        m.addProperty("name",monthly.name);
        m.addProperty("amount",monthly.amount);
        m.addProperty("currency",monthly.currency);
        jo.add("monthlyFee",m);
        JsonObject y = new JsonObject();
        y.addProperty("type",yearly.type);
        y.addProperty("name",yearly.name);
        y.addProperty("amount",yearly.amount);
        y.addProperty("currency",yearly.currency);
        jo.add("yearlyFee",y);
        return jo;
    }
}
