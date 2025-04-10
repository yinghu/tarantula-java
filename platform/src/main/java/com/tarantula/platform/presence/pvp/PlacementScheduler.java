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
                if(seasonPlayerIndex.onSeason){
                    Rating rating = presenceServiceProvider.rating(new SimpleStub(seasonPlayerIndex.playerId));
                    League league = pvpBattleServiceProvider.leagues.get(rating.level());
                    if(league!=null){
                        //logger.warn("Placement ["+seasonPlayerIndex.playerId+" : "+seasonPlayerIndex.seasonId+"]");
                        pvpBattleServiceProvider.placementReward(seasonPlayerIndex.playerId,league.placementReward.distributionId());
                        eloReset(rating,league);
                    }else{
                        logger.warn("No league linked with ["+rating.level()+"] for placement");
                    }
                }
            }catch (Exception ex){
                //continue if error on single player
                logger.warn("Error on placement ["+seasonPlayerIndex.playerId+" : "+seasonPlayerIndex.seasonId+"]");
            }
        });
    }

    private void eloReset(Rating rating,League league){
        //starter1 starter2 starter3  no change
        //bronze1 -> 300 bronze2 --> 300  bronze3 --> 350
        //silver1 --> 400 silver2 --> 500  silver3 -- > 600
        //gold1 750 gold2 900 gold3 1050
        //diamond1 1250 diamond2 1450 diamond3 1650
        if(!league.name().startsWith("starter")){
            rating.level(league.resetPoint());
            rating.update();
        }
    }
}
