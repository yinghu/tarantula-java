package com.perfectday.games.earth8.data;

import com.icodesoftware.OnLog;
import com.icodesoftware.SchedulingTask;
import com.icodesoftware.protocol.GameContext;

import java.util.concurrent.ConcurrentHashMap;

public class GamePlayEventRunner implements SchedulingTask {

    private final long TIMER = GamePlayCount.CONCURRENCY_INTERVAL_MINUTES*60*1000;

    private final GameContext gameContext;
    private final ConcurrentHashMap<String,GamePlayCount> gamePlayCounts;
    private final String query;

    public GamePlayEventRunner(GameContext gameContext,ConcurrentHashMap<String,GamePlayCount> gamePlayCounts,String query){
        this.gameContext = gameContext;
        this.gamePlayCounts = gamePlayCounts;
        this.query = query;
    }

    @Override
    public boolean oneTime() {
        return true;
    }

    @Override
    public long initialDelay() {
        return 0;
    }

    @Override
    public long delay() {
        return TIMER;
    }

    @Override
    public void run() {
        this.gamePlayCounts.forEach((k,v)->{
            try{
                v.publish(query);
            }catch (Exception ex){
                gameContext.log("Error on game play event ["+k+"]",ex, OnLog.ERROR);
            }
        });
        gameContext.schedule(this);
    }
}
