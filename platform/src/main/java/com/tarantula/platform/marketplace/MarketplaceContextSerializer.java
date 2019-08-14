package com.tarantula.platform.marketplace;

import com.google.gson.*;
import com.tarantula.platform.util.ResponseSerializer;

import java.lang.reflect.Type;

/**
 * Created by yinghu lu on 2/15/2019.
 */
public class MarketplaceContextSerializer implements JsonSerializer<MarketplaceContext> {

    @Override
    public JsonElement serialize(MarketplaceContext marketplaceContext, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = (JsonObject)new ResponseSerializer().serialize(marketplaceContext,type,jsonSerializationContext);
        JsonArray mlist = new JsonArray();
        VirtualCreditsPackSerializer vcs = new VirtualCreditsPackSerializer();
        jo.addProperty("paymentClientId",marketplaceContext.paymentClientId);
        if(marketplaceContext.virtualCreditsPackList!=null){
            marketplaceContext.virtualCreditsPackList.forEach((v)->{
                mlist.add(vcs.serialize(v,type,jsonSerializationContext));
            });
        }
        jo.add("packList",mlist);
        if(marketplaceContext.onCheckout!=null){
            jo.add("onCheckout",vcs.serialize(marketplaceContext.onCheckout,type,jsonSerializationContext));
        }
        return jo;
    }
}
