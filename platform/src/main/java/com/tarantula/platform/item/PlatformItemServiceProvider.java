package com.tarantula.platform.item;

import com.icodesoftware.*;
import com.icodesoftware.service.ConfigurationServiceProvider;
import com.icodesoftware.service.ServiceContext;

import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.service.ApplicationPreSetup;
import com.tarantula.platform.service.deployment.TypedListener;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlatformItemServiceProvider implements ConfigurationServiceProvider, ItemDistributionCallback,ApplicationPreSetup.Listener {

    public static final String NAME = "item";

    private TarantulaLogger logger;
    private ConcurrentHashMap<String, TypedListener> rListeners = new ConcurrentHashMap<>();
    private ServiceContext serviceContext;

    private DistributionItemService distributionItemService;

    private final String gameServiceName;
    private GameCluster gameCluster;
    private ApplicationPreSetup applicationPreSetup;

    public PlatformItemServiceProvider(PlatformGameServiceProvider gameServiceProvider){
        this.gameCluster = gameServiceProvider.gameCluster();
        this.gameServiceName = gameCluster.serviceType();
    }

    public List<ConfigurableObject> list(Descriptor descriptor,String category){
        return applicationPreSetup.list(descriptor,new ConfigurableObjectQuery(category));
    }

    @Override
    public <T extends Configurable> void register(T config) {
        distributionItemService.onRegisterItem(gameServiceName,name(),config.configurationCategory(),config.distributionKey());
    }



    @Override
    public String registerConfigurableListener(Descriptor application, Configurable.Listener listener) {
        String rid = UUID.randomUUID().toString();
        List<ConfigurableObject> items = applicationPreSetup.list(application,new ConfigurableObjectQuery("category/"+application.category()));
        items.forEach((a)-> listener.onCreated(a));
        this.rListeners.put(rid,new TypedListener(application.category(),listener));
        logger.warn("Listener registered with ->"+application.category());
        this.gameCluster.addListener(this);
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
        this.applicationPreSetup = gameCluster.applicationPreSetup();
        this.logger = serviceContext.logger(PlatformItemServiceProvider.class);
        this.distributionItemService = this.serviceContext.clusterProvider().serviceProvider(DistributionItemService.NAME);
    }
    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void start() throws Exception {
        this.logger.warn("Item service provider started on ->"+gameServiceName);
    }

    @Override
    public void shutdown() throws Exception {

    }
    public boolean onItemRegistered(String category,String itemId){
        ConfigurableObject configurableObject = new ConfigurableObject();
        configurableObject.distributionKey(itemId);
        Descriptor app = gameCluster.serviceWithCategory("item");
        if(!applicationPreSetup.load(app,configurableObject)){
            return false;
        }
        rListeners.forEach((k,c)->{
            c.listener.onCreated(configurableObject);
        });
        return true;
    }
    public boolean onItemReleased(String category,String itemId){
        return false;
    }

    public <T extends Configurable> void onCreated(Descriptor application,T t){
        //logger.warn(application.distributionKey()+">>CCC"+t.distributionKey()+">>"+t.configurationVersion());
    }
    public <T extends Configurable> void onUpdated(Descriptor application,T t){
        //logger.warn(application.distributionKey()+">>UUU"+t.distributionKey()+">>"+t.configurationVersion());
    }
    public <T extends Configurable> void onDeleted(Descriptor application,T t){
        //logger.warn(application.distributionKey()+">>DDD"+t.distributionKey()+">>"+t.configurationVersion());
    }
}
