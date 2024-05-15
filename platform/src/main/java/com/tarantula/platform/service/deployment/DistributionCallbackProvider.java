package com.tarantula.platform.service.deployment;

import com.icodesoftware.*;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.protocol.GameServerListener;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.service.OnLobby;
import com.tarantula.platform.GameCluster;
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
    public void onGameServiceStarted(long gameClusterId) {
        GameCluster gameCluster = this.tarantulaContext.loadGameCluster(gameClusterId);
        if(gameCluster==null){
            log.warn("No game cluster found ["+gameClusterId+"]");
            return;
        }
        this.tarantulaContext.setGameServiceProvider(gameCluster);
    }

    @Override
    public void onGameClusterLaunched(long gameClusterId) {
        GameCluster gameCluster = this.tarantulaContext.loadGameCluster(gameClusterId);
        if(gameCluster==null){
            log.warn("No game cluster found ["+gameClusterId+"]");
            return;
        }
        this.tarantulaContext.setGameClusterOnLobby(gameCluster,new OnLobbyListener(this.platformDeploymentServiceProvider));
    }

    @Override
    public void onGameClusterShutdown(long gameClusterId) {
        GameCluster gameCluster = this.tarantulaContext.loadGameCluster(gameClusterId);
        if(gameCluster==null){
            log.warn("No game cluster found ["+gameClusterId+"]");
            return;
        }
        onModuleShutdown(gameCluster.gameDataName);
        onModuleShutdown(gameCluster.gameLobbyName);
        onModuleShutdown(gameCluster.gameServiceName);
        this.tarantulaContext.releaseServiceProvider(gameCluster.gameServiceName);
        this.tarantulaContext.unloadGameCluster(gameClusterId);
    }



    //@Override
    public void onModuleShutdown(String typeId) {

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
    public void onApplicationLaunched(String typeId ,long applicationId) {
        this.tarantulaContext.setApplicationOnLobby(typeId,applicationId);
    }

    @Override
    public void onApplicationShutdown(String typeId, long applicationId) {

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
                platformDeploymentServiceProvider.disableLobby(d.typeId());
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
    public void onConnectionRegistered(String typeId, Connection connection) {
        GameServerListener gameServerListener = platformDeploymentServiceProvider.cListeners.get(typeId);
        if(gameServerListener==null) return;
        gameServerListener.onConnectionRegistered(connection);
    }

    @Override
    public void onConnectionVerified(String typeId, String serverId) {
        try{
            GameServerListener gameServerListener = platformDeploymentServiceProvider.cListeners.get(typeId);
            if(gameServerListener==null) return;
            gameServerListener.onConnectionVerified(serverId);
        }catch (Exception ex){
            log.error("error on ping",ex);
        }
    }

    @Override
    public void onConnectionStarted(String typeId,Connection connection) {
        try{
            GameServerListener gameServerListener = platformDeploymentServiceProvider.cListeners.get(typeId);
            if(gameServerListener==null) return;
            gameServerListener.onConnectionStarted(connection);
        }catch (Exception ex){
            log.error("error on start connection",ex);
        }
    }

    @Override
    public void onConnectionReleased(String typeId, Connection connection) {
        GameServerListener gameServerListener = platformDeploymentServiceProvider.cListeners.get(typeId);
        if(gameServerListener==null) return;
        gameServerListener.onConnectionReleased(connection);
    }

    public void onAccessIndexDisabled(){
        platformDeploymentServiceProvider.onAccessIndex.set(false);
        platformDeploymentServiceProvider.aListeners.forEach((a)->a.onStop());
    }
    public void onAccessIndexEnabled(){
        platformDeploymentServiceProvider.onAccessIndex.set(true);
        platformDeploymentServiceProvider.aListeners.forEach((a)->a.onStart());
    }


}
