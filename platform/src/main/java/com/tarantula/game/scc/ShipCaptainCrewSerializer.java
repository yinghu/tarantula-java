package com.tarantula.game.scc;

import com.google.gson.*;
import com.tarantula.game.GameComponentSerializer;

import java.lang.reflect.Type;

public class ShipCaptainCrewSerializer implements JsonSerializer<ShipCaptainCrew> {
    @Override
    public JsonElement serialize(ShipCaptainCrew shipCaptainCrew, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo =  (JsonObject) new GameComponentSerializer().serialize(shipCaptainCrew,type,jsonSerializationContext);
        jo.addProperty("ship",shipCaptainCrew.ship);
        jo.addProperty("captain",shipCaptainCrew.captain);
        jo.addProperty("crew",shipCaptainCrew.crew);
        jo.addProperty("rollRemaining",shipCaptainCrew.rollRemaining);
        jo.addProperty("dicePickRemaining",shipCaptainCrew.dicePickRemaining);
        jo.addProperty("roundOver",shipCaptainCrew.roundOver);
        jo.add("cargo",new CargoSerializer().serialize(shipCaptainCrew.cargo,type,jsonSerializationContext));
        JsonArray plist = new JsonArray();
        DiceSideSerializer ds = new DiceSideSerializer();
        for(DiceSide d : shipCaptainCrew.board){
            plist.add(ds.serialize(d,type,jsonSerializationContext));
        }
        jo.add("board",plist);
        return jo;
    }
}
