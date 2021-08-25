package com.tarantula.platform.item;

import com.icodesoftware.*;
import com.icodesoftware.service.ConfigurationServiceProvider;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.game.service.DynamicGameLobbySetup;
import com.tarantula.platform.service.ApplicationPreSetup;
import com.tarantula.platform.service.deployment.TypedListener;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ItemConfigurationServiceProvider implements ConfigurationServiceProvider {
    private TarantulaLogger logger;
    private ConcurrentHashMap<String, TypedListener> rListeners = new ConcurrentHashMap<>();
    private ServiceContext serviceContext;
    private DistributionItemService distributionItemService;
    private DataStore dataStore;
    private final String name;
    private ApplicationPreSetup applicationPreSetup;
    public ItemConfigurationServiceProvider(String name){
        this.name = name;
    }
    @Override
    public <T extends Configurable> void register(T config) {
        distributionItemService.register(name,(Item)config);
    }

    @Override
    public <T extends Configurable> void release(T t) {

    }

    @Override
    public void configure(String s) {

    }

    @Override
    public <T extends Configuration> T configuration(String s) {
        return null;
    }
    @Override
    public String registerConfigurableListener(Descriptor application, Configurable.Listener listener) {
        String rid = UUID.randomUUID().toString();
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
        this.applicationPreSetup = new DynamicGameLobbySetup();
        this.serviceContext = serviceContext;
        this.dataStore = serviceContext.dataStore(name.replace("-","_"),serviceContext.partitionNumber());
        this.logger = serviceContext.logger(ItemConfigurationServiceProvider.class);
        this.distributionItemService = this.serviceContext.clusterProvider(Distributable.DATA_SCOPE).serviceProvider(DistributionItemService.NAME);
    }
    @Override
    public String name() {
        return name;
    }

    @Override
    public void start() throws Exception {
        this.logger.warn("item configuration service provider started");
    }

    @Override
    public void shutdown() throws Exception {

    }
    public boolean onRegister(Configurable configurable){
        rListeners.forEach((k,c)->{
            if(c.type==null||c.type.equals("system")){
                c.listener.onCreated(configurable);
            }
            else if(c.type.equals(configurable.configurationCategory())){
                c.listener.onCreated(configurable);
            }
        });
        return true;
    }
    public String registerConfigurableListener(String category, Configurable.Listener listener){
        throw new UnsupportedOperationException("using descriptor");
    }
}
