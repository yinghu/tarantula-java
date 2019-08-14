package com.tarantula.game.casino.blackjack;

import com.google.gson.*;
import com.tarantula.game.casino.SeatSerializer;

import java.lang.reflect.Type;

/**
 * Created by yinghu lu on 11/26/2018
 */
public class BlackJackSeatSerializer implements JsonSerializer<BlackJackSeat> {
    @Override
    public JsonElement serialize(BlackJackSeat position, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = (JsonObject) new SeatSerializer().serialize(position,type,jsonSerializationContext);
        HandSerializer hs = new HandSerializer();
        JsonArray hlist = new JsonArray();
        if(position.hands!=null){
            for(BlackJackHand h:position.hands){
                hlist.add(hs.serialize(h,type,jsonSerializationContext));
            }
        }
        jo.add("handList",hlist);
        return jo;
    }
}
