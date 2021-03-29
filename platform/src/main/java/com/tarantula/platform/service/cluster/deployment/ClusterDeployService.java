package com.tarantula.platform.service.cluster.deployment;

import com.google.gson.GsonBuilder;
import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.Member;
import com.hazelcast.spi.*;
import com.icodesoftware.*;
import com.icodesoftware.service.DeployCode;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.logging.JDKLogger;
import com.tarantula.platform.*;
import com.tarantula.platform.bootstrap.ServiceBootstrap;
import com.tarantula.platform.service.Application;
import com.tarantula.platform.service.deployment.*;
import com.tarantula.platform.util.ResponseSerializer;

import java.util.*;


public class ClusterDeployService implements ManagedService, RemoteService, MembershipAwareService {

    private static TarantulaLogger log = JDKLogger.getLogger(ClusterDeployService.class);

    private NodeEngine nodeEngine;
    private TarantulaContext tarantulaContext;
    private DeploymentServiceProvider deploymentServiceProvider;
    private int scope;
    private GsonBuilder builder;
    @Override
    public void init(NodeEngine nodeEngine, Properties properties) {
        this.nodeEngine = nodeEngine;
        this.scope = Integer.parseInt(properties.getProperty("tarantula-scope"));
        tarantulaContext = TarantulaContext.getInstance();
        this.deploymentServiceProvider = this.tarantulaContext.deploymentService();
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(ResponseHeader.class,new ResponseSerializer());
        new ServiceBootstrap(tarantulaContext._integrationClusterStarted,tarantulaContext._deployServiceStarted,new DeployServiceBootstrap(this),"deploy-service",true).start();
    }
    public void setup(){
        log.info("Clustering deployment service started ["+nodeEngine.getConfig().getGroupConfig().getName()+"] on scope ["+this.scope+"]");
    }
    @Override
    public void reset() {

    }

    @Override
    public void shutdown(boolean b) {
        log.warn("deploy service stopped on scope ["+this.scope+"]");
    }

    @Override
    public DistributedObject createDistributedObject(String s) {
        return new DeployServiceProxy(s,nodeEngine,this);
    }

    @Override
    public void destroyDistributedObject(String s) {
    }
    public boolean addLobby(Descriptor descriptor,String publishingId){
        DataStore ds = this.tarantulaContext.masterDataStore();
        LobbyTypeIdIndex lobbyTypeIdIndex = new LobbyTypeIdIndex(tarantulaContext.bucketId(),descriptor.typeId());
        if(!ds.createIfAbsent(lobbyTypeIdIndex,false)){
            return false;
        }
        ModuleIndex moduleIndex = new ModuleIndex();
        moduleIndex.distributionKey(publishingId);
        moduleIndex.index(descriptor.typeId());
        ds.create(moduleIndex);
        descriptor.owner(publishingId);
        descriptor.label(LobbyDescriptor.LABEL);
        descriptor.onEdge(true);
        descriptor.resetEnabled(true);
        descriptor.disabled(true);
        ds.create(descriptor);
        lobbyTypeIdIndex.index(descriptor.distributionKey());
        lobbyTypeIdIndex.owner(publishingId);
        ds.update(lobbyTypeIdIndex);
        return descriptor.distributionKey()!=null;
    }
    public boolean enableLobby(String typeId){
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
    public boolean disableLobby(String typeId){
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
    public String enableApplication(String applicationId){
        DataStore ds = this.tarantulaContext.masterDataStore();
        DeploymentDescriptor app = new DeploymentDescriptor();
        app.distributionKey(applicationId);
        if(!ds.load(app)||!app.disabled()){
            return null;
        }
        app.disabled(false);
        ds.update(app);
        return app.typeId();
    }
    public String disableApplication(String applicationId){
        DataStore ds = this.tarantulaContext.masterDataStore();
        DeploymentDescriptor app = new DeploymentDescriptor();
        app.distributionKey(applicationId);
        if(!ds.load(app)||app.disabled()){
            return null;
        }
        app.disabled(true);
        ds.update(app);
        return app.typeId();
    }
    public String addApplication(Descriptor descriptor){
        DataStore ds = this.tarantulaContext.masterDataStore();
        LobbyTypeIdIndex query = new LobbyTypeIdIndex(tarantulaContext.bucketId(),descriptor.typeId());
        if(!ds.load(query)){
            return null;
        }
        descriptor.owner(query.index());
        descriptor.label(Application.LABEL);
        descriptor.onEdge(true);
        if(ds.create(descriptor)){
            if(!descriptor.typeId().equals(descriptor.moduleId())){
                //create index for moduleId
                IndexSet indexSet = new IndexSet();
                indexSet.distributionKey(descriptor.index());
                indexSet.label(ExposedGameService.INDEX_LABEL);
                indexSet.keySet.add(descriptor.distributionKey());
                if(!ds.createIfAbsent(indexSet,true)){
                    indexSet.keySet.add(descriptor.distributionKey());
                    ds.update(indexSet);
                }
                //log.warn("create index->"+descriptor.moduleId()+"<><><>"+descriptor.index());
            }
            return descriptor.distributionKey();
        }
        else {
            return null;
        }
    }
    public boolean addView(OnView view){
        DataStore ds = this.tarantulaContext.masterDataStore();
        LobbyTypeIdIndex query = new LobbyTypeIdIndex(tarantulaContext.bucketId(),view.owner());
        if(!ds.load(query)){
            return false;
        }
        view.owner(query.index());
        if(!ds.createIfAbsent(view,false)){
            ds.update(view);
        }
        return true;
    }
    public void updateView(OnView onView){
        this.deploymentServiceProvider.distributionCallback().updateView(onView);
    }
    public boolean resetModule(Descriptor descriptor){
        boolean[] suc ={false};
        DataStore dataStore = this.tarantulaContext.masterDataStore();
        LobbyTypeIdIndex lobbyTypeIdIndex = new LobbyTypeIdIndex(this.tarantulaContext.bucketId(),descriptor.typeId());
        if(!dataStore.load(lobbyTypeIdIndex)){
            if(descriptor.index()!=null){
                IndexSet indexSet = new IndexSet();
                indexSet.distributionKey(descriptor.index());
                indexSet.label(ExposedGameService.INDEX_LABEL);
                if(dataStore.load(indexSet)){
                    indexSet.keySet.forEach((k)->{
                        DeploymentDescriptor app = new DeploymentDescriptor();
                        app.distributionKey(k);
                        if(dataStore.load(app)){
                            app.codebase(descriptor.codebase());
                            app.moduleArtifact(descriptor.moduleArtifact());
                            app.moduleVersion(descriptor.moduleVersion());
                            dataStore.update(app);
                            suc[0]=true;
                        }
                    });
                }
            }
            return suc[0];
        }
        dataStore.list(new ApplicationQuery(lobbyTypeIdIndex.index()),(a)->{
            a.codebase(descriptor.codebase());
            a.moduleArtifact(descriptor.moduleArtifact());
            a.moduleVersion(descriptor.moduleVersion());
            dataStore.update(a);
            suc[0]=true;
            return true;
        });
        return suc[0];
    }
    @Override
    public void memberAdded(MembershipServiceEvent membershipServiceEvent) {
        if(this.scope == Distributable.DATA_SCOPE){
            Member lm = nodeEngine.getLocalMember();
            int sz = nodeEngine.getClusterService().getSize();
            int pt = 0;
            for(Member m : nodeEngine.getClusterService().getMembers()){
                if(lm.getUuid().equals(m.getUuid())){
                    break;
                }
                pt++;
            }
            log.warn("partition updating on member added->["+pt+"/"+sz+"]"+lm.getUuid());
            for(int i=0;i<this.tarantulaContext.platformRoutingNumber;i++){
                this.tarantulaContext.integrationCluster().onPartition(i,i%sz==pt);
            }
            this.tarantulaContext.tarantulaCluster().onReload();
        }
        this.deploymentServiceProvider.distributionCallback().memberAdded(membershipServiceEvent.getMember().getUuid());
    }

    @Override
    public void memberRemoved(MembershipServiceEvent membershipServiceEvent) {
        if(this.scope==Distributable.DATA_SCOPE){
            Member lm = nodeEngine.getLocalMember();
            int sz = nodeEngine.getClusterService().getSize();
            int pt = 0;
            for(Member m : nodeEngine.getClusterService().getMembers()){
                if(lm.getUuid().equals(m.getUuid())){
                    break;
                }
                pt++;
            }
            log.warn("partition updating on member removed->["+pt+"/"+sz+"]"+lm.getUuid());
            for(int i=0;i<this.tarantulaContext.platformRoutingNumber;i++){
                this.tarantulaContext.integrationCluster().onPartition(i,i%sz==pt);
            }
            this.tarantulaContext.tarantulaCluster().onReload();
        }
        this.deploymentServiceProvider.distributionCallback().memberRemoved(membershipServiceEvent.getMember().getUuid());
    }

    @Override
    public void memberAttributeChanged(MemberAttributeServiceEvent memberAttributeServiceEvent) {
    }

    public boolean enableGameCluster(String gameClusterId){
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
    public boolean disableGameCluster(String gameClusterId){
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
        return suc1&&suc2&&suc3;//make sure all disabled
    }
    public GameCluster createGameCluster(String owner, String name,boolean tournamentEnabled,String publishingId){
        GameCluster gameCluster = new GameCluster();
        try {
            DataStore mds = this.tarantulaContext.masterDataStore();
            gameCluster.property(GameCluster.NAME,name);
            gameCluster.property(GameCluster.OWNER,owner);
            gameCluster.property(GameCluster.PUBLISHING_ID,publishingId);
            gameCluster.property(GameCluster.ACCESS_KEY,"mock access key");
            gameCluster.property(GameCluster.TIMESTAMP,0);
            gameCluster.property(GameCluster.TOURNAMENT_ENABLED,tournamentEnabled);
            gameCluster.property(GameCluster.DISABLED,true);
            mds.create(gameCluster);//create first and discharge if any errors on loop
            gameCluster.successful(true);
            XMLParser parser = new XMLParser();
            String typePrefix = name.toLowerCase();
            parser.parse(Thread.currentThread().getContextClassLoader().getResourceAsStream("game-cluster-basic-plan.xml"));
            for (LobbyConfiguration configuration : parser.configurations) {
                configuration.descriptor.typeId(configuration.descriptor.typeId().replace("game",typePrefix));//lower case only typeId
                if(configuration.descriptor.typeId().endsWith("-lobby")){
                    gameCluster.property(GameCluster.GAME_LOBBY,configuration.descriptor.typeId());
                }
                else if(configuration.descriptor.typeId().endsWith("-service")){
                    gameCluster.property(GameCluster.GAME_SERVICE,configuration.descriptor.typeId());
                }
                else if(configuration.descriptor.typeId().endsWith("-data")){
                    gameCluster.property(GameCluster.GAME_DATA,configuration.descriptor.typeId());
                }
                LobbyTypeIdIndex lobbyTypeIdIndex = new LobbyTypeIdIndex(tarantulaContext.bucketId(),configuration.descriptor.typeId());
                if(mds.load(lobbyTypeIdIndex)){//stop existed
                    throw new RuntimeException("["+name+"] duplicated");
                }
                //log.warn("Create named lobby type id->"+configuration.descriptor.typeId());
                Descriptor descriptor = configuration.descriptor;
                descriptor.owner(publishingId);
                descriptor.label(LobbyDescriptor.LABEL);
                descriptor.onEdge(true);
                descriptor.resetEnabled(true);
                descriptor.disabled(true);//pending launch
                descriptor.deployCode(DeployCode.USER_GAME_CLUSTER);
                mds.create(descriptor);
                lobbyTypeIdIndex.index(descriptor.distributionKey());
                lobbyTypeIdIndex.owner(gameCluster.distributionKey());
                mds.create(lobbyTypeIdIndex);
                configuration.applications.forEach((a)->{
                    a.owner(descriptor.distributionKey());
                    a.label(Application.LABEL);
                    a.onEdge(true);
                    a.tournamentEnabled(tournamentEnabled);
                    a.typeId(descriptor.typeId());//replaced with named type id
                    a.subtypeId(a.subtypeId().replace("game",typePrefix));
                    a.tag(a.tag().replace("game",typePrefix));
                    a.applicationClassName(tarantulaContext.singleModuleApplication);
                    mds.create(a);
                });
            }
            gameCluster.message("["+name+"] game created successfully");
            mds.update(gameCluster);
        }catch (Exception ex){
            gameCluster.message(ex.getMessage());
            gameCluster.successful(false);
        }
        return gameCluster;
    }
    public void addServerPushEvent(Event event){
        this.deploymentServiceProvider.distributionCallback().registerServerPushEvent(event);
    }
    public void removeServerPushEvent(String serverId){
        this.deploymentServiceProvider.distributionCallback().releaseServerPushEvent(serverId);
    }
    public void ackServerPushEvent(String serverId){
        this.deploymentServiceProvider.distributionCallback().ackServerPushEvent(serverId);
    }
    public void upload(String fileName,byte[] content){
        this.tarantulaContext._writeContent(fileName,content);
    }
    public void launchGameCluster(String gameClusterKey){
        GameCluster gameCluster = new GameCluster();
        gameCluster.distributionKey(gameClusterKey);
        this.deploymentServiceProvider.distributionCallback().addGameCluster(gameCluster);
    }
    public void shutdownGameCluster(String gameClusterKey){
        GameCluster gameCluster = new GameCluster();
        gameCluster.distributionKey(gameClusterKey);
        this.deploymentServiceProvider.distributionCallback().closeGameCluster(gameCluster);
    }
    public void launchApplication(String typeId,String applicationId){
        this.deploymentServiceProvider.distributionCallback().addApplication(typeId,applicationId);
    }
    public void shutdownApplication(String typeId,String applicationId){
        this.deploymentServiceProvider.distributionCallback().removeApplication(typeId,applicationId);
    }
    public void launchModule(String typeId){
        this.deploymentServiceProvider.distributionCallback().addLobby(typeId);
    }
    public void shutdownModule(String typeId){
        this.deploymentServiceProvider.distributionCallback().removeLobby(typeId);
    }
    public void updateModule(Descriptor descriptor){
        this.deploymentServiceProvider.distributionCallback().updateModule(descriptor);
    }
    public void updateResource(String contentUrl,String resourceName){
        this.deploymentServiceProvider.distributionCallback().updateResource(contentUrl,resourceName);
    }
    public void deployModule(String contentUrl,String resourceName){
        this.deploymentServiceProvider.distributionCallback().updateModule(contentUrl,resourceName);
    }
    public void serverPushEventSync(String memberId){
        this.deploymentServiceProvider.distributionCallback().syncServerPushEvent(memberId);//dispatch task
    }

    public void getConnection(String lobbyTag,Session session){
        this.deploymentServiceProvider.distributionCallback().getConnection(lobbyTag,session);
    }
    public void sync(String key){
        this.deploymentServiceProvider.distributionCallback().syncKey(key);
    }
}
