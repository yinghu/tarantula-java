package com.tarantula.game.casino.roulette;

import com.google.gson.*;
import com.tarantula.game.GameSerializer;
import com.tarantula.game.casino.BetLineSerializer;
import com.tarantula.game.casino.SeatSerializer;

import java.lang.reflect.Type;

/**
 * Created by yinghu lu on 1/12/2019.
 */
public class RouletteSerializer implements JsonSerializer<Roulette> {

    @Override
    public JsonElement serialize(Roulette roulette, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = (JsonObject)new GameSerializer().serialize(roulette,type,jsonSerializationContext);
        if(roulette.wheelStop!=null){
            jo.add("stop",new WheelStopSerializer().serialize(roulette.wheelStop,type,jsonSerializationContext));
        }
        JsonArray plist = new JsonArray();
        SeatSerializer seatSerializer = new SeatSerializer();
        jo.add("dealer",seatSerializer.serialize(roulette.dealer,type,jsonSerializationContext));
        roulette.seatList.forEach((k,s)->{
            plist.add(seatSerializer.serialize(s,type,jsonSerializationContext));
        });
        jo.add("seatList",plist);
        JsonArray blist = new JsonArray();
        BetLineSerializer betLineSerializer = new BetLineSerializer();
        roulette.betLineList.forEach((b)->{
            blist.add(betLineSerializer.serialize(b,type,jsonSerializationContext));
        });
        if(blist.size()>0){
            jo.add("betList",blist);
        }
        return jo;
    }
}
