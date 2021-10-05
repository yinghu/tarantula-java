package com.tarantula.platform.presence;

import com.icodesoftware.Descriptor;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.ServiceProvider;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.item.ItemConfigurationServiceProvider;

public class PresenceServiceProvider implements ServiceProvider {
    private TarantulaLogger logger;
    private final String name;
    private final GameCluster gameCluster;
    private ServiceContext serviceContext;

    public PresenceServiceProvider(GameCluster gameCluster){
        this.name = (String)gameCluster.property(GameCluster.GAME_SERVICE);
        this.gameCluster = gameCluster;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void start() throws Exception {
        logger.warn("presence service provider started");
    }

    @Override
    public void shutdown() throws Exception {

    }
    @Override
    public void setup(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
        this.logger = serviceContext.logger(ItemConfigurationServiceProvider.class);
    }
    public void onPlay(String systemId, Descriptor lobby){
        logger.warn("adding recently play list->"+systemId+"on looby->"+lobby.tag());
    }
}
