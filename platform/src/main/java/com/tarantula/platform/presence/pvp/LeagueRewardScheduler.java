package com.tarantula.platform.presence.pvp;

import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.tarantula.platform.presence.PlatformPresenceServiceProvider;

import java.util.List;


public class LeagueRewardScheduler implements Runnable{
    private static final TarantulaLogger logger = JDKLogger.getLogger(LeagueRewardScheduler.class);

    private final List<ChampionLeaderBoardEntry> pending;
    private final PlatformPVPBattleServiceProvider pvpBattleServiceProvider;
    private final PlatformPresenceServiceProvider presenceServiceProvider;
    public LeagueRewardScheduler(List<ChampionLeaderBoardEntry> pending, PlatformPVPBattleServiceProvider pvpBattleServiceProvider, PlatformPresenceServiceProvider presenceServiceProvider){
        this.pending = pending;
        this.pvpBattleServiceProvider = pvpBattleServiceProvider;
        this.presenceServiceProvider = presenceServiceProvider;
    }

    @Override
    public void run() {
        pending.forEach(championLeaderBoardEntry -> {
            try{
                League league = pvpBattleServiceProvider.leagues.get(championLeaderBoardEntry.elo);
                if(league!=null){
                    PlayerRewardIndex playerRewardIndex = pvpBattleServiceProvider.playerRewardIndex(championLeaderBoardEntry.playerId);
                    playerRewardIndex.leagueRewardId = league.leagueReward.distributionId();
                    playerRewardIndex.update();
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
