package com.tarantula.game;

import com.google.gson.*;
import com.tarantula.platform.util.SystemUtil;

import java.lang.reflect.Type;

/**
 * updated by yinghu on 5/29/2019.
 */
public class GameSerializer implements JsonSerializer<Game> {

    public JsonElement serialize(Game game, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo =(JsonObject) new GameComponentSerializer().serialize(game,type,jsonSerializationContext);
        jo.addProperty("deckSize",game.deckSize);
        jo.addProperty("dealerSeatFee",game.dealerSeatFee);
        jo.addProperty("dealerSeatFeeAsString", SystemUtil.toCreditsString(game.dealerSeatFee));
        jo.addProperty("minWager",game.minWager);
        jo.addProperty("minWagerAsString", SystemUtil.toCreditsString2(game.minWager));
        jo.addProperty("maxWager",game.maxWager);
        jo.addProperty("maxWagerAsString", SystemUtil.toCreditsString2(game.maxWager));
        jo.addProperty("seats",game.seats);
        jo.addProperty("successful",true);
        jo.addProperty("instanceId",game.instanceId());
        jo.addProperty("entryCost",game.entryCost());
        jo.addProperty("tournamentEnabled",game.tournamentEnabled());
        jo.addProperty("name",game.name());
        return jo;
    }
}
