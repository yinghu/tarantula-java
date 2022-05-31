package com.tarantula.platform.lobby;

import com.icodesoftware.Configurable;
import com.icodesoftware.Descriptor;
import com.icodesoftware.Distributable;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.service.ConfigurationServiceProvider;
import com.icodesoftware.service.ServiceContext;

import com.tarantula.platform.GameCluster;
import com.tarantula.platform.item.DistributionItemService;
import com.tarantula.platform.service.ApplicationPreSetup;
import com.tarantula.platform.service.ClusterConfigurationCallback;
import com.tarantula.platform.util.SystemUtil;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class PlatformLobbyServiceProvider implements ConfigurationServiceProvider, ClusterConfigurationCallback {

    private ServiceContext serviceContext;
    private TarantulaLogger logger;
    private GameCluster gameCluster;
    private String gameServiceName;
    private ApplicationPreSetup applicationPreSetup;
    private DistributionItemService distributionItemService;
    private ConcurrentHashMap<String,Configurable.Listener<LobbyItem>> lobbyListeners;
    public PlatformLobbyServiceProvider(GameCluster gameCluster){
        this.gameCluster = gameCluster;
        this.gameServiceName = (String)gameCluster.property(GameCluster.GAME_SERVICE);
    }
    @Override
    public void setup(ServiceContext serviceContext) {
        this.lobbyListeners = new ConcurrentHashMap<>();
        this.serviceContext = serviceContext;
        this.applicationPreSetup = SystemUtil.applicationPreSetup((String)gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME));
        this.distributionItemService = this.serviceContext.clusterProvider(Distributable.DATA_SCOPE).serviceProvider(DistributionItemService.NAME);
        this.logger = serviceContext.logger(PlatformLobbyServiceProvider.class);
        this.logger.warn("Lobby service provider started on ->"+gameServiceName);
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
    public <T extends Configurable> void register(T t) {
        t.registered();
        distributionItemService.register(gameServiceName,name(),t.configurationTypeId(),t.distributionKey());
    }
    @Override
    public <T extends Configurable> void release(T t) {
        t.released();
        distributionItemService.release(gameServiceName,name(),t.configurationTypeId(),t.distributionKey());
    }

    public boolean onRegister(String category,String itemId){
        LobbyItem configurableObject = new LobbyItem();
        configurableObject.distributionKey(itemId);
        GameCluster _gc = serviceContext.deploymentServiceProvider().gameCluster(gameCluster.distributionKey());
        Descriptor app = _gc.serviceWithCategory(category);
        if(!applicationPreSetup.load(serviceContext,app,configurableObject)){
            return false;
        }
        configurableObject.setup();
        //achievements.put(configurableObject.name(),configurableObject);
        lobbyListeners.forEach((k,v)->v.onUpdated(configurableObject));
        return true;
    }
    public boolean onRelease(String category,String itemId){
        String[] released = {null};
        //achievements.forEach((k,v)->{
            //if(v.distributionKey().equals(itemId)) released[0] = k;
        //});
        //if(released[0]!=null) achievements.remove(released[0]);
        return true;
    }

    public String registerConfigurableListener(Descriptor descriptor, Configurable.Listener listener) {
        lobbyListeners.put(descriptor.tag(),listener);
        logger.warn("register lobby module->"+descriptor.tag()+">>"+descriptor.category());
        List<LobbyItem> items = applicationPreSetup.list(serviceContext,descriptor,new LobbyItemObjectQuery("typeId/"+descriptor.category()));
        items.forEach((a)-> {
            logger.warn("Lobby->"+a.configurationName());
            lobbyListeners.forEach((k,v)->v.onUpdated(a));
            //if (!a.disabled()) {
                //registerShop(a);
            //}
        });
        return null;
    }
}
