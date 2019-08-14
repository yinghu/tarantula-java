package com.tarantula.game.casino.craps;

import com.google.gson.*;
import com.tarantula.game.GameSerializer;
import com.tarantula.game.casino.BetLineSerializer;
import com.tarantula.game.casino.SeatSerializer;

import java.lang.reflect.Type;

/**
 * Created by yinghu lu on 1/12/2019.
 */
public class CrapsSerializer implements JsonSerializer<Craps> {

    @Override
    public JsonElement serialize(Craps craps, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = (JsonObject) new GameSerializer().serialize(craps,type,jsonSerializationContext);
        jo.add("diceStop",new DiceStopSerializer().serialize(craps.diceStop,type,jsonSerializationContext));
        JsonArray plist = new JsonArray();
        SeatSerializer seatSerializer = new SeatSerializer();
        jo.add("dealer",seatSerializer.serialize(craps.dealer,type,jsonSerializationContext));
        craps.seatList.forEach((k,s)->{
            plist.add(seatSerializer.serialize(s,type,jsonSerializationContext));
        });
        jo.add("seatList",plist);
        JsonArray blist = new JsonArray();
        BetLineSerializer betLineSerializer = new BetLineSerializer();
        craps.betLines.forEach((k,bl)->{
            bl.wagerList.forEach((b)-> {
                blist.add(betLineSerializer.serialize(b,type,jsonSerializationContext));
            });
        });
        if(blist.size()>0){
            jo.add("betList",blist);
        }
        return jo;
    }
}
