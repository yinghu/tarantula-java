package com.tarantula.game.casino.baccarat;

import com.google.gson.*;
import com.tarantula.game.casino.Card;
import com.tarantula.game.casino.CardSerializer;

import java.lang.reflect.Type;

/**
 * Created by yinghu lu on 11/26/2018
 */
public class BaccaratBankerHandSerializer implements JsonSerializer<BaccaratBankerHand> {
    @Override
    public JsonElement serialize(BaccaratBankerHand bankerHand, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = new JsonObject();
        jo.addProperty("lineId",bankerHand.stub());
        jo.addProperty("name",bankerHand.name());
        jo.addProperty("standing",bankerHand.standing);
        if(bankerHand.hand!=null){
            JsonArray clist = new JsonArray();
            jo.addProperty("rank",bankerHand.rank());
            CardSerializer cx = new CardSerializer();
            for(Card c: bankerHand.hand){
                if(c!=null){
                    clist.add(cx.serialize(c,type,jsonSerializationContext));
                }
            }
            jo.add("cardList",clist);
        }
        return jo;
    }
}
