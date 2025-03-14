package com.tarantula.platform.presence.pvp;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.DataStore;
import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.util.LongCompositeKey;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.TimeUtil;

import java.sql.Time;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class ChampionLeaderBoard extends RecoverableObject implements RecoverableFactory<ChampionLeaderBoardEntry> {

    private final int championsLeaderBoardThreshold;

    public final long nodeId;
    public final long seasonId;
    public final DataStore dataStore;
    private final int lastEntry;
    public final ChampionLeaderBoardEntry[] championLeaderBoardEntries;
    private final ConcurrentHashMap<Long,ChampionLeaderBoardEntry> onBoardIndex = new ConcurrentHashMap<>();

    private final ChampionLeaderBoardEntryComparator championLeaderBoardEntryComparator = new ChampionLeaderBoardEntryComparator();

    private ChampionLeaderBoard(){
        this.nodeId = 0;
        this.seasonId = 0;
        this.dataStore = null;
        this.lastEntry = 0;
        this.championLeaderBoardEntries = new ChampionLeaderBoardEntry[0];
        this.championsLeaderBoardThreshold = 0;
    }

    public ChampionLeaderBoard(DataStore dataStore,long nodeId,long seasonId,int championsLeaderBoardThreshold,int boardSize){
        this.dataStore = dataStore;
        this.nodeId = nodeId;
        this.seasonId = seasonId;
        this.lastEntry = boardSize-1;
        this.championsLeaderBoardThreshold = championsLeaderBoardThreshold;
        this.championLeaderBoardEntries = new ChampionLeaderBoardEntry[boardSize];
        for(int i=0;i<boardSize;i++){
            this.championLeaderBoardEntries[i] = new ChampionLeaderBoardEntry();
        }
    }

    public void load(){
        int[] i ={0};
        dataStore.list(this,e->{
            if(i[0]<championLeaderBoardEntries.length){
                championLeaderBoardEntries[i[0]]=e;
                if(e.playerId > 0) onBoardIndex.put(e.playerId,e);
                i[0]++;
            }
            return true;
        });
        for(ChampionLeaderBoardEntry e : championLeaderBoardEntries){
            if(e.distributionId() == 0){
                e.ownerKey(this.key());
                dataStore.create(e);
            }
        }
        sort();
    }

    public synchronized void onBoard(long playerId,int elo){
        //if(elo< championsLeaderBoardThreshold) return;
        ChampionLeaderBoardEntry last = championLeaderBoardEntries[lastEntry];
        if(elo < last.elo && onBoardIndex.containsValue(playerId)){
            ChampionLeaderBoardEntry pe = onBoardIndex.get(playerId);
            pe.elo = 0;//will be kickoff by next
            dataStore.update(pe);
            return;
        }
        if(elo > last.elo && onBoardIndex.containsValue(playerId)){
            ChampionLeaderBoardEntry pe = onBoardIndex.get(playerId);
            pe.elo = elo; //update existing
            pe.timestamp(TimeUtil.toUTCMilliseconds(LocalDateTime.now()));
            dataStore.update(pe);
            sort();
            return;
        }
        if(elo > last.elo && !onBoardIndex.containsValue(playerId)){
            onBoardIndex.remove(last.playerId);
            last.playerId = playerId; //replace last one
            last.elo = elo;
            last.timestamp(TimeUtil.toUTCMilliseconds(LocalDateTime.now()));
            dataStore.update(last);
            onBoardIndex.put(last.playerId,last);
            sort();
        }
    }

    public List<ChampionLeaderBoardEntry> leaderBoard(){
        ArrayList<ChampionLeaderBoardEntry> pending = new ArrayList<>();
        onBoardIndex.forEach((k,v)->{
            pending.add(v.duplicate());
        });
        Collections.sort(pending,championLeaderBoardEntryComparator);
        return pending;
    }

    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        JsonArray list = new JsonArray();
        leaderBoard().forEach(e->{
            list.add(e.toJson());
        });
        jsonObject.add("_leaderBoard",list);
        return jsonObject;
    }

    private void sort(){
        Arrays.sort(championLeaderBoardEntries,championLeaderBoardEntryComparator);
    }

    @Override
    public ChampionLeaderBoardEntry create() {
        return new ChampionLeaderBoardEntry();
    }

    @Override
    public String label() {
        return ChampionLeaderBoardEntry.LABEL;
    }

    @Override
    public Recoverable.Key key() {
        return new LongCompositeKey(nodeId,seasonId);
    }

    public static ChampionLeaderBoard noBoard(){
        return new ChampionLeaderBoard();
    }

}
