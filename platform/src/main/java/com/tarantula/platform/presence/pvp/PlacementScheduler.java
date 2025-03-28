package com.tarantula.platform.presence.pvp;

import com.icodesoftware.Rating;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.tarantula.game.SimpleStub;
import com.tarantula.platform.presence.PlatformPresenceServiceProvider;

import java.util.List;


public class PlacementScheduler implements Runnable{
    private static final TarantulaLogger logger = JDKLogger.getLogger(PlacementScheduler.class);
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
                    //logger.warn("Placement ["+seasonPlayerIndex.playerId+" : "+seasonPlayerIndex.seasonId+"]");
                    pvpBattleServiceProvider.placementReward(seasonPlayerIndex.playerId,league.placementReward.distributionId());
                }else{
                    logger.warn("No league linked with ["+rating.level()+"] for placement");
                }
            }catch (Exception ex){
                //continue if error on single player
                logger.warn("Error on placement ["+seasonPlayerIndex.playerId+" : "+seasonPlayerIndex.seasonId+"]");
            }
        });
    }
}
