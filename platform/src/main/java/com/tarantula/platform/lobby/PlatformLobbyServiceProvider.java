package com.tarantula.platform.lobby;

import com.icodesoftware.Configurable;
import com.icodesoftware.Descriptor;
import com.icodesoftware.Distributable;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.service.ConfigurationServiceProvider;

import com.icodesoftware.service.ServiceContext;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.item.DistributionItemService;
import com.tarantula.platform.presence.PlatformPresenceServiceProvider;
import com.tarantula.platform.service.ClusterConfigurationCallback;
import com.tarantula.platform.util.SystemUtil;


public class PlatformLobbyServiceProvider implements ConfigurationServiceProvider, ClusterConfigurationCallback {

    private ServiceContext serviceContext;
    private TarantulaLogger logger;
    private GameCluster gameCluster;
    private String gameServiceName;

    public PlatformLobbyServiceProvider(GameCluster gameCluster){
        this.gameCluster = gameCluster;
        this.gameServiceName = (String)gameCluster.property(GameCluster.GAME_SERVICE);
    }
    @Override
    public void setup(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
        //this.applicationPreSetup = SystemUtil.applicationPreSetup((String)gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME));
        //this.presenceDataStore = this.applicationPreSetup.dataStore(serviceContext,gameCluster,name());
        //this.distributionItemService = this.serviceContext.clusterProvider(Distributable.DATA_SCOPE).serviceProvider(DistributionItemService.NAME);
        this.logger = serviceContext.logger(PlatformPresenceServiceProvider.class);
        this.logger.warn("Presence service provider started on ->"+gameServiceName);
    }
    @Override
    public String name() {
        return "lobby";
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }

    @Override
    public boolean onRegister(String category, String itemId) {
        return false;
    }

    @Override
    public boolean onRelease(String category, String itemId) {
        return false;
    }
    public String registerConfigurableListener(Descriptor application, Configurable.Listener listener) {
        logger.warn("register lobby module->"+application.tag());
        return null;
    }
}
