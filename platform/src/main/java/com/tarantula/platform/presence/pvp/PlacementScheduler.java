package com.tarantula.platform.presence.pvp;

import com.icodesoftware.Rating;
import com.tarantula.game.SimpleStub;
import com.tarantula.platform.presence.PlatformPresenceServiceProvider;

import java.util.List;


public class PlacementScheduler implements Runnable{

    private final List<SeasonPlayerIndex> pending;
    private final PlatformPVPBattleServiceProvider pvpBattleServiceProvider;
    private final PlatformPresenceServiceProvider presenceServiceProvider;
    public PlacementScheduler(List<SeasonPlayerIndex> pending,PlatformPVPBattleServiceProvider pvpBattleServiceProvider,PlatformPresenceServiceProvider presenceServiceProvider){
        this.pending = pending;
        this.pvpBattleServiceProvider = pvpBattleServiceProvider;
        this.presenceServiceProvider = presenceServiceProvider;
    }

    @Override
    public void run() {
        pending.forEach(seasonPlayerIndex -> {
            try{
                Rating rating = presenceServiceProvider.rating(new SimpleStub(seasonPlayerIndex.playerId));
                League league = pvpBattleServiceProvider.leagues.get(rating.level());
                if(league!=null){
                    PlayerRewardIndex playerRewardIndex = pvpBattleServiceProvider.playerRewardIndex(seasonPlayerIndex.playerId);
                    playerRewardIndex.placementRewardId = league.placementReward.distributionId();
                    playerRewardIndex.update();
                }
            }catch (Exception ex){
                //continue if error on single player
            }
        });
    }
}
