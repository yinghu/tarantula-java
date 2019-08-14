package com.tarantula.platform.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.tarantula.platform.playmode.GameDescriptor;

import java.lang.reflect.Type;

/**
 * updated by yinghu on 5/29/2019.
 */
public class GameDescriptorSerializer implements JsonSerializer<GameDescriptor> {

    public JsonElement serialize(GameDescriptor game, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = (JsonObject) new DescriptorSerializer().serialize(game.descriptor,type,jsonSerializationContext);
        if(game.configuration!=null){
            jo.add("configuration",new ConfigurationSerializer().serialize(game.configuration,type,jsonSerializationContext));
        }
        /**
        if(game.descriptor!=null){
            jo = (JsonObject) new DescriptorSerializer().serialize(game.descriptor,type,jsonSerializationContext);
        }else{
            jo = (JsonObject) new GameComponentSerializer().serialize(game,type,jsonSerializationContext);
        }
        jo.addProperty("deckSize",game.deckSize);
        jo.addProperty("dealerSeatFee",game.dealerSeatFee);
        jo.addProperty("dealerSeatFeeAsString", SystemUtil.toCreditsString(game.dealerSeatFee));
        jo.addProperty("minWager",game.minWager);
        jo.addProperty("minWagerAsString", SystemUtil.toCreditsString2(game.minWager));
        jo.addProperty("maxWager",game.maxWager);
        jo.addProperty("maxWagerAsString", SystemUtil.toCreditsString2(game.maxWager));
        jo.addProperty("seats",game.seats);
        if(game.descriptor==null){
            jo.addProperty("successful",true);
            jo.addProperty("instanceId",game.instanceId());
            jo.addProperty("entryCost",game.entryCost());
            jo.addProperty("tournamentEnabled",game.tournamentEnabled());
            jo.addProperty("name",game.name());
        }**/
        return jo;
    }
}
