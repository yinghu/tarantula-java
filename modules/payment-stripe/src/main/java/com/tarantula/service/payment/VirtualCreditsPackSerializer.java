package com.tarantula.service.payment;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.tarantula.platform.util.SystemUtil;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by yinghu lu on 2/15/2019.
 */
public class VirtualCreditsPackSerializer implements JsonSerializer<VirtualCreditsPack> {
    @Override
    public JsonElement serialize(VirtualCreditsPack virtualCreditsPack, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = new JsonObject();
        jo.addProperty("oid",virtualCreditsPack.key().asString());
        jo.addProperty("name",virtualCreditsPack.name);
        jo.addProperty("amount",virtualCreditsPack.price);
        jo.addProperty("price",new BigDecimal(virtualCreditsPack.price/100).setScale(2, RoundingMode.HALF_EVEN).toString());
        jo.addProperty("credits", SystemUtil.toCreditsString(virtualCreditsPack.credits));
        return jo;
    }
}
