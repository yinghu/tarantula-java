package com.tarantula.platform.presence.pvp;

import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;

import java.util.List;


public class LeagueRewardScheduler implements Runnable{
    private static final TarantulaLogger logger = JDKLogger.getLogger(LeagueRewardScheduler.class);

    private final List<ChampionLeaderBoardEntry> pending;
    private final PlatformPVPBattleServiceProvider pvpBattleServiceProvider;

    public LeagueRewardScheduler(List<ChampionLeaderBoardEntry> pending, PlatformPVPBattleServiceProvider pvpBattleServiceProvider){
        this.pending = pending;
        this.pvpBattleServiceProvider = pvpBattleServiceProvider;
    }

    @Override
    public void run() {
        pending.forEach(championLeaderBoardEntry -> {
            try{
                League league = pvpBattleServiceProvider.leagues.get(championLeaderBoardEntry.elo);
                if(league!=null){
                    pvpBattleServiceProvider.leagueReward(championLeaderBoardEntry.playerId,league.leagueReward.distributionId());
                }else{
                    logger.warn("No league linked with ["+championLeaderBoardEntry.elo+"] for top100");
                }
            }catch (Exception ex){
                logger.warn("Error on league reward ["+championLeaderBoardEntry.playerId+"]");
                //continue if error on single player
            }
        });
    }
}
