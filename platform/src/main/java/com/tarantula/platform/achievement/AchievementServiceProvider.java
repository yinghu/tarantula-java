package com.tarantula.platform.achievement;

import com.icodesoftware.Distributable;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.ServiceProvider;
import com.tarantula.platform.GameCluster;

public class AchievementServiceProvider implements ServiceProvider {

    private TarantulaLogger logger;
    private final String name;
    private final GameCluster gameCluster;

    public AchievementServiceProvider(GameCluster gameCluster){
        this.name = (String)gameCluster.property(GameCluster.GAME_SERVICE);
        this.gameCluster = gameCluster;
    }
    @Override
    public String name() {
        return name;
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }
    @Override
    public void setup(ServiceContext serviceContext) {
        //this.serviceContext = serviceContext;
        //this.applicationPreSetup = SystemUtil.applicationPreSetup((String)gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME));
        this.logger = serviceContext.logger(AchievementServiceProvider.class);
        ///this.distributionItemService = this.serviceContext.clusterProvider(Distributable.DATA_SCOPE).serviceProvider(DistributionItemService.NAME);
    }
    public void onProgress(String goal,double delta){
        logger.warn("progress->"+goal+">>"+delta);
    }
}
