package com.tarantula.platform.presence.pvp;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.Configuration;
import com.icodesoftware.Session;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.item.PlatformItemServiceProvider;


public class PlatformPVPBattleServiceProvider extends PlatformItemServiceProvider {

    public static final String NAME = "pvp_battle";
    private int teamCreationWaitingTime = 5;
    private int seasonTimeGap = 5; //minutes
    private int seasonRunningDays = 12; //days

    public PlatformPVPBattleServiceProvider(PlatformGameServiceProvider gameServiceProvider){
        super(gameServiceProvider,NAME);
    }

    @Override
    public void setup(ServiceContext serviceContext) {
        super.setup(serviceContext);
        Configuration configuration = serviceContext.configuration("game-presence-settings");
        JsonObject pvp = ((JsonElement)configuration.property("pvp")).getAsJsonObject();
        teamCreationWaitingTime = pvp.get("waitingMinutesPerTeamFormation").getAsInt();
        seasonTimeGap = pvp.get("seasonTimeGapMinutes").getAsInt();
        seasonRunningDays = pvp.get("seasonRunningDays").getAsInt();
        this.dataStore = applicationPreSetup.dataStore(gameCluster,NAME);
        this.logger = JDKLogger.getLogger(PlatformPVPBattleServiceProvider.class);
        this.logger.warn("PVP battle service provider started on ->"+gameServiceName);
    }



    public TeamFormationResponse saveDefenseTeam(Session session,byte[] content){
        TeamFormationIndex teamFormationIndex = new TeamFormationIndex();
        teamFormationIndex.distributionId(session.distributionId());
        dataStore.createIfAbsent(teamFormationIndex,true);
        if(!teamFormationIndex.expired()) return TeamFormationResponse.failure(teamFormationIndex.timestamp());
        DefenseTeam defenseTeam = DefenseTeam.parse(content);
        defenseTeam.save(dataStore,teamFormationIndex,teamCreationWaitingTime);
        return TeamFormationResponse.success(teamFormationIndex.timestamp());
    }
}
