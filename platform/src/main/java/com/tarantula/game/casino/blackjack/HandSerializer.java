package com.tarantula.game.casino.blackjack;

import com.google.gson.*;
import com.tarantula.game.casino.Card;
import com.tarantula.game.casino.CardSerializer;

import java.lang.reflect.Type;

/**
 * Created by yinghu lu on 11/26/2018
 */
public class HandSerializer implements JsonSerializer<BlackJackHand> {
    @Override
    public JsonElement serialize(BlackJackHand hand, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = new JsonObject();
        jo.addProperty("subscript",hand.subscript);
        jo.addProperty("rank",hand.rank());
        jo.addProperty("standing",hand.standing);
        jo.addProperty("onDeck",hand.onDeck);
        JsonArray clist = new JsonArray();
        if(hand.hand!=null){
            CardSerializer cx = new CardSerializer();
            for(Card c: hand.hand){
                if(c!=null){
                    clist.add(cx.serialize(c,type,jsonSerializationContext));
                }
            }
        }
        jo.add("cardList",clist);
        return jo;
    }
}
