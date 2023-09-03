package com.tarantula.platform.lobby;

import com.icodesoftware.Configurable;
import com.icodesoftware.Descriptor;

import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.ConfigurationServiceProvider;
import com.icodesoftware.service.ServiceContext;

import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.item.DistributionItemService;
import com.tarantula.platform.service.ApplicationPreSetup;
import com.tarantula.platform.item.ItemDistributionCallback;


import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class PlatformLobbyServiceProvider implements ConfigurationServiceProvider, ItemDistributionCallback {

    public static final String NAME = "lobby";

    private ServiceContext serviceContext;
    private TarantulaLogger logger;
    private final GameCluster gameCluster;
    private String gameServiceName;
    private String gameTypeId;
    private ApplicationPreSetup applicationPreSetup;
    private DistributionItemService distributionItemService;
    private ConcurrentHashMap<String,ListenerOnLobby> lobbyListeners;
    private ConcurrentHashMap<String,LobbyItem> lobbyItems;

    public PlatformLobbyServiceProvider(PlatformGameServiceProvider gameServiceProvider){
        this.gameCluster = gameServiceProvider.gameCluster();
        this.gameServiceName = gameCluster.serviceType();
        this.gameTypeId = gameCluster.typeId();
    }
    @Override
    public void setup(ServiceContext serviceContext) {
        this.lobbyListeners = new ConcurrentHashMap<>();
        this.lobbyItems = new ConcurrentHashMap<>();
        this.serviceContext = serviceContext;
        this.applicationPreSetup = gameCluster.applicationPreSetup();
        this.distributionItemService = this.serviceContext.clusterProvider().serviceProvider(DistributionItemService.NAME);
        this.logger = JDKLogger.getLogger(PlatformLobbyServiceProvider.class);
        this.logger.warn("Lobby service provider started on ->"+gameServiceName+"-->"+gameTypeId);
    }
    @Override
    public String name() {
        return NAME;
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
        logger.warn("register->"+t.distributionKey());
        distributionItemService.onRegisterItem(gameServiceName,name(),t.configurationTypeId(),t.distributionKey());
    }
    @Override
    public <T extends Configurable> void release(T t) {
        t.released();
        logger.warn("release->"+t.distributionKey());
        distributionItemService.onReleaseItem(gameServiceName,name(),t.configurationTypeId(),t.configurationName());
    }

    public boolean onItemRegistered(String category,String itemId){
        LobbyItem lobbyItem = new LobbyItem();
        lobbyItem.distributionKey(itemId);
        //GameCluster _gc = serviceContext.deploymentServiceProvider().gameCluster(gameCluster.distributionKey());
        Descriptor app = gameCluster.serviceWithCategory(category);
        if(!applicationPreSetup.load(app,lobbyItem)){
            return false;
        }
        lobbyItem.setup();
        String lobbyTag = gameTypeId+"/"+lobbyItem.configurationName();
        lobbyItems.put(lobbyTag,lobbyItem);
        ListenerOnLobby lobbyListener = lobbyListeners.get(lobbyTag);
        if(lobbyListener!=null) lobbyListener.listener.onUpdated(lobbyItem);
        return true;
    }
    public boolean onItemReleased(String category,String itemId){
        String lobbyTag = gameTypeId+"/"+itemId;
        LobbyItem removed = lobbyItems.remove(lobbyTag);
        if(removed!=null){
            ListenerOnLobby listener = lobbyListeners.get(lobbyTag);
            if(listener!=null) listener.listener.onRemoved(removed);
        }
        return true;
    }

    public String registerConfigurableListener(Descriptor descriptor, Configurable.Listener listener) {
        lobbyListeners.put(descriptor.tag(),new ListenerOnLobby(descriptor,listener));
        List<LobbyItem> items = applicationPreSetup.list(descriptor,new LobbyItemObjectQuery(descriptor.key(),descriptor.category()));
        items.forEach((a)-> {
            logger.warn(a.configurationCategory()+""+a.oid());
            if(!a.disabled()){
                a.setup();
                lobbyItems.put(gameTypeId+"/"+a.configurationName(),a);
            }
        });
        lobbyItems.forEach((k,v)->{
            ListenerOnLobby lobbyListener = lobbyListeners.get(k);
            if(lobbyListener!=null && k.equals(lobbyListener.lobby.tag())) lobbyListener.listener.onLoaded(v);
        });
        return null;
    }
    public void unregisterConfigurableListener(String registryKey){
        lobbyListeners.remove(registryKey);
    }
}
