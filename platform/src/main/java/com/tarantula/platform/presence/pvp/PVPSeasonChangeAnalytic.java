package com.tarantula.platform.presence.pvp;

import com.tarantula.platform.configuration.SeasonCredentialConfiguration;

public class PVPSeasonChangeAnalytic extends PVPAnalytic{
    private static final String MESSAGE_TYPE = "/earth8/pvp/0.0.1/PVPSeasonChange";

    public PVPSeasonChangeAnalytic(SeasonCredentialConfiguration.Season oldSeason, SeasonCredentialConfiguration.Season newSeason)
    {
        super(MESSAGE_TYPE);
        data.addProperty("old_season_id", oldSeason.seasonId);
        data.addProperty("faction_1", oldSeason.faction1.name());
        data.addProperty("faction_2", oldSeason.faction2.name());
        data.addProperty("faction_3", oldSeason.faction3.name());

        if(newSeason != null){
            data.addProperty("new_season_id", oldSeason.seasonId);
            data.addProperty("faction_1", oldSeason.faction1.name());
            data.addProperty("faction_2", oldSeason.faction2.name());
            data.addProperty("faction_3", oldSeason.faction3.name());
        }else {
            data.addProperty("new_season_id", 0);
        }
    }
}
