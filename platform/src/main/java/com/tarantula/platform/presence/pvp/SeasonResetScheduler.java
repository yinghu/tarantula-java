package com.tarantula.platform.presence.pvp;

import com.icodesoftware.DataStore;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;


import java.util.List;

public class SeasonResetScheduler implements Runnable{
    private static final TarantulaLogger logger = JDKLogger.getLogger(SeasonResetScheduler.class);
    private final List<SeasonPlayerIndex> pending;
    private DataStore dataStore;
    public SeasonResetScheduler(List<SeasonPlayerIndex> pending, DataStore dataStore){
        this.pending = pending;
       this.dataStore = dataStore;
    }

    @Override
    public void run() {
        pending.forEach(seasonPlayerIndex -> {
            try{
                seasonPlayerIndex.onSeason = false;
                dataStore.update(seasonPlayerIndex);
            }catch (Exception ex){
                //continue if error on single player
                logger.warn("Error on season reset ["+seasonPlayerIndex.playerId+" : "+seasonPlayerIndex.seasonId+"]");
            }
        });
    }
}
