package com.perfectday.games.earth8;

import com.icodesoftware.OnLog;
import com.icodesoftware.SchedulingTask;

import java.util.concurrent.ArrayBlockingQueue;

public class ScoreRunner implements SchedulingTask {

    private static final long RUN_RATE = 110;

    private long systemId;
    private int waitingCount;
    private double lastScore;
    private final ArrayBlockingQueue<PendingScore> pending = new ArrayBlockingQueue<>(20);

    private final Earth8GameServiceProvider earth8GameServiceProvider;

    public ScoreRunner(long systemId, Earth8GameServiceProvider earth8GameServiceProvider){
        this.earth8GameServiceProvider = earth8GameServiceProvider;
        this.systemId = systemId;
    }

    void add(PendingScore pendingScore){
        pending.offer(pendingScore);
    }

    @Override
    public boolean oneTime() {
        return true;
    }

    @Override
    public long initialDelay() {
        return RUN_RATE;
    }

    @Override
    public long delay() {
        return 0;
    }

    @Override
    public void run() {
        PendingScore pendingScore = pending.poll();
        if(pendingScore == null){
            waitingCount++;
            if(waitingCount > 3){
                earth8GameServiceProvider.scoreRunners.remove(systemId);
                return;
            }
            earth8GameServiceProvider.gameContext.schedule(this);
            return;
        }
        double scored = earth8GameServiceProvider.scoreTournamentWithJoined(pendingScore.session,pendingScore.battleUpdate,pendingScore.serverSession);
        //earth8GameServiceProvider.gameContext.log("Total : " + scored+" Delta :"+pendingScore.battleUpdate.score, OnLog.WARN);
        if(scored == 0){
            earth8GameServiceProvider.scoreRunners.remove(systemId);
            return;
        }
        if(scored <= lastScore){
            add(pendingScore);
            earth8GameServiceProvider.gameContext.log("Retry score ["+scored+"] ["+lastScore+"]", OnLog.WARN);
        }
        else{
            lastScore = scored;
        }
        earth8GameServiceProvider.gameContext.schedule(this);
        waitingCount = 0;
    }
}
