package com.tarantula.game.casino.blackjack;

import com.google.gson.*;
import com.tarantula.game.GameSerializer;
import com.tarantula.game.GameStatisticsEntry;
import com.tarantula.game.GameStatisticsEntrySerializer;
import com.tarantula.game.casino.BetLineSerializer;
import com.tarantula.game.casino.DeckSerializer;

import java.lang.reflect.Type;

/**
 * Created by yinghu lu on 11/28/2018.
 */
public class BlackJackSerializer implements JsonSerializer<BlackJack> {

    @Override
    public JsonElement serialize(BlackJack blackjack, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo =  (JsonObject) new GameSerializer().serialize(blackjack,type,jsonSerializationContext);

        jo.addProperty("round",blackjack.round);
        jo.addProperty("counter",blackjack.counter);
        jo.addProperty("cutter",blackjack.cutter);
        jo.addProperty("onTurn",blackjack.onTurn);
        jo.addProperty("splittable",blackjack.splittable);
        if(blackjack.tournamentEnabled()){
        }
        jo.add("turn",new BlackJackTurnSerializer().serialize((BlackJackTurn) blackjack.currentCheckPoint,type,jsonSerializationContext));
        //SeatSerializer ot = new SeatSerializer();
        //JsonArray tlist = new JsonArray();
        //blackjack.indexing.forEach((k,v)->{
            //tlist.add(ot.serialize(v,type,jsonSerializationContext));
        //});
        //jo.add("onTable",tlist);
        JsonArray plist = new JsonArray();
        BlackJackSeatSerializer ps = new BlackJackSeatSerializer();
        for(BlackJackSeat p : blackjack.onSeat){
            plist.add(ps.serialize(p,type,jsonSerializationContext));
        }
        jo.add("seatList",plist);
        jo.add("deck",new DeckSerializer().serialize(blackjack.deck,type,jsonSerializationContext));
        JsonArray blist = new JsonArray();
        BetLineSerializer betLineSerializer = new BetLineSerializer();
        blackjack.betLineList.forEach((b)->{
            blist.add(betLineSerializer.serialize(b,type,jsonSerializationContext));
        });
        if(blist.size()>0){
            jo.add("betList",blist);
        }
        GameStatisticsEntrySerializer gse = new GameStatisticsEntrySerializer();
        JsonArray slist = new JsonArray();
        for(GameStatisticsEntry g : blackjack.onStatistics){
            slist.add(gse.serialize(g,type,jsonSerializationContext));
        }
        jo.add("statisticsList",slist);

        JsonArray rlist = new JsonArray();
        for(int r : blackjack.dealerRankList){
            rlist.add(r);
        }
        jo.add("dealerRankList",rlist);
        return jo;
    }
}
