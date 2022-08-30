package com.tarantula.platform.service.deployment;

import com.icodesoftware.*;
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
    private TarantulaLogger log;

    public DistributionCallbackProvider(TarantulaContext tarantulaContext,PlatformDeploymentServiceProvider platformDeploymentServiceProvider){
        this.tarantulaContext = tarantulaContext;
        this.platformDeploymentServiceProvider = platformDeploymentServiceProvider;
        log = this.tarantulaContext.logger(DistributionCallbackProvider.class);
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
    public <T extends OnAccess> void addGameCluster(T gameCluster) {
        if(!this.tarantulaContext.masterDataStore().load(gameCluster)) return;
        this.tarantulaContext.setGameClusterOnLobby((GameCluster)gameCluster,new OnLobbyListener(this.platformDeploymentServiceProvider));
    }

    @Override
    public <T extends OnAccess> void closeGameCluster(T gameCluster) {
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
    public <T extends OnAccess> void onGameClusterCreated(T t) {

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
    public void addApplication(String typeId ,String applicationId) {
        this.tarantulaContext.setApplicationOnLobby(typeId,applicationId);
    }

    @Override
    public void removeApplication(String typeId, String applicationId) {

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
    public void updateView(OnView onView) {

        //checkContent(onView);
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
    public void updateResource(String contentUrl,String resourceName) {
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
    public boolean addChannel(String typeId, Channel channel) {
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
    public void addConnection(String typeId, Connection connection) {
        platformDeploymentServiceProvider.cListeners.forEach((k,v)->{
            if(v.typeId().equals(typeId)) v.onConnection(connection);
        });
    }

    @Override
    public void pingConnection(String typeId, String serverId) {
        try{
            platformDeploymentServiceProvider.cListeners.forEach((k,v)->{
                if(v.typeId().equals(typeId)) v.onPing(serverId);
            });
        }catch (Exception ex){
            log.error("error on ping",ex);
        }
    }

    @Override
    public void removeConnection(String typeId, Connection connection) {
        platformDeploymentServiceProvider.cListeners.forEach((k,v)->{
            if(v.typeId().equals(typeId)) v.onDisConnection(connection);
        });
    }

    public void stopAccessIndex(){
        platformDeploymentServiceProvider.onAccessIndex.set(false);
        platformDeploymentServiceProvider.aListeners.forEach((a)->a.onStop());
    }
    public void startAccessIndex(){
        platformDeploymentServiceProvider.onAccessIndex.set(true);
        platformDeploymentServiceProvider.aListeners.forEach((a)->a.onStart());
    }

    @Override
    public void syncKey(String key) {
        if(platformDeploymentServiceProvider.vMap.containsKey(key)){
            Configurable configurable = platformDeploymentServiceProvider.vMap.get(key);
            configurable.updated(new ServiceContextProxy(this.tarantulaContext));
        }
    }
}
