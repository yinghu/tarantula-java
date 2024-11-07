package com.tarantula.platform.service.deployment;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.icodesoftware.protocol.GameServerListener;
import com.icodesoftware.service.*;
import com.icodesoftware.logging.JDKLogger;

import com.icodesoftware.util.FileUtil;
import com.icodesoftware.util.SnowflakeKey;
import com.tarantula.admin.GameClusterQuery;
import com.tarantula.platform.*;
import com.tarantula.platform.service.*;

import com.tarantula.platform.service.persistence.DataStoreViewer;

import com.tarantula.platform.util.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlatformDeploymentServiceProvider implements DeploymentServiceProvider{

    private TarantulaLogger log = JDKLogger.getLogger(PlatformDeploymentServiceProvider.class);

    private ClusterProvider integrationCluster;
    private SecureRandom secureRandom;

    ConcurrentHashMap<String,TypedListener> oListeners = new ConcurrentHashMap<>();

    //callback on access index service
    CopyOnWriteArrayList<AccessIndexService.Listener> aListeners = new CopyOnWriteArrayList<>();

    //on view, on lobby , configs mappings
    ConcurrentHashMap<String,Configurable> vMap = new ConcurrentHashMap<>();

    //push event cache mappings
    ConcurrentHashMap<String, GameServerListener> cListeners = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, GameClusterEventListener> eListeners = new ConcurrentHashMap<>();


    //module class loader mappings
    ConcurrentHashMap<String,DynamicModuleClassLoader> cMap = new ConcurrentHashMap<>();

    //content cache ( web admin )
    ConcurrentHashMap<String,Content> rMap = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String,ExposedGameService> eMap = new ConcurrentHashMap<>();


    private TarantulaContext tarantulaContext;


    String contentDir;

    AtomicBoolean onAccessIndex;

    private MetricsListener metricsListener;
    private DistributionCallback distributionCallback;

    private ClusterProvider.ClusterStore clusterStore;
    @Override
    public void start() throws Exception {
        this.secureRandom = new SecureRandom();
        this.onAccessIndex = new AtomicBoolean(true);
        this.distributionCallback = new DistributionCallbackProvider(this);
    }

    @Override
    public void shutdown() throws Exception {
        log.info("Platform deployment service provider shut down");
    }
    @Override
    public String name() {
        return DeploymentServiceProvider.NAME;
    }




    public Module module(Descriptor descriptor){
        if(descriptor.codebase()!=null){
            DynamicModuleClassLoader mc = cMap.computeIfAbsent(descriptor.moduleId(),(k)-> {
                DynamicModuleClassLoader _cl = new DynamicModuleClassLoader(descriptor);
                _cl._load();
                return _cl;
            });
            ModuleProxy module = new ModuleProxy(descriptor);
            mc.proxies.add(module);
            return module;
        }
        else{
            return _internalModule(descriptor.moduleName());
        }
    }

    public boolean saveContent(String typeId,Session session,Content content){
        String dir = "web/"+typeId+"/"+session.distributionKey();
        String save = "#"+content.fileName()+"#"+content.revisionNumber()+"#"+content.type();
        integrationCluster.deployService().onUpload(dir+save,content.data());
        return true;

        //String dir = tarantulaContext.deployDir+"/web/"+typeId+"/"+session.distributionKey();
        //FileUtil.createDirectory(dir);
        //String save = "/"+content.fileName()+"."+content.revisionNumber()+"."+content.type();

        //try(BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(dir+save))){
            //bufferedOutputStream.write(content.data());
            //bufferedOutputStream.flush();
            //return true;
        //}
        //catch (Exception ex){
            //log.error("Error on save content :"+session.distributionKey(),ex);
            //return false;
        //}
    }

    public Content loadContent(String typeId,Session session,Content content){
        String dir = tarantulaContext.deployDir+"/web/"+typeId+"/"+session.distributionKey();
        FileUtil.createDirectory(dir);
        String save = "/"+content.fileName()+"."+content.revisionNumber()+"."+content.type();
        try(BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(dir+save))){
            byte[] data = bufferedInputStream.readAllBytes();
            return ContentMapping.forSave(data,content.fileName(),content.revisionNumber());
        }
        catch (Exception ex){
            log.error("Error on load content :"+session.distributionKey(),ex);
            return content;
        }
    }

    private Content fromContext(String name){
        return rMap.computeIfAbsent(name,(rk)->{
                byte[] ret = new byte[0];
                BufferedInputStream cin=null;
                try {//read from deploy dir
                    cin = new BufferedInputStream(new FileInputStream(contentDir + "/" + name));
                    ret = cin.readAllBytes();
                    cin.close();
                }catch (Exception ex1){
                    try {if(cin!=null){cin.close();}}catch (Exception ex2){}
                    //read from backup
                    try{
                        cin = new BufferedInputStream(Thread.currentThread().getContextClassLoader().getResourceAsStream(name));
                        ret = cin.readAllBytes();
                    }catch (Exception ex3){
                        log.warn("Resource ["+name+"] not existed");
                    }
                    finally {
                        if(cin!=null){
                            try{cin.close();}catch (Exception ex4){}
                        }
                    }
                }
                return new ContentMapping(ret,SystemUtil.mimeType(name),ret.length>0);
            }
        );
    }
    void checkContent(OnView onView){
        try{
            //log.warn("update view->"+onView.toString());
            Path _web_resource = Paths.get(this.contentDir+"/"+onView.moduleContext());
            if(!Files.exists(_web_resource)){
                Files.createDirectories(_web_resource);
            }
            int ix = onView.moduleResourceFile().lastIndexOf('/');

            String rn = ix<0?onView.moduleResourceFile():(onView.moduleResourceFile().substring(ix+1));
            File f = new File(this.tarantulaContext.deployDir+"/"+rn);
            if(!f.exists()){
                return;
            }
            boolean isRoot = onView.moduleContext().startsWith("root");
            String x = isRoot?(contentDir+"/"+onView.moduleContext()+"/"+onView.moduleResourceFile()):(contentDir+"/"+onView.moduleResourceFile());
            File fe = new File(x);
            if(!fe.exists()||fe.lastModified()<f.lastModified()){
                BufferedInputStream fin = new BufferedInputStream(new FileInputStream(f));
                BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(fe));
                fos.write(fin.readAllBytes());
                fin.close();
                fos.flush();
                fos.close();
            }
        }catch (Exception ex){
            log.error("content load error",ex);
        }
    }
    public Content resource(String name){
        //log.warn("load resource ["+name+"]");
        return fromContext(name);
    }

    public void deleteResource(String name){
        rMap.remove(name);
        File deleting = new File(this.contentDir+"/"+name);
        if(deleting.exists()) deleting.delete();
    }
    public void resource(Descriptor descriptor, String name, Module.OnResource onResource){
        DynamicModuleClassLoader dyn = cMap.get(descriptor.moduleId());
        dyn.loadResource(name,onResource);
    }

    public boolean resetModule(Descriptor descriptor){
        //update app desc via typeId
        boolean[] suc ={false};
        DataStore dataStore = this.tarantulaContext.masterDataStore();
        LobbyTypeIdIndex lobbyTypeIdIndex = new LobbyTypeIdIndex(this.tarantulaContext.node().deploymentId(),descriptor.typeId());
        if(!dataStore.load(lobbyTypeIdIndex)){
            if(descriptor.index()!=null){
                //IndexSet indexSet = new IndexSet();
                //indexSet.distributionKey(descriptor.index());
                //indexSet.label(ExposedGameService.INDEX_LABEL);
                //if(dataStore.load(indexSet)){
                    //indexSet.keySet().forEach((k)->{
                        //DeploymentDescriptor app = new DeploymentDescriptor();
                        //app.distributionKey(k);
                        //if(dataStore.load(app)){
                            //app.codebase(descriptor.codebase());
                            //app.moduleArtifact(descriptor.moduleArtifact());
                            //app.moduleVersion(descriptor.moduleVersion());
                            //dataStore.update(app);
                            //suc[0]=true;
                        //}
                    //});
                //}
            }
            return suc[0];
        }
        /***
        dataStore.list(new ApplicationQuery(lobbyTypeIdIndex.index()),(a)->{
            a.codebase(descriptor.codebase());
            a.moduleArtifact(descriptor.moduleArtifact());
            a.moduleVersion(descriptor.moduleVersion());
            dataStore.update(a);
            suc[0]=true;
            return true;
        });**/
        //return suc[0];

        //DeployService deployService = this.tarantulaContext.integrationCluster().deployService();
        //boolean suc = deployService.resetModule(descriptor);
        if(suc[0]){
            this.integrationCluster.deployService().onUpdateModule(descriptor);
        }
        return suc[0];
    }
    public ClassLoader classLoader(String moduleId){
        return cMap.get(moduleId);
    }
    private Module _internalModule(String mname){
        try{
            return (Module)Class.forName(mname).getConstructor().newInstance();
        }catch (Exception ex){
            log.error(mname,ex);
            throw new RuntimeException(ex);
        }
    }
    public Response exportModule(Descriptor descriptor){
        DynamicModuleClassLoader mc = new DynamicModuleClassLoader(descriptor);
        Response response = new ResponseHeader();
        mc.loadResource("export.json",(in)->{
            try{
                JsonParser parser = new JsonParser();
                JsonObject jo = parser.parse(new InputStreamReader(in)).getAsJsonObject();
                String _moduleId = jo.get(ExposedGameService.MODULE_ID).getAsString();
                response.message(_moduleId);
                AccessIndex accessIndex = this.tarantulaContext.clusterProvider().accessIndexService().setIfAbsent(_moduleId,AccessIndex.SYSTEM_INDEX);
                jo.getAsJsonArray("exposedServiceList").forEach((es)->{
                    JsonObject je = es.getAsJsonObject();
                    ExposedGameService egs = new ExposedGameService();
                    egs.property(ExposedGameService.MODULE_ID,_moduleId);
                    egs.property(ExposedGameService.MODULE_INDEX,accessIndex.distributionKey());
                    egs.name(je.get(ExposedGameService.NAME).getAsString());
                    egs.property(ExposedGameService.DESCRIPTION,je.get(ExposedGameService.DESCRIPTION).getAsString());
                    egs.property(ExposedGameService.MODULE_CODE_BASE,descriptor.codebase());
                    egs.property(ExposedGameService.MODULE_ARTIFACT,descriptor.moduleArtifact());
                    egs.property(ExposedGameService.MODULE_VERSION,descriptor.moduleVersion());
                    egs.property(ExposedGameService.MODULE_NAME,je.get(ExposedGameService.MODULE_NAME).getAsString());
                    egs.property(ExposedGameService.DEPLOY_PRIORITY,je.get(ExposedGameService.DEPLOY_PRIORITY).getAsInt());
                    egs.property(ExposedGameService.ACCESS_CONTROL,je.get(ExposedGameService.ACCESS_CONTROL).getAsInt());
                    eMap.put(egs.name(),egs);
                });
            }catch (Exception ex){
                log.warn("failed to parse export.json",ex);
                response.message(ex.getMessage());
            }
        });
        return response;
    }

    public <T extends OnAccess> void onGameClusterEvent(T event){
        if(event.typeId()==null) return;
        GameClusterEventListener gameClusterEventListener = eListeners.get(event.typeId());
        if(gameClusterEventListener==null) return;
        gameClusterEventListener.onGameClusterEvent(event);
    }
    public void registerGameClusterEventListener(GameClusterEventListener gameClusterEventListener){
        this.eListeners.put(gameClusterEventListener.typeId(),gameClusterEventListener);
    }
    public void unregisterGameClusterEventListener(GameClusterEventListener gameClusterEventListener){
        this.eListeners.remove(gameClusterEventListener.typeId());
    }

    public List<Descriptor> gameServiceList(){
        return this.tarantulaContext.availableServices();
    }
    public Response deployModule(String contextUrl,String resourceName){
        Response checked =  this.tarantulaContext.checkModule(contextUrl,resourceName);
        if(!checked.successful()){
            return checked;
        }
        boolean suc = this.tarantulaContext.integrationCluster().deployService().onDeployModule(contextUrl,resourceName);
        checked.successful(suc);
        checked.message(suc?"deployed->"+resourceName:"failed->"+resourceName);
        return checked;
    }

    public Response createModule(Descriptor descriptor){
        DynamicModuleClassLoader mc = new DynamicModuleClassLoader(descriptor);
        XMLParser xmlParser = new XMLParser();
        Response response = new ResponseHeader();
        response.successful(false);
        mc.loadResource("descriptor.xml",(in)->{
            try{
                xmlParser.parse(in);
                response.successful(true);
            }catch (Exception ex){
                log.warn("failed to parse descriptor.xml",ex);
                response.message(ex.getMessage());
            }
        });
        if(!response.successful()){
            return response;
        }
        LobbyConfiguration a = xmlParser.configurations.get(0);
        AccessIndex publishId = this.tarantulaContext.clusterProvider().accessIndexService().set(a.descriptor.typeId(),AccessIndex.SYSTEM_INDEX);
        if(publishId==null){
            response.successful(false);
            response.message("module already existed");
            return response;
        }
        DataStore ds = this.tarantulaContext.masterDataStore();
        LobbyTypeIdIndex lobbyTypeIdIndex = new LobbyTypeIdIndex(tarantulaContext.node().deploymentId(),descriptor.typeId());
        if(!ds.createIfAbsent(lobbyTypeIdIndex,false)){
            response.successful(false);
            response.message("module already existed");
            return response;
        }
        ModuleIndex moduleIndex = new ModuleIndex();
        moduleIndex.distributionKey(publishId.distributionKey());
        moduleIndex.index(descriptor.typeId());
        ds.create(moduleIndex);
        descriptor.owner(publishId.distributionKey());
        descriptor.label(LobbyDescriptor.LABEL);
        descriptor.onEdge(true);
        descriptor.resetEnabled(true);
        descriptor.disabled(true);
        ds.create(descriptor);
        lobbyTypeIdIndex.index(descriptor.distributionKey());
        lobbyTypeIdIndex.owner(publishId.distributionKey());
        ds.update(lobbyTypeIdIndex);
        a.applications.forEach((b)->{
            b.codebase(descriptor.codebase());
            b.moduleArtifact(descriptor.moduleArtifact());
            b.moduleVersion(descriptor.moduleVersion());
            b.applicationClassName(this.tarantulaContext.singleModuleApplication);
            this.createApplication(b,null,false);
        });
        response.successful(true);
        response.message("module created");
        return response;
    }

    public boolean createApplication(Descriptor descriptor,String configName,boolean launching){
        DataStore ds = this.tarantulaContext.masterDataStore();
        LobbyTypeIdIndex query = new LobbyTypeIdIndex(tarantulaContext.node().deploymentId(),descriptor.typeId());
        if(!ds.load(query)){
            return false;
        }
        descriptor.ownerKey(SnowflakeKey.from(query.lobbyId()));
        descriptor.label(ApplicationProvider.LABEL);
        descriptor.onEdge(true);
        if(!ds.create(descriptor)) return false;
        if(!descriptor.typeId().equals(descriptor.moduleId())){
            //create index for moduleId
            //IndexSet indexSet = new IndexSet();
            //indexSet.distributionKey(descriptor.index());
            //indexSet.label(ExposedGameService.INDEX_LABEL);
            //indexSet.addKey(descriptor.distributionKey());
            //if(!ds.createIfAbsent(indexSet,true)){
                //indexSet.addKey(descriptor.distributionKey());
                //ds.update(indexSet);
            //}
            //log.warn("create index->"+descriptor.moduleId()+"<><><>"+descriptor.index());
        }
        this.integrationCluster.deployService().onLaunchApplication(descriptor.typeId(),descriptor.distributionId());
        return true;
    }
    public boolean updateApplication(Descriptor descriptor,OnAccess properties){
        DataStore dataStore = this.tarantulaContext.masterDataStore();
        if(!dataStore.load(descriptor)) return false;
        descriptor.resetEnabled((boolean)properties.property("resetEnabled"));
        boolean privateAccess = (boolean)properties.property("privateAccess");
        descriptor.accessMode(privateAccess?Access.PRIVATE_ACCESS_MODE:0);
        return dataStore.update(descriptor);
    }
    public boolean enableApplication(long applicationId){
        DataStore ds = this.tarantulaContext.masterDataStore();
        DeploymentDescriptor app = new DeploymentDescriptor();
        app.distributionId(applicationId);
        if(!ds.load(app)||!app.disabled()){
            return false;
        }
        app.disabled(false);
        ds.update(app);
        DeployService  deployService = this.tarantulaContext.integrationCluster().deployService();
        return deployService.onLaunchApplication(app.typeId(),applicationId);
    }
    public boolean disableApplication(long applicationId){
        DataStore ds = this.tarantulaContext.masterDataStore();
        DeploymentDescriptor app = new DeploymentDescriptor();
        app.distributionId(applicationId);
        if(!ds.load(app)||app.disabled()){
            return false;
        }
        app.disabled(true);
        ds.update(app);
        DeployService deployService = this.tarantulaContext.integrationCluster().deployService();
        return deployService.onShutdownApplication(app.typeId(),applicationId);
    }

    public boolean launchModule(String typeId){
        if(!enableLobby(typeId)) return false;
        this.integrationCluster.deployService().onLaunchModule(typeId);
        return true;
    }
    public boolean shutdownModule(String typeId){
        if(!disableLobby(typeId)) return false;
        this.integrationCluster.deployService().onShutdownModule(typeId);
        return true;
    }

    @Override
    public void setup(ServiceContext serviceContext){
        this.metricsListener = (n,v)->{};
        this.tarantulaContext = (TarantulaContext)serviceContext;
        this.integrationCluster = serviceContext.clusterProvider();
        try{
            contentDir = this.tarantulaContext.deployDir+"/web";
            Path _path = Paths.get(this.tarantulaContext.deployDir);
            if(!Files.exists(_path)){
                Files.createDirectories(_path);
            }
            Path _web_root = Paths.get(this.tarantulaContext.deployDir+"/web/root");
            if(!Files.exists(_web_root)){
                Files.createDirectories(_web_root);
            }
            Path _web_resource = Paths.get(this.tarantulaContext.deployDir+"/web/resource");
            if(!Files.exists(_web_resource)){
                Files.createDirectories(_web_resource);
            }
            Path _web_presence = Paths.get(this.tarantulaContext.deployDir+"/web/presence");
            if(!Files.exists(_web_presence)){
                Files.createDirectories(_web_presence);
            }
            Path _web_account = Paths.get(this.tarantulaContext.deployDir+"/web/account");
            if(!Files.exists(_web_account)){
                Files.createDirectories(_web_account);
            }
            Path _web_admin = Paths.get(this.tarantulaContext.deployDir+"/web/admin");
            if(!Files.exists(_web_admin)){
                Files.createDirectories(_web_admin);
            }
            Path _web_sudo = Paths.get(this.tarantulaContext.deployDir+"/web/sudo");
            if(!Files.exists(_web_sudo)){
                Files.createDirectories(_web_sudo);
            }
            Path _config_game = Paths.get(this.tarantulaContext.deployDir+"/conf/root");
            if(!Files.exists(_config_game)){
                Files.createDirectories(_config_game);
            }
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
        //log.info("Platform deployment provider started");
    }

    @Override
    public void waitForData() {
        /**
        this.publisher = this.tarantulaContext.integrationCluster().subscribe(NAME,(e)->{
            String tp = e.trackId();
            RecoverableListener listener = tMap.get(tp);
            if(listener!=null){
                listener.onUpdated(e.stub(),e.trackId(),e.index(),e.payload());
            }
            return false;
        });**/
        this.clusterStore = this.tarantulaContext.integrationCluster().clusterStore(ClusterProvider.ClusterStore.SMALL,DeploymentServiceProvider.NAME,true,false,false);
        log.info("Platform deployment service started on ["+this.tarantulaContext.dataBucketNode+"/"+this.tarantulaContext.dataBucketGroup+"]");
    }

    @Override
    public void register(ServiceProvider serviceProvider) {
        this.tarantulaContext.deployServiceProvider(serviceProvider);
    }
    public void release(ServiceProvider serviceProvider){
        this.tarantulaContext.releaseServiceProvider(serviceProvider.name());
    }

    public void updateView(OnView onView){
        checkContent(onView);
        OnView removed = (OnView) vMap.remove(onView.viewId());
        if(removed!=null){
            rMap.remove(removed.moduleResourceFile());
        }
        rMap.remove(onView.moduleResourceFile());
        vMap.put(onView.viewId(),onView);
    }
    public OnView view(String viewId){
        return (OnView)vMap.get(viewId);
    }
    public Response deployResource(String contentUrl,String resourceName){
        OnView view = new OnViewTrack();
        view.moduleResourceFile(resourceName);
        view.moduleContext(contentUrl);
        Response response = this.tarantulaContext.checkResource(view,"web");
        if(!response.successful()){
            return response;
        }
        boolean suc = this.tarantulaContext.integrationCluster().deployService().onUpdateResource(contentUrl,resourceName);
        response.successful(suc);
        response.message(suc?"ok":"failed");
        return  response;
    }


    public Response createView(OnView onView){
        Response response = this.tarantulaContext.checkResource(onView,"web");
        if(!response.successful()){
            return response;
        }
        DataStore ds = this.tarantulaContext.masterDataStore();
        LobbyTypeIdIndex query = new LobbyTypeIdIndex(tarantulaContext.node().deploymentId(),onView.owner());
        if(!ds.load(query)){
            return new ResponseHeader("create/update view","cannot create view",false);
        }
        boolean updated = false;
        onView.owner(query.index());
        if(!ds.createIfAbsent(onView,false)){
            ds.update(onView);
            updated = true;
        }
        if(updated){
            this.tarantulaContext.integrationCluster().deployService().onUpdateView(onView);
        }
        return new ResponseHeader("create/update view",updated?"view deployed->"+onView.moduleContext():"cannot create view",updated);
    }

    private void register(OnLobby onLobby){
        if(onLobby.deployCode()<2) return;
        vMap.put(onLobby.typeId(),onLobby);
        if(onLobby.resetEnabled()&&TarantulaContext.lobbySubscriptionEnabled) this.tarantulaContext.tokenValidatorProvider().onCheck(onLobby);

        oListeners.forEach((k,o)->
            {
                if(o.type.equals(onLobby.configurationType())){
                    o.listener.onUpdated(onLobby);
                }
            }
        );
    }
    public String registerConfigurableListener(String type, Configurable.Listener listener){
        String regKey = UUID.randomUUID().toString();
        oListeners.put(regKey,new TypedListener(type,listener));
        return regKey;
    }
    public void unregisterConfigurableListener(String registryKey){
        oListeners.remove(registryKey);
    }

    public <T extends Configuration> T configuration(String config){
        return (T)tarantulaContext.configuration(config);
    }
    public <T extends Configurable> void register(T configurable){
        if(configurable instanceof OnView){
            updateView((OnView)configurable);
            return;
        }
        if(configurable instanceof OnLobby){
            register((OnLobby)configurable);
            return;
        }
        if(configurable instanceof GameCluster){
            oListeners.forEach((k,o)->
                    {
                        if(o.type.equals(GameCluster.GAME_CLUSTER_CONFIGURATION_TYPE)){
                            o.listener.onLoaded(configurable);
                        }
                    }
            );
            return;
        }
        vMap.putIfAbsent(configurable.key().asString(),configurable);
        configurable.registered();
    }

    public <T extends OnAccess> boolean launchGameCluster(T gameCluster){
        DeployService deployService = this.tarantulaContext.integrationCluster().deployService();
        if(this.enableGameCluster(gameCluster.distributionId()) && deployService.onStartGameService(gameCluster.distributionId())){
            return tarantulaContext.integrationCluster().deployService().onLaunchGameCluster(gameCluster.distributionId());
        }
        else{
            return false;
        }
    }
    public <T extends OnAccess> boolean shutdownGameCluster(T gameCluster){
        DeployService deployService = this.tarantulaContext.integrationCluster().deployService();
        if(this.disableGameCluster(gameCluster.distributionId())){
            log.warn("close game cluster->"+gameCluster.distributionId());
            return deployService.onShutdownGameCluster(gameCluster.distributionId());
        }
        else{
            return false;
        }
    }

    public <T extends OnAccess> T updateGameCluster(long gameClusterId,OnAccess properties){
        GameCluster gameCluster = this.tarantulaContext.loadGameCluster(gameClusterId);
        DataStore mds = this.tarantulaContext.masterDataStore();
        if(gameCluster==null) throw new RuntimeException("["+gameClusterId+"] not existed");
        String playMode = (String) properties.property("playMode");
        boolean dedicated = (boolean)properties.property("dedicated");
        String gameIcon = (String) properties.property("gameIcon");
        String developerIcon = (String) properties.property("developerIcon");
        String developer = (String) properties.property("developer");
        gameCluster.mode = playMode;
        gameCluster.developer = developer;
        gameCluster.dedicated =dedicated;
        gameCluster.gameIcon = gameIcon;
        gameCluster.developerIcon = developerIcon;
        gameCluster.upgradeVersion++;
        mds.update(gameCluster);
        return (T)gameCluster;
    }
    public <T extends OnAccess> List<T> gameClusterList(Access access){
        GameClusterQuery gameClusterQuery = new GameClusterQuery(access.primary()?access.distributionId():access.distributionId());
        return (List<T>)this.tarantulaContext.masterDataStore().list(gameClusterQuery);
    }
    public  <T extends OnAccess> T createGameCluster(Account account,String name,OnAccess properties){
        AccessIndex accessIndex = this.tarantulaContext.clusterProvider().accessIndexService().set(name,AccessIndex.SYSTEM_INDEX);//name+"-"+mode
        if(accessIndex==null){
            GameCluster gc = new GameCluster();
            gc.successful(false);
            gc.message("duplicated name ["+name+"]");
            return (T)gc;
        }
        long publishingId = accessIndex.distributionId();
        String playMode = (String) properties.property("playMode");
        boolean tournamentEnabled = (boolean)properties.property("tournamentEnabled");
        boolean dedicated = (boolean)properties.property("dedicated");
        String gameIcon = (String) properties.property("gameIcon");
        String developerIcon = (String) properties.property("developerIcon");
        String developer = (String) properties.property("developer");
        String gameServiceProvider = (String) properties.property("gameServiceProvider");
        Configuration gameClusterConfig = (Configuration)properties.property(OnAccess.GAME_CLUSTER_CONFIG);
        int maxLobbyCount = ((Number)gameClusterConfig.property("maxGameLobbyCount")).intValue();
        int maxZoneCount = ((Number)gameClusterConfig.property("maxGameZoneCount")).intValue();
        int maxArenaCount = ((Number)gameClusterConfig.property("maxGameArenaCount")).intValue();
        int maxDataSize = ((Number)gameClusterConfig.property("maxDataSizeOnSet")).intValue();

        GameCluster gameCluster = new GameCluster();
        try {
            DataStore mds = this.tarantulaContext.masterDataStore();
            gameCluster.name(name);
            gameCluster.mode = playMode;
            gameCluster.accountId = account.distributionId();
            gameCluster.developer = developer;
            gameCluster.gameServiceProvider = gameServiceProvider;
            gameCluster.tournamentEnabled = tournamentEnabled;
            gameCluster.disabled(true);
            gameCluster.dedicated = dedicated;
            gameCluster.gameIcon = gameIcon;
            gameCluster.developerIcon = developerIcon;
            gameCluster.maxLobbyCount = maxLobbyCount;
            gameCluster.maxZoneCount = maxZoneCount;
            gameCluster.maxArenaCount = maxArenaCount;
            gameCluster.maxDataSize = maxDataSize;
            gameCluster.upgradeVersion = 1;
            gameCluster.distributionId(publishingId);
            if(!mds.createIfAbsent(gameCluster,false)) throw new RuntimeException("failed to create game cluster");//create first and discharge if any errors on loop
            gameCluster.ownerKey(new SnowflakeKey(account.distributionId()));
            mds.createEdge(gameCluster,GameCluster.LABEL);
            gameCluster.ownerKey(new SnowflakeKey(this.tarantulaContext.node().deploymentId()));
            mds.createEdge(gameCluster,GameCluster.LABEL);

            gameCluster.successful(true);
            XMLParser parser = new XMLParser();
            String typePrefix = name.toLowerCase();
            parser.parse(Thread.currentThread().getContextClassLoader().getResourceAsStream(tournamentEnabled?"tournament-game-cluster-basic-plan.xml":"game-cluster-basic-plan.xml"));
            for (LobbyConfiguration configuration : parser.configurations) {
                configuration.descriptor.typeId(configuration.descriptor.typeId().replace("game",typePrefix));//lower case only typeId
                Lobby _lobby = new DefaultLobby(configuration.descriptor);
                if(configuration.descriptor.typeId().endsWith("-lobby")){
                    gameCluster.gameLobbyName = (configuration.descriptor.typeId());
                    gameCluster.gameLobby = _lobby;
                }
                else if(configuration.descriptor.typeId().endsWith("-service")){
                    gameCluster.gameServiceName=(configuration.descriptor.typeId());
                    this.tarantulaContext.availableServices().forEach((s)-> configuration.applications.add((DeploymentDescriptor)s));
                    gameCluster.serviceLobby = _lobby;
                }
                else if(configuration.descriptor.typeId().endsWith("-data")){
                    gameCluster.gameDataName =(configuration.descriptor.typeId());
                    gameCluster.dataLobby  = _lobby;
                }
                LobbyTypeIdIndex lobbyTypeIdIndex = new LobbyTypeIdIndex(tarantulaContext.node().deploymentId(),configuration.descriptor.typeId());
                if(mds.load(lobbyTypeIdIndex)){//stop existed
                    throw new RuntimeException("["+name+"] duplicated");
                }
                Descriptor descriptor = configuration.descriptor;
                descriptor.ownerKey(new SnowflakeKey(publishingId));
                descriptor.label(LobbyDescriptor.LABEL);
                descriptor.onEdge(true);
                descriptor.resetEnabled(true);
                descriptor.disabled(true);//pending launch
                descriptor.deployCode(DeployCode.USER_GAME_CLUSTER);
                mds.create(descriptor);
                lobbyTypeIdIndex = new LobbyTypeIdIndex(tarantulaContext.node().deploymentId(),configuration.descriptor.typeId(),descriptor.distributionId(),gameCluster.distributionId());
                mds.createIfAbsent(lobbyTypeIdIndex,false);
                configuration.applications.forEach((a)->{
                    a.ownerKey(descriptor.key());
                    a.label(ApplicationProvider.LABEL);
                    a.onEdge(true);
                    a.tournamentEnabled(tournamentEnabled);
                    a.typeId(descriptor.typeId());//replaced with named type id
                    a.tag(a.tag().replaceFirst("game",typePrefix));
                    a.applicationClassName(tarantulaContext.singleModuleApplication);
                    mds.create(a);
                    _lobby.addEntry(a);
                });
            }
            gameCluster.message("["+name+"] game created successfully");
            mds.update(gameCluster);
            gameCluster.setup(this.tarantulaContext);
            oListeners.forEach((k,o)->
                {
                    if(o.type.equals(GameCluster.GAME_CLUSTER_CONFIGURATION_TYPE)){
                        o.listener.onCreated(gameCluster);
                    }
                }
            );
        }catch (Exception ex){
            log.error("error on create game cluster",ex);
            gameCluster.message(ex.getMessage());
            gameCluster.successful(false);
        }
        return (T)gameCluster;
    }
    public <T extends Configuration,S extends OnAccess> T configuration(S gameCluster,String config){
        return (T)this.tarantulaContext.configuration((GameCluster)gameCluster,config);
    }
    public <T extends OnAccess> T gameCluster(long key){
        return (T)tarantulaContext.loadGameCluster(key);
    }
    public Lobby lobby(String typeId){
        DataStore mds = this.tarantulaContext.masterDataStore();
        LobbyTypeIdIndex lobbyTypeIdIndex = new LobbyTypeIdIndex(this.tarantulaContext.node().deploymentId(),typeId);
        if(!mds.load(lobbyTypeIdIndex)) return null;
        LobbyDescriptor lb = new LobbyDescriptor();
        lb.distributionId(lobbyTypeIdIndex.lobbyId());
        if(!mds.load(lb)) return null;
        Lobby lobby = new DefaultLobby(lb);
        List<DeploymentDescriptor> apps = this.tarantulaContext.masterDataStore().list(new ApplicationQuery(lb.distributionId()));
        apps.forEach((a)->{
            lobby.addEntry(a);
        });
        return lobby;
    }
    public String resetCode(String key){
        String code = UUID.randomUUID().toString();
        clusterStore.mapSet(code.getBytes(),key.getBytes());
        return code;
    }
    public String checkCode(String resetCode){
        byte[] ret = clusterStore.mapRemove(resetCode.getBytes());
        return (ret!=null?new String(ret):"");
    }

    public byte[] serverKey(String typeId){
        byte[] key = new byte[KEY_SIZE];
        secureRandom.nextBytes(key);
        byte[] existing = clusterStore.mapSetIfAbsent(typeId.getBytes(),key);
        return existing!=null?existing:key;
    }

    public String registerGameServerListener(GameServerListener gameChannelListener){
        String regKey = gameChannelListener.typeId();//UUID.randomUUID().toString();
        cListeners.put(regKey,gameChannelListener);
        return regKey;
    }
    public GameServerListener gameServerListener(String typeId){
        return cListeners.get(typeId);
    }

    public void unregisterGameServerListener(String registerKey){
        cListeners.remove(registerKey);
    }

    public void registerAccessIndexListener(AccessIndexService.Listener listener){
        if(onAccessIndex.get()){
            listener.onStart();
        }
        else{
            listener.onStop();
        }
        aListeners.add(listener);
    }

    public void onStart(EndPoint endPoint){
        cListeners.forEach((k,v)->{
            v.onStart(endPoint);
        });
    }


    public File issueDataStoreBackup(int scope){
        return this.tarantulaContext.deploymentDataStoreProvider.backup(scope);
    }

    public List<String> listDataStore(int scope) {
        return this.tarantulaContext.dataStoreProvider().list(scope);
    }

    public DataStoreSummary validDataStore(String dataStore){
        DataStore ds = this.tarantulaContext.deploymentDataStoreProvider.lookup(dataStore);
        return ds!=null? new DataStoreViewer(tarantulaContext,ds): null;
    }

    public ClusterProvider.Summary clusterSummary(){
        return integrationCluster.summary();
    }

    public List<String> listServiceView(){
       return tarantulaContext.serviceViewList;
    }

    public List<String> listMetricsView(){
        return this.tarantulaContext.metricsList();
    }

    public Metrics metrics(String name){
        return this.tarantulaContext.metrics(name);
    }
    public Transaction transaction(int scope){
        return tarantulaContext.transaction(scope);
    }
    public void atMidnight(){

    }

    public DistributionCallback distributionCallback(){
        return this.distributionCallback;
    }


    public <T extends Configurable> void release(T configurable){
        Configurable removed = this.vMap.remove(configurable.distributionKey());
        removed.released();
    }

    @Override
    public void registerMetricsListener(MetricsListener metricsListener){
        this.metricsListener = metricsListener;
    }

    public void onUpdated(String mkey,double delta){
        this.metricsListener.onUpdated(mkey,delta);
    }

    boolean enableLobby(String typeId){
        DataStore ds = this.tarantulaContext.masterDataStore();
        LobbyTypeIdIndex query = new LobbyTypeIdIndex(tarantulaContext.node().deploymentId(),typeId);
        if(!ds.load(query)){
            return false;
        }
        LobbyDescriptor lobbyDescriptor = new LobbyDescriptor();
        lobbyDescriptor.distributionId(query.lobbyId());
        if(!ds.load(lobbyDescriptor)||!lobbyDescriptor.disabled()){
            return false;
        }
        lobbyDescriptor.disabled(false);
        ds.update(lobbyDescriptor);
        return true;
    }

    boolean disableLobby(String typeId){
        DataStore ds = this.tarantulaContext.masterDataStore();
        LobbyTypeIdIndex query = new LobbyTypeIdIndex(tarantulaContext.node().deploymentId(),typeId);
        if(!ds.load(query)){
            return false;
        }
        LobbyDescriptor lobbyDescriptor = new LobbyDescriptor();
        lobbyDescriptor.distributionId(query.lobbyId());
        if(!ds.load(lobbyDescriptor)||lobbyDescriptor.disabled()){
            return false;
        }
        lobbyDescriptor.disabled(true);
        ds.update(lobbyDescriptor);
        return true;
    }

    boolean enableGameCluster(long gameClusterId){
        GameCluster gameCluster = this.tarantulaContext.loadGameCluster(gameClusterId);
        DataStore mds = this.tarantulaContext.masterDataStore();
        if(gameCluster==null){
            return false;
        }
        String data = gameCluster.gameDataName;//1
        String lobby = gameCluster.gameLobbyName;//2
        String service = gameCluster.gameServiceName;//3
        boolean suc1 = enableLobby(data);
        boolean suc2 = enableLobby(lobby);
        boolean suc3 = enableLobby(service);
        gameCluster.disabled(false);
        mds.update(gameCluster);
        return suc1&&suc2&&suc3;//make sure all enabled
    }

    boolean disableGameCluster(long gameClusterId){
        GameCluster gameCluster = this.tarantulaContext.loadGameCluster(gameClusterId);
        DataStore mds = this.tarantulaContext.masterDataStore();
        if(gameCluster==null){
            return false;
        }
        String data = gameCluster.gameDataName;//1
        String lobby = gameCluster.gameLobbyName;//2
        String service = gameCluster.gameServiceName;//3
        boolean suc1 = disableLobby(data);
        boolean suc2 = disableLobby(lobby);
        boolean suc3 = disableLobby(service);
        gameCluster.disabled(true);
        mds.update(gameCluster);
        return suc1&&suc2&&suc3;
    }

    public NodeShutdownOperator nodeShutdownOperator(Access access){
        if(!access.role().equals(AccessControl.root.name())) return null;
        DeployService deployService = tarantulaContext.integrationCluster().deployService();
        return (NodeShutdownOperator)deployService;
    }
    class ModuleProxy implements Module{

        private Module module;
        private ApplicationContext applicationContext;
        private Descriptor descriptor;
        public ModuleProxy(Descriptor descriptor){
            this.descriptor = descriptor;
        }
        @Override
        public void onJoin(Session session) throws Exception {
            this.module.onJoin(session);
        }

        @Override
        public boolean onRequest(Session session, byte[] payload) throws Exception {
            return this.module.onRequest(session,payload);
        }

        @Override
        public void setup(ApplicationContext context) throws Exception {
            this.applicationContext = context;
            _setup();
            this.module.setup(context);
        }

        @Override
        public void clear(){
            try{
                if(this.module!=null){
                    this.module.clear();
                }
            }catch (Exception ex){
                log.warn("error on old module clear fix it and reset again",ex);
            }
        }
        private void _setup(){
            DynamicModuleClassLoader moduleClassLoader = cMap.get(descriptor.moduleId());
            this.module = moduleClassLoader.newModule(descriptor.moduleName());
        }

        public void reset(){
            try{
                DynamicModuleClassLoader moduleClassLoader = cMap.get(descriptor.moduleId());
                this.clear();//clear on old instance
                this.module = moduleClassLoader.newModule(descriptor.moduleName());
                this.module.setup(applicationContext);//inject the limited content to prevent unexpected calls from modules
                log.warn("Module ["+descriptor.moduleName()+"] reset on singleton instance ["+descriptor.tag()+"]");
            }catch (Exception ex){
                log.error("error on module reset, fix it and reset again",ex);
            }
        }
    }
}
