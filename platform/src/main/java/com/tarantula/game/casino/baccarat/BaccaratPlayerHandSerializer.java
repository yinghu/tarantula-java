package com.tarantula.game.casino.baccarat;

import com.google.gson.*;
import com.tarantula.game.casino.Card;
import com.tarantula.game.casino.CardSerializer;

import java.lang.reflect.Type;

/**
 * Created by yinghu lu on 11/26/2018
 */
public class BaccaratPlayerHandSerializer implements JsonSerializer<BaccaratPlayerHand> {
    @Override
    public JsonElement serialize(BaccaratPlayerHand playerHand, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = new JsonObject();
        jo.addProperty("lineId",playerHand.stub());
        jo.addProperty("name",playerHand.name());
        jo.addProperty("standing",playerHand.standing);
        if(playerHand.hand!=null) {
            JsonArray clist = new JsonArray();
            jo.addProperty("rank", playerHand.rank());
            CardSerializer cx = new CardSerializer();
            for (Card c : playerHand.hand) {
                if (c != null) {
                    clist.add(cx.serialize(c, type, jsonSerializationContext));
                }
            }
            jo.add("cardList",clist);
        }
        return jo;
    }
}
