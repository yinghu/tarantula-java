package com.tarantula.platform.item;

import com.icodesoftware.*;
import com.icodesoftware.service.ConfigurationServiceProvider;
import com.icodesoftware.service.ServiceContext;

import com.tarantula.platform.GameCluster;
import com.tarantula.platform.service.ApplicationPreSetup;
import com.tarantula.platform.service.ClusterConfigurationCallback;
import com.tarantula.platform.service.deployment.TypedListener;
import com.tarantula.platform.util.SystemUtil;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlatformItemServiceProvider implements ConfigurationServiceProvider, ClusterConfigurationCallback {
    private TarantulaLogger logger;
    private ConcurrentHashMap<String, TypedListener> rListeners = new ConcurrentHashMap<>();
    private ServiceContext serviceContext;
    private DistributionItemService distributionItemService;

    private final String name;
    private GameCluster gameCluster;
    private ApplicationPreSetup applicationPreSetup;

    public PlatformItemServiceProvider(GameCluster gameCluster){
        this.name = (String)gameCluster.property(GameCluster.GAME_SERVICE);
        this.gameCluster = gameCluster;
    }

    public List<ConfigurableObject> list(Descriptor descriptor,String category){
        return applicationPreSetup.list(serviceContext,descriptor,new ConfigurableObjectQuery(category));
    }

    @Override
    public <T extends Configurable> void register(T config) {
        distributionItemService.register(name,name(),config.configurationCategory(),config.distributionKey());
    }



    @Override
    public String registerConfigurableListener(Descriptor application, Configurable.Listener listener) {
        String rid = UUID.randomUUID().toString();
        List<ConfigurableObject> items = applicationPreSetup.list(serviceContext,application,new ConfigurableObjectQuery("category/"+application.category()));
        items.forEach((a)-> listener.onCreated(a));
        this.rListeners.put(rid,new TypedListener(application.category(),listener));
        logger.warn("Listener registered with ->"+application.category());
        return rid;
    }
    @Override
    public void unregisterConfigurableListener(String registryKey){
        TypedListener t = rListeners.remove(registryKey);
        logger.warn("Listener removed with ->"+t.type);
    }
    @Override
    public void setup(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
        this.applicationPreSetup = SystemUtil.applicationPreSetup((String)gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME));
        this.logger = serviceContext.logger(PlatformItemServiceProvider.class);
        this.distributionItemService = this.serviceContext.clusterProvider(Distributable.INTEGRATION_SCOPE).serviceProvider(DistributionItemService.NAME);
    }
    @Override
    public String name() {
        return "ItemService";
    }

    @Override
    public void start() throws Exception {
        this.logger.warn("Item service provider started");
    }

    @Override
    public void shutdown() throws Exception {

    }
    public boolean onRegister(String category,String itemId){
        ConfigurableObject configurableObject = new ConfigurableObject();
        configurableObject.distributionKey(itemId);
        GameCluster _gc = serviceContext.deploymentServiceProvider().gameCluster(gameCluster.distributionKey());
        Descriptor app = _gc.serviceWithCategory("item");
        if(!applicationPreSetup.load(serviceContext,app,configurableObject)){
            return false;
        }
        rListeners.forEach((k,c)->{
            c.listener.onCreated(configurableObject);
        });
        return true;
    }
    public boolean onRelease(String category,String itemId){
        return false;
    }
}
