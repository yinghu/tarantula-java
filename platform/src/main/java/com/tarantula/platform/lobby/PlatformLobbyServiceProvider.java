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
    private String gameName;
    private ApplicationPreSetup applicationPreSetup;
    private DistributionItemService distributionItemService;
    private ConcurrentHashMap<String,ListenerOnLobby> lobbyListeners;
    private ConcurrentHashMap<String,LobbyItem> lobbyItems;
    public PlatformLobbyServiceProvider(GameCluster gameCluster){
        this.gameCluster = gameCluster;
        this.gameServiceName = (String)gameCluster.property(GameCluster.GAME_SERVICE);
        this.gameName = ((String)gameCluster.property(GameCluster.NAME)).toLowerCase();
    }
    @Override
    public void setup(ServiceContext serviceContext) {
        this.lobbyListeners = new ConcurrentHashMap<>();
        this.lobbyItems = new ConcurrentHashMap<>();
        this.serviceContext = serviceContext;
        this.applicationPreSetup = SystemUtil.applicationPreSetup((String)gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME));
        this.distributionItemService = this.serviceContext.clusterProvider(Distributable.DATA_SCOPE).serviceProvider(DistributionItemService.NAME);
        this.logger = serviceContext.logger(PlatformLobbyServiceProvider.class);
        this.logger.warn("Lobby service provider started on ->"+gameServiceName+"-->"+gameName);
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
        distributionItemService.release(gameServiceName,name(),t.configurationTypeId(),t.configurationName());
    }

    public boolean onRegister(String category,String itemId){
        LobbyItem lobbyItem = new LobbyItem();
        lobbyItem.distributionKey(itemId);
        GameCluster _gc = serviceContext.deploymentServiceProvider().gameCluster(gameCluster.distributionKey());
        Descriptor app = _gc.serviceWithCategory(category);
        if(!applicationPreSetup.load(serviceContext,app,lobbyItem)){
            return false;
        }
        lobbyItem.setup();
        String lobbyTag = gameName+"/"+lobbyItem.configurationName();
        lobbyItems.put(lobbyTag,lobbyItem);
        ListenerOnLobby lobbyListener = lobbyListeners.get(lobbyTag);
        if(lobbyListener!=null) lobbyListener.listener.onUpdated(lobbyItem);
        return true;
    }
    public boolean onRelease(String category,String itemId){
        String lobbyTag = gameName+"/"+itemId;
        LobbyItem removed = lobbyItems.remove(lobbyTag);
        if(removed!=null){
            ListenerOnLobby listener = lobbyListeners.get(lobbyTag);
            if(listener!=null) listener.listener.onRemoved(removed);
        }
        return true;
    }

    public String registerConfigurableListener(Descriptor descriptor, Configurable.Listener listener) {
        lobbyListeners.put(descriptor.tag(),new ListenerOnLobby(descriptor,listener));
        List<LobbyItem> items = applicationPreSetup.list(serviceContext,descriptor,new LobbyItemObjectQuery("typeId/"+descriptor.category()));
        items.forEach((a)-> {
            if(!a.disabled()){
                a.setup();
                lobbyItems.put(gameName+"/"+a.configurationName(),a);
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
