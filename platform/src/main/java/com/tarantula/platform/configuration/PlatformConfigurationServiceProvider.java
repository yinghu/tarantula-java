package com.tarantula.platform.configuration;

import com.icodesoftware.Configurable;
import com.icodesoftware.Descriptor;
import com.icodesoftware.OnAccess;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.service.ConfigurationServiceProvider;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.TokenValidatorProvider;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.presence.PlatformPresenceServiceProvider;

public class PlatformConfigurationServiceProvider implements ConfigurationServiceProvider{


    private GameCluster gameCluster;
    private TarantulaLogger logger;
    private final String gameServiceName;

    private TokenValidatorProvider tokenValidatorProvider;

    public PlatformConfigurationServiceProvider(GameCluster gameCluster){
        this.gameServiceName = (String)gameCluster.property(GameCluster.GAME_SERVICE);
        this.gameCluster = gameCluster;
        //this.inventoryServiceProvider = inventoryServiceProvider;
        //this.achievements = new ConcurrentHashMap<>();
    }

    @Override
    public String name() {
        return "configuration";
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }
    @Override
    public String registerConfigurableListener(Descriptor descriptor, Configurable.Listener listener) {
        //List<Achievement> items = applicationPreSetup.list(descriptor,new AchievementObjectQuery("typeId/"+descriptor.category()));
        //items.forEach((a)-> {
            //a.setup();
            //if(!a.disabled()) achievements.put(a.name(),a);
        //});
        return null;
    }
    @Override
    public <T extends Configurable> void register(T t) {
        logger.warn(t.configurationCategory()+">>>"+t.distributionKey());
        t.registered();
        //distributionItemService.register(gameServiceName,name(),t.configurationTypeId(),t.distributionKey());
    }
    @Override
    public <T extends Configurable> void release(T t) {
        t.released();
        //distributionItemService.release(gameServiceName,name(),t.configurationTypeId(),t.distributionKey());

    }
    @Override
    public void setup(ServiceContext serviceContext) {
        //this.serviceContext = serviceContext;
        this.tokenValidatorProvider = (TokenValidatorProvider)serviceContext.serviceProvider(TokenValidatorProvider.NAME);
        //TokenValidatorProvider.AuthVendor vendor = this.tokenValidatorProvider.authVendor()
        //this.applicationPreSetup = gameCluster.applicationPreSetup();//SystemUtil.applicationPreSetup((String)gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME));
        //this.presenceDataStore = this.applicationPreSetup.dataStore(gameCluster,name());
        //this.distributionItemService = this.serviceContext.clusterProvider().serviceProvider(DistributionItemService.NAME);
        this.logger = serviceContext.logger(PlatformPresenceServiceProvider.class);
        this.logger.warn("Configuration service provider started on ->"+gameServiceName);
    }
    @Override
    public void waitForData(){

    }

}
