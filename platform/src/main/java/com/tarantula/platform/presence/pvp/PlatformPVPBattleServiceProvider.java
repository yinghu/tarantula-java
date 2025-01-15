package com.tarantula.platform.presence.pvp;

import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.item.PlatformItemServiceProvider;

public class PlatformPVPBattleServiceProvider extends PlatformItemServiceProvider {

    public static final String NAME = "pvp_battle";

    public PlatformPVPBattleServiceProvider(PlatformGameServiceProvider gameServiceProvider){
        super(gameServiceProvider,NAME);
    }

    @Override
    public void setup(ServiceContext serviceContext) {
        super.setup(serviceContext);
        this.dataStore = applicationPreSetup.dataStore(gameCluster,NAME);
        this.logger = JDKLogger.getLogger(PlatformPVPBattleServiceProvider.class);
        this.logger.warn("PVP battle service provider started on ->"+gameServiceName);
    }

}
