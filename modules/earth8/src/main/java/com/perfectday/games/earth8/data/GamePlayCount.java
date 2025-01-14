package com.perfectday.games.earth8.data;


import com.google.gson.JsonObject;
import com.icodesoftware.OnAccess;
import com.icodesoftware.OnLog;
import com.icodesoftware.protocol.GameContext;
import com.icodesoftware.service.TokenValidatorProvider;
import com.icodesoftware.util.TimeUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class GamePlayCount{

    public static final String CONCURRENT_PLAYERS = "current_players";
    public static final String ON_JOINED = "on_joined";
    public static final String ON_START_GAME = "on_start_game";
    public static final String ON_UPDATE_GAME = "on_update_game";
    public static final String ON_END_GAME = "on_end_game";
    public static final String ON_LEFT = "on_left";

    public static final String ON_GAME_CLUSTER_EVENT = "on_game_cluster_event";

    public static final int CONCURRENCY_INTERVAL_MINUTES = 3;

    private ConcurrentHashMap<Long, LocalDateTime> playerUpdates = new ConcurrentHashMap<>();

    private final GameContext gameContext;
    private final String event;
    private AtomicInteger count;
    private AtomicInteger success;
    private AtomicInteger failure;

    private final GamePlayCount concurrentPlayers;

    public GamePlayCount(GameContext gameContext,String event,double total,GamePlayCount concurrentPlayers){
        this.gameContext = gameContext;
        this.event = event;
        this.count = new AtomicInteger(Double.valueOf(total).intValue());
        this.success = new AtomicInteger(0);
        this.failure = new AtomicInteger(0);
        this.concurrentPlayers = concurrentPlayers;
    }

    private JsonObject toJson(String node){
        JsonObject resp = new JsonObject();
        resp.addProperty("message_type",event);
        resp.addProperty("node",node);
        resp.addProperty("count",count.get());
        resp.addProperty("success",success.getAndSet(0));
        resp.addProperty("failure",failure.getAndSet(0));
        resp.addProperty("timestamp",LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        return resp;
    }

    public void success(long playerId){
        count.incrementAndGet();
        success.incrementAndGet();
        gameContext.onMetrics(event,1);
        if(concurrentPlayers==null || playerId==0) return;
        concurrentPlayers.update(playerId);
    }

    public void failure(long playerId){
        count.incrementAndGet();
        failure.incrementAndGet();
        gameContext.onMetrics(event,1);
        if(concurrentPlayers==null || playerId==0) return;
        concurrentPlayers.update(playerId);
    }

    public void update(long playerId){
        playerUpdates.put(playerId,LocalDateTime.now().plusMinutes(CONCURRENCY_INTERVAL_MINUTES));
    }

    public void publish(String query){
        ArrayList<Long> expired = new ArrayList<>();
        playerUpdates.forEach((k,v)->{
            if(!TimeUtil.expired(v)){
                count.incrementAndGet();
                success.incrementAndGet();
            }
            else{
                expired.add(k);
            }
        });
        expired.forEach(rm->{
            playerUpdates.remove(rm);
        });
        TokenValidatorProvider.AuthVendor webhook = gameContext.authorVendor(OnAccess.WEB_HOOK);
        String payload = toJson(gameContext.node().nodeName()).toString();
        //gameContext.log(payload, OnLog.WARN);
        webhook.upload(query,payload.getBytes());
    }



}
