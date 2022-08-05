package com.tarantula.platform.configuration;

import com.icodesoftware.Configurable;
import com.icodesoftware.Descriptor;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.service.ConfigurationServiceProvider;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.TokenValidatorProvider;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.item.ConfigurableObject;
import com.tarantula.platform.item.ConfigurableObjectQuery;
import com.tarantula.platform.item.DistributionItemService;
import com.tarantula.platform.presence.PlatformPresenceServiceProvider;
import com.tarantula.platform.service.ApplicationPreSetup;
import com.tarantula.platform.service.ClusterConfigurationCallback;

import java.util.List;

public class PlatformConfigurationServiceProvider implements ConfigurationServiceProvider, ClusterConfigurationCallback {


    private GameCluster gameCluster;
    private TarantulaLogger logger;
    private final String gameServiceName;

    private ApplicationPreSetup applicationPreSetup;
    private TokenValidatorProvider tokenValidatorProvider;

    private DistributionItemService distributionItemService;

    private ServiceContext serviceContext;

    public PlatformConfigurationServiceProvider(GameCluster gameCluster){
        this.gameServiceName = (String)gameCluster.property(GameCluster.GAME_SERVICE);
        this.gameCluster = gameCluster;
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
        //this.tokenValidatorProvider.registerAuthVendor();
        List<ConfigurableObject> items = applicationPreSetup.list(descriptor,new ConfigurableObjectQuery("typeId/"+descriptor.category()));
        items.forEach((a)-> {
            a.setup();
            logger.warn(a.configurationCategory()+">>"+a.distributionKey());
            //if(!a.disabled()) achievements.put(a.name(),a);
        });
        return null;
    }
    @Override
    public <T extends Configurable> void register(T t) {
        t.registered();
        distributionItemService.register(gameServiceName,name(),t.configurationTypeId(),t.distributionKey());
    }
    @Override
    public <T extends Configurable> void release(T t) {
        t.released();
        this.distributionItemService.release(gameServiceName,name(),t.configurationTypeId(),t.distributionKey());
    }

    public boolean onRegister(String category,String itemId){
        ConfigurableObject configurableObject = new ConfigurableObject();
        configurableObject.distributionKey(itemId);
        GameCluster _gc = serviceContext.deploymentServiceProvider().gameCluster(gameCluster.distributionKey());
        Descriptor app = _gc.serviceWithCategory("item");
        if(!applicationPreSetup.load(app,configurableObject)){
            return false;
        }
        return true;
    }
    public boolean onRelease(String category,String itemId){
        logger.warn("CAT->"+category+">>>"+itemId);
        return false;
    }


    @Override
    public void setup(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
        this.tokenValidatorProvider = (TokenValidatorProvider)serviceContext.serviceProvider(TokenValidatorProvider.NAME);
        this.applicationPreSetup = gameCluster.applicationPreSetup();
        this.distributionItemService = this.serviceContext.clusterProvider().serviceProvider(DistributionItemService.NAME);
        this.logger = serviceContext.logger(PlatformPresenceServiceProvider.class);
        this.logger.warn("Configuration service provider started on ->"+gameServiceName);
    }
    @Override
    public void waitForData(){

    }

}
