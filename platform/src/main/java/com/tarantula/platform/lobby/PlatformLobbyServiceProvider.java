package com.tarantula.platform.lobby;

import com.google.gson.JsonObject;
import com.icodesoftware.Configurable;
import com.icodesoftware.Descriptor;

import com.icodesoftware.logging.JDKLogger;

import com.icodesoftware.service.ServiceContext;

import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.item.PlatformItemServiceProvider;


import java.util.concurrent.ConcurrentHashMap;


public class PlatformLobbyServiceProvider extends PlatformItemServiceProvider{

    public static final String NAME = "lobby";

    private String gameTypeId;
    private ConcurrentHashMap<String,ListenerOnLobby> lobbyListeners;
    private ConcurrentHashMap<String,LobbyItem> lobbyItems;

    public PlatformLobbyServiceProvider(PlatformGameServiceProvider gameServiceProvider){
        super(gameServiceProvider,NAME);
        this.gameTypeId = gameCluster.typeId();
    }
    @Override
    public void setup(ServiceContext serviceContext) {
        super.setup(serviceContext);
        this.lobbyListeners = new ConcurrentHashMap<>();
        this.lobbyItems = new ConcurrentHashMap<>();
        this.logger = JDKLogger.getLogger(PlatformLobbyServiceProvider.class);
        this.logger.warn("Lobby service provider started on ->"+gameServiceName+"-->"+gameTypeId);
    }

    @Override
    public void start() throws Exception {
        String resp = serviceContext.node().homingAgent().onConfiguration(gameCluster.distributionId(),"Map");
        JsonObject mapList = JsonUtil.parse(resp);
        mapList.get("list").getAsJsonArray().forEach(e->{
            JsonObject mo = e.getAsJsonObject();
            LobbyItem lobbyItem = new LobbyItem(mo);
            lobbyItems.put(gameTypeId+"/"+mo.get("ConfigurationName").getAsString(),lobbyItem);
        });
        this.logger.warn("Lobby service provider started on->"+gameServiceName+" with lobby configuration");
    }


    public boolean onItemRegistered(String category,String itemId){
        LobbyItem lobbyItem = new LobbyItem();
        lobbyItem.distributionKey(itemId);
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
        lobbyItems.forEach((k,v)->{
            ListenerOnLobby lobbyListener = lobbyListeners.get(k);
            if(lobbyListener!=null && k.equals(lobbyListener.lobby.tag())) lobbyListener.listener.onLoaded(v);
        });
        return null;
    }
    public void unregisterConfigurableListener(String registryKey){
        lobbyListeners.remove(registryKey);
    }



    public boolean onItemRegistered(int publishId){
        String config = serviceContext.node().homingAgent().onConfigurationRegistered(publishId);
        logger.warn(config);
        return true;
    }
    public boolean onItemReleased(int publishId){
        logger.warn("release local resource with ["+publishId+"]");
        return true;
    }
}
