package com.perfectday.games.earth8;

import com.icodesoftware.SchedulingTask;
import com.icodesoftware.util.TimeUtil;

import java.util.concurrent.ConcurrentHashMap;

public class PVPBattleChecker implements SchedulingTask {

    private static final long RUN_RATE = 30000; //30 seconds

    private final ConcurrentHashMap<Long, PendingBattle> pending = new ConcurrentHashMap<>();

    private final Earth8GameServiceProvider earth8GameServiceProvider;

    public PVPBattleChecker(Earth8GameServiceProvider earth8GameServiceProvider){
        this.earth8GameServiceProvider = earth8GameServiceProvider;
    }

    void add(PendingBattle pendingBattle, long battleId){
        pending.put(battleId, pendingBattle);
    }

    void remove(long playerId){
        pending.remove(playerId);
    }

    void endBattle(long playerID){
        PendingBattle battle = pending.get(playerID);
        if(battle == null) return;

        remove(battle.session.distributionId());
        try {
            earth8GameServiceProvider.endGame(battle.session, battle.battleTransaction.toJson().toString().getBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    void endBattle(PendingBattle battle){
        remove(battle.session.distributionId());

        try {
            earth8GameServiceProvider.endGame(battle.session, battle.battleTransaction.toJson().toString().getBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean oneTime() {return true;}

    @Override
    public long initialDelay() {return 0;}

    @Override
    public long delay() {return RUN_RATE;}

    @Override
    public void run() {
        for(PendingBattle pendingBattle: pending.values()){
            if(TimeUtil.expired(TimeUtil.fromUTCMilliseconds(pendingBattle.battleTransaction.timestamp()))){
                endBattle(pendingBattle);
            }
        }

        earth8GameServiceProvider.gameContext.schedule(this);
    }
}
