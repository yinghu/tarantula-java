package com.tarantula.platform.service.deployment;

import com.icodesoftware.*;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.service.OnLobby;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.LobbyDescriptor;
import com.tarantula.platform.LobbyTypeIdIndex;
import com.tarantula.platform.TarantulaContext;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DistributionCallbackProvider implements DeploymentServiceProvider.DistributionCallback {

    private TarantulaContext tarantulaContext;
    private PlatformDeploymentServiceProvider platformDeploymentServiceProvider;
    private TarantulaLogger log = JDKLogger.getLogger(DistributionCallbackProvider.class);

    public DistributionCallbackProvider(PlatformDeploymentServiceProvider platformDeploymentServiceProvider){
        this.tarantulaContext = TarantulaContext.getInstance();
        this.platformDeploymentServiceProvider = platformDeploymentServiceProvider;

    }
    @Override
    public <T extends OnAccess> void addGameService(T gameCluster) {
        if(!this.tarantulaContext.masterDataStore().load(gameCluster)){
            log.warn("No game cluster found ["+gameCluster.distributionKey()+"]");
            return;
        }
        this.tarantulaContext.setGameServiceProvider((GameCluster)gameCluster);
    }

    @Override
    public void onGameClusterLaunched(String gameClusterId) {
        GameCluster gameCluster = new GameCluster();
        gameCluster.distributionKey(gameClusterId);
        if(!this.tarantulaContext.masterDataStore().load(gameCluster)) return;
        this.tarantulaContext.setGameClusterOnLobby(gameCluster,new OnLobbyListener(this.platformDeploymentServiceProvider));
    }

    @Override
    public void onGameClusterShutdown(String gameClusterId) {
        GameCluster gameCluster = new GameCluster();
        gameCluster.distributionKey(gameClusterId);
        if(!tarantulaContext.masterDataStore().load(gameCluster)){
            log.warn("No game cluster found ["+gameCluster.distributionKey()+"]");
            return;
        }
        this.tarantulaContext.releaseServiceProvider((String) gameCluster.property(GameCluster.GAME_SERVICE));
        removeLobby((String)gameCluster.property(GameCluster.GAME_DATA));
        removeLobby((String)gameCluster.property(GameCluster.GAME_LOBBY));
        removeLobby((String)gameCluster.property(GameCluster.GAME_SERVICE));
    }

    @Override
    public void onGameClusterCreated(String gameClusterId) {
        GameCluster gameCluster = new GameCluster();
        gameCluster.distributionKey(gameClusterId);
        gameCluster.dataStore(this.tarantulaContext.masterDataStore());
        if(!tarantulaContext.masterDataStore().load(gameCluster)){
            log.warn("Game cluster ["+gameClusterId+"] not found");
        }
        gameCluster.setup(this.tarantulaContext);
        platformDeploymentServiceProvider.oListeners.forEach((k,o)->
            {
                if(o.type.equals(GameCluster.GAME_CLUSTER_CONFIGURATION_TYPE)){
                    o.listener.onCreated(gameCluster);
                }
            }
        );
    }

    public boolean onGameClusterEnabled(String gameClusterId){
        GameCluster gameCluster = new GameCluster();
        gameCluster.distributionKey(gameClusterId);
        DataStore mds = this.tarantulaContext.masterDataStore();
        if(!mds.load(gameCluster)){
            return false;
        }
        String data = (String) gameCluster.property(GameCluster.GAME_DATA);//1
        String lobby = (String) gameCluster.property(GameCluster.GAME_LOBBY); //2
        String service = (String) gameCluster.property(GameCluster.GAME_SERVICE);;//3
        boolean suc1 =enableLobby(data);
        boolean suc2 =enableLobby(lobby);
        boolean suc3 =enableLobby(service);
        gameCluster.property(GameCluster.DISABLED,false);
        mds.update(gameCluster);
        return suc1&&suc2&&suc3;//make sure all enabled
    }
    public boolean onGameClusterDisabled(String gameClusterId){
        GameCluster gameCluster = new GameCluster();
        gameCluster.distributionKey(gameClusterId);
        DataStore mds = this.tarantulaContext.masterDataStore();
        if(!mds.load(gameCluster)){
            return false;
        }
        String data = (String) gameCluster.property(GameCluster.GAME_DATA);//1
        String lobby = (String) gameCluster.property(GameCluster.GAME_LOBBY); //2
        String service = (String) gameCluster.property(GameCluster.GAME_SERVICE);;//3
        boolean suc1 = disableLobby(data);
        boolean suc2 = disableLobby(lobby);
        boolean suc3 = disableLobby(service);
        gameCluster.property(GameCluster.DISABLED,true);
        mds.update(gameCluster);
        return suc1&&suc2&&suc3;
    }

    @Override
    public void addLobby(String typeId) {
        AccessIndex accessIndex = this.tarantulaContext.accessIndexService().get(typeId);
        this.tarantulaContext.setOnLobby(typeId,accessIndex.distributionKey(),new OnLobbyListener(platformDeploymentServiceProvider));
    }

    @Override
    public void removeLobby(String typeId) {

        platformDeploymentServiceProvider.oListeners.forEach((k,ol)->{
            if(platformDeploymentServiceProvider.vMap.containsKey(typeId)){//skip system level modules
                OnLobby onLobby =(OnLobby) platformDeploymentServiceProvider.vMap.get(typeId);
                onLobby.closed(true);
                if(ol.type.equals(onLobby.configurationType())){
                    ol.listener.onUpdated(onLobby);
                }
            }
        });
        this.tarantulaContext.unsetLobby(typeId,(d)->{//clean up from runtime context
            //remove modules
            if(d.moduleName()!=null&&d.codebase()!=null){ //clean class loader if all apps removed on the class loader
                DynamicModuleClassLoader dynamicModuleClassLoader = platformDeploymentServiceProvider.cMap.remove(d.moduleId());
                if(dynamicModuleClassLoader!=null){
                    log.warn("Module resource clear on ["+d.codebase()+"/"+d.moduleArtifact()+"/"+d.moduleVersion()+"]");
                    dynamicModuleClassLoader._clear();
                }
            }
        });
    }

    @Override
    public void onApplicationLaunched(String typeId ,String applicationId) {
        this.tarantulaContext.setApplicationOnLobby(typeId,applicationId);
    }

    @Override
    public void onApplicationShutdown(String typeId, String applicationId) {

        this.tarantulaContext.unsetApplication(typeId,applicationId,(d)->{
            if(d.type().equals(Descriptor.TYPE_LOBBY)){
                platformDeploymentServiceProvider.oListeners.forEach((k,ol)->{ //remove lobby entry
                    OnLobby onLobby = (OnLobby) platformDeploymentServiceProvider.vMap.get(d.typeId());
                    onLobby.closed(true);
                    if(onLobby.typeId().equals(ol.type)){
                        ol.listener.onUpdated(onLobby);
                    }//removed lobby entry
                });
                //rListeners.remove(d.tag()); //remove instance entry
                this.tarantulaContext.integrationCluster().deployService().disableLobby(d.typeId());
            }
            if(d.moduleName()!=null&&d.codebase()!=null){ //clean class loader if all apps removed on the class loader
                DynamicModuleClassLoader dynamicModuleClassLoader = platformDeploymentServiceProvider.cMap.remove(d.moduleId());
                if(dynamicModuleClassLoader!=null){
                    log.warn("Module resource clear on ["+d.codebase()+"/"+d.moduleArtifact()+"/"+d.moduleVersion()+"]");
                    dynamicModuleClassLoader._clear();
                }
            }
        });
    }

    @Override
    public void updateModule(Descriptor descriptor) {

        DynamicModuleClassLoader mc = platformDeploymentServiceProvider.cMap.computeIfPresent(descriptor.moduleId(),(k,c)->{
            DynamicModuleClassLoader nmc = new DynamicModuleClassLoader(descriptor);
            nmc.proxies.addAll(c.proxies);
            c._clear();
            nmc._load();
            return nmc;
        });
        mc.proxies.forEach((mp)->{
            mp.reset();
        });

        platformDeploymentServiceProvider.cMap.computeIfPresent(descriptor.moduleId(),(k,c)->{
            c.reset(descriptor.resetEnabled());
            return c;
        });
        try{//agent operation into the platform vm
            Runtime rt  =Runtime.getRuntime();
            rt.exec("java -jar gec-agent-1.0.jar "+ProcessHandle.current().pid()+" "+descriptor.moduleId());
        }catch (Exception ex){
            log.error("error from agent",ex);
        }
    }

    @Override
    public void onViewUpdated(OnView onView) {

        platformDeploymentServiceProvider.checkContent(onView);
        OnView removed = (OnView) platformDeploymentServiceProvider.vMap.remove(onView.viewId());
        if(removed!=null){
            platformDeploymentServiceProvider.rMap.remove(removed.moduleResourceFile());
        }
        platformDeploymentServiceProvider.rMap.remove(onView.moduleResourceFile());
        platformDeploymentServiceProvider.vMap.put(onView.viewId(),onView);

    }

    @Override
    public void updateModule(String contentUrl,String resourceName) {
        try{
            //content dir deployDir/module
            Path _path = Paths.get(this.tarantulaContext.deployDir+"/module/"+contentUrl);
            if(!Files.exists(_path)){
                Files.createDirectories(_path);
            }
            File f = new File(this.tarantulaContext.deployDir+"/"+resourceName);
            File fe = new File(this.tarantulaContext.deployDir+"/module/"+contentUrl+"/"+resourceName);
            if(!fe.exists()||fe.lastModified()<f.lastModified()){
                BufferedInputStream fin = new BufferedInputStream(new FileInputStream(f));
                BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(fe));
                fos.write(fin.readAllBytes());
                fin.close();
                fos.flush();
                fos.close();
                platformDeploymentServiceProvider.rMap.remove(contentUrl+"/"+resourceName);//clear cache
            }
        }catch (Exception ex){
            log.error(contentUrl+"/"+resourceName,ex);
        }
    }

    @Override
    public void onResourceUpdated(String contentUrl,String resourceName) {
        try{
            String contentDir = platformDeploymentServiceProvider.contentDir;
            Path _path = Paths.get(contentDir+"/"+contentUrl);
            if(!Files.exists(_path)){
                Files.createDirectories(_path);
            }
            File f = new File(this.tarantulaContext.deployDir+"/"+resourceName);
            File fe = new File(contentDir+"/"+contentUrl+"/"+resourceName);
            if(!fe.exists()||fe.lastModified()<f.lastModified()){
                BufferedInputStream fin = new BufferedInputStream(new FileInputStream(f));
                BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(fe));
                fos.write(fin.readAllBytes());
                fin.close();
                fos.flush();
                fos.close();
                platformDeploymentServiceProvider.rMap.remove(contentUrl+"/"+resourceName);//clear cache
            }
        }catch (Exception ex){
            log.error(contentUrl+"/"+resourceName,ex);
        }
    }

    @Override
    public boolean onChannelRegistered(String typeId, Channel channel) {
        try{
            platformDeploymentServiceProvider.cListeners.forEach((k,v)->{
                if(v.typeId().equals(typeId)) v.onChannel(channel);
            });
            return true;
        }catch (Exception ex){
            log.error("error on add channel",ex);
            return false;
        }
    }

    @Override
    public void onConnectionRegistered(String typeId, Connection connection) {
        platformDeploymentServiceProvider.cListeners.forEach((k,v)->{
            if(v.typeId().equals(typeId)) v.onConnection(connection);
        });
    }

    @Override
    public void onConnectionVerified(String typeId, String serverId) {
        try{
            platformDeploymentServiceProvider.cListeners.forEach((k,v)->{
                if(v.typeId().equals(typeId)) v.onPing(serverId);
            });
        }catch (Exception ex){
            log.error("error on ping",ex);
        }
    }

    @Override
    public void onConnectionReleased(String typeId, Connection connection) {
        platformDeploymentServiceProvider.cListeners.forEach((k,v)->{
            if(v.typeId().equals(typeId)) v.onDisConnection(connection);
        });
    }

    public void onAccessIndexDisabled(){
        platformDeploymentServiceProvider.onAccessIndex.set(false);
        platformDeploymentServiceProvider.aListeners.forEach((a)->a.onStop());
    }
    public void onAccessIndexEnabled(){
        platformDeploymentServiceProvider.onAccessIndex.set(true);
        platformDeploymentServiceProvider.aListeners.forEach((a)->a.onStart());
    }

    @Override
    public void onConfigurableUpdated(String key) {
        if(platformDeploymentServiceProvider.vMap.containsKey(key)){
            Configurable configurable = platformDeploymentServiceProvider.vMap.get(key);
            configurable.updated(new ServiceContextProxy(this.tarantulaContext));
        }
    }

    private boolean enableLobby(String typeId){
        DataStore ds = this.tarantulaContext.masterDataStore();
        LobbyTypeIdIndex query = new LobbyTypeIdIndex(tarantulaContext.bucketId(),typeId);
        if(!ds.load(query)){
            return false;
        }
        LobbyDescriptor lobbyDescriptor = new LobbyDescriptor();
        lobbyDescriptor.distributionKey(query.index());
        if(!ds.load(lobbyDescriptor)||!lobbyDescriptor.disabled()){
            return false;
        }
        lobbyDescriptor.disabled(false);
        ds.update(lobbyDescriptor);
        return true;
    }

    private boolean disableLobby(String typeId){
        DataStore ds = this.tarantulaContext.masterDataStore();
        LobbyTypeIdIndex query = new LobbyTypeIdIndex(tarantulaContext.bucketId(),typeId);
        if(!ds.load(query)){
            return false;
        }
        LobbyDescriptor lobbyDescriptor = new LobbyDescriptor();
        lobbyDescriptor.distributionKey(query.index());
        if(!ds.load(lobbyDescriptor)||lobbyDescriptor.disabled()){
            return false;
        }
        lobbyDescriptor.disabled(true);
        ds.update(lobbyDescriptor);
        return true;
    }
}
