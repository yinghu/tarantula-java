package com.tarantula.game.casino.baccarat;

import com.google.gson.*;
import com.tarantula.game.GameSerializer;
import com.tarantula.game.GameStatisticsEntry;
import com.tarantula.game.GameStatisticsEntrySerializer;
import com.tarantula.game.casino.BetLineSerializer;
import com.tarantula.game.casino.DeckSerializer;
import com.tarantula.game.casino.Seat;
import com.tarantula.game.casino.SeatSerializer;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by yinghu lu on 11/28/2018.
 */
public class BaccaratSerializer implements JsonSerializer<Baccarat> {

    @Override
    public JsonElement serialize(Baccarat baccarat, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = (JsonObject)new GameSerializer().serialize(baccarat.header,type,jsonSerializationContext);
        jo.add("playerHand",new BaccaratPlayerHandSerializer().serialize(baccarat.playerHand,type,jsonSerializationContext));
        jo.add("bankerHand",new BaccaratBankerHandSerializer().serialize(baccarat.bankerHand,type,jsonSerializationContext));
        jo.add("tieHand",new BaccaratTieBetLineSerializer().serialize(baccarat.baccaratTieBetLine,type,jsonSerializationContext));
        SeatSerializer seatSerializer = new SeatSerializer();
        jo.add("dealer",seatSerializer.serialize(baccarat.houseSeat,type,jsonSerializationContext));
        JsonArray plist = new JsonArray();
        for(Seat s : baccarat.onStage){
            plist.add(seatSerializer.serialize(s,type,jsonSerializationContext));
        }
        jo.add("seatList",plist);
        jo.add("deck",new DeckSerializer().serialize(baccarat.deck,type,jsonSerializationContext));
        JsonArray blist = new JsonArray();
        BetLineSerializer betLineSerializer = new BetLineSerializer();
        baccarat.betLineList.forEach((b)->{
            blist.add(betLineSerializer.serialize(b,type,jsonSerializationContext));
        });
        if(blist.size()>0){
            jo.add("betList",blist);
        }
        GameStatisticsEntrySerializer gse = new GameStatisticsEntrySerializer();
        JsonArray slist = new JsonArray();
        for(GameStatisticsEntry g : baccarat.onStatistics){
            slist.add(gse.serialize(g,type,jsonSerializationContext));
        }
        jo.add("statisticsList",slist);
        JsonArray rlist = new JsonArray();
        baccarat.roundResult.list(new ArrayList<>()).forEach((r)->{rlist.add(r);});
        jo.add("resultList",rlist);
        return jo;
    }
}
