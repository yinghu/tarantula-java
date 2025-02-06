package com.perfectday.games.earth8.config;

import com.icodesoftware.Configurable;
import com.icodesoftware.OnAccess;
import com.icodesoftware.OnLog;
import com.perfectday.games.earth8.Earth8GameServiceProvider;

public class SeasonConfigurationListener implements Configurable.Listener<Configurable> {
    public static final String platformServiceName = "pvp_battle";

    private final Earth8GameServiceProvider earth8GameServiceProvider;

    public SeasonConfigurationListener(Earth8GameServiceProvider earth8GameServiceProvider){
        this.earth8GameServiceProvider = earth8GameServiceProvider;
    }

    public void onLoaded(Configurable loaded){
        this.earth8GameServiceProvider.configurations.put(OnAccess.SEASON,loaded);
        this.earth8GameServiceProvider.gameContext.log(loaded.distributionId()+" loaded", OnLog.WARN);
    }

    public void onRemoved(Configurable removed){
        this.earth8GameServiceProvider.configurations.remove(OnAccess.SEASON);
        this.earth8GameServiceProvider.gameContext.log(removed.distributionId()+" removed", OnLog.WARN);
    }
}
