package com.tarantula.platform.service.deployment;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.icodesoftware.protocol.GameChannelListener;
import com.icodesoftware.service.*;
import com.icodesoftware.logging.JDKLogger;

import com.tarantula.platform.*;
import com.tarantula.platform.event.*;
import com.tarantula.platform.room.ChannelStub;
import com.tarantula.platform.service.*;
import com.tarantula.platform.service.metrics.ServiceView;
import com.tarantula.platform.util.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlatformDeploymentServiceProvider implements DeploymentServiceProvider{

    private TarantulaLogger log = JDKLogger.getLogger(PlatformDeploymentServiceProvider.class);

    private EventService integrationEventService;
    private ClusterProvider integrationCluster;
    private SecureRandom secureRandom;

    ConcurrentHashMap<String,TypedListener> oListeners = new ConcurrentHashMap<>();

    //callback on access index service
    CopyOnWriteArrayList<AccessIndexService.Listener> aListeners = new CopyOnWriteArrayList<>();

    //on view, on lobby , configs mappings
    ConcurrentHashMap<String,Configurable> vMap = new ConcurrentHashMap<>();

    //push event cache mappings
    ConcurrentHashMap<String,GameChannelListener> cListeners = new ConcurrentHashMap<>();


    //module class loader mappings
    ConcurrentHashMap<String,DynamicModuleClassLoader> cMap = new ConcurrentHashMap<>();

    //content cache ( web admin )
    ConcurrentHashMap<String,Content> rMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String,ExposedGameService> eMap = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String,RecoverableListener> tMap = new ConcurrentHashMap<>();
    private EventService publisher;
    private TarantulaContext tarantulaContext;


    String contentDir;

    AtomicBoolean onAccessIndex;

    private MetricsListener metricsListener;
    private DistributionCallback distributionCallback;

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
            ex.printStackTrace();
        }
    }
    public Content resource(String name){
        //log.warn("load resource ["+name+"]");
        return fromContext(name);
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
                IndexSet indexSet = new IndexSet();
                indexSet.distributionKey(descriptor.index());
                indexSet.label(ExposedGameService.INDEX_LABEL);
                if(dataStore.load(indexSet)){
                    indexSet.keySet().forEach((k)->{
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
                AccessIndex accessIndex = this.tarantulaContext.accessIndexService().setIfAbsent(_moduleId,0);
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
    public List<Descriptor> gameServiceList(){
        return this.tarantulaContext.availableServices();
        //ArrayList<T> arrayList = new ArrayList<>();
        //ExposedGameService exposedGameService = new ExposedGameService();

        //eMap.forEach((k,es)->{
            //arrayList.add((T)es);
        //});
        //return arrayList;
    }
    public Descriptor gameService(String name){
        return null;
        //return (T)eMap.get(name);
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
        AccessIndex publishId = this.tarantulaContext.accessIndexService().set(a.descriptor.typeId(),0);
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
            this.createApplication(b,null,null,false);
        });
        response.successful(true);
        response.message("module created");
        return response;
    }

    public boolean createApplication(Descriptor descriptor, String postSetup,String configName,boolean launching){
        DataStore ds = this.tarantulaContext.masterDataStore();
        LobbyTypeIdIndex query = new LobbyTypeIdIndex(tarantulaContext.node().deploymentId(),descriptor.typeId());
        if(!ds.load(query)){
            return false;
        }
        descriptor.owner(query.index());
        descriptor.label(ApplicationProvider.LABEL);
        descriptor.onEdge(true);
        if(!ds.create(descriptor)) return false;
        if(!descriptor.typeId().equals(descriptor.moduleId())){
            //create index for moduleId
            IndexSet indexSet = new IndexSet();
            indexSet.distributionKey(descriptor.index());
            indexSet.label(ExposedGameService.INDEX_LABEL);
            indexSet.addKey(descriptor.distributionKey());
            if(!ds.createIfAbsent(indexSet,true)){
                indexSet.addKey(descriptor.distributionKey());
                ds.update(indexSet);
            }
            //log.warn("create index->"+descriptor.moduleId()+"<><><>"+descriptor.index());
        }
        if(postSetup!=null){
            ApplicationPreSetup setup = SystemUtil.applicationPreSetup(postSetup);
            setup.setup(tarantulaContext,descriptor,configName);
        }
        this.integrationCluster.deployService().onLaunchApplication(descriptor.typeId(),descriptor.distributionKey());
        return true;
    }

    public boolean enableApplication(String applicationId){
        DataStore ds = this.tarantulaContext.masterDataStore();
        DeploymentDescriptor app = new DeploymentDescriptor();
        app.distributionKey(applicationId);
        if(!ds.load(app)||!app.disabled()){
            return false;
        }
        app.disabled(false);
        ds.update(app);
        DeployService  deployService = this.tarantulaContext.integrationCluster().deployService();
        return deployService.onLaunchApplication(app.typeId(),applicationId);
    }
    public boolean disableApplication(String applicationId){
        DataStore ds = this.tarantulaContext.masterDataStore();
        DeploymentDescriptor app = new DeploymentDescriptor();
        app.distributionKey(applicationId);
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
        this.integrationEventService = integrationCluster.publisher();
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
        this.publisher = this.tarantulaContext.integrationCluster().subscribe(NAME,(e)->{
            String tp = e.trackId();
            RecoverableListener listener = tMap.get(tp);
            if(listener!=null){
                listener.onUpdated(e.stub(),e.trackId(),e.index(),e.payload());
            }
            return false;
        });
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
    public String registerConfigurableListener(Descriptor category, Configurable.Listener listener){
        throw new UnsupportedOperationException("use string");
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
        if(configurable instanceof Connection){
            Connection connection = (Connection)configurable;
            this.integrationCluster.index(connection.configurationTypeId(),connection.toBinary());
            this.integrationCluster.deployService().onRegisterConnection(connection);
            return;
        }
        if(configurable instanceof Channel){
            ChannelStub channelStub = (ChannelStub)configurable;
            this.integrationCluster.index(channelStub.serverId,channelStub.toBinary());
            this.integrationCluster.deployService().onRegisterChannel(channelStub.configurationTypeId(),channelStub);
            return;
        }
        vMap.putIfAbsent(configurable.key().asString(),configurable);
        configurable.registered();
    }
    public void configure(String key){
        if(vMap.containsKey(key)){
            this.tarantulaContext.integrationCluster().deployService().onUpdateConfigurable(key);
        }
    }


    public <T extends OnAccess> boolean launchGameCluster(T gameCluster){
        DeployService deployService = this.tarantulaContext.integrationCluster().deployService();
        if(this.enableGameCluster(gameCluster.distributionKey())&&deployService.onStartGameService(gameCluster.distributionKey())){
            return tarantulaContext.integrationCluster().deployService().onLaunchGameCluster(gameCluster.distributionKey());
        }
        else{
            return false;
        }
    }
    public <T extends OnAccess> boolean shutdownGameCluster(T gameCluster){
        DeployService deployService = this.tarantulaContext.integrationCluster().deployService();
        if(this.disableGameCluster(gameCluster.distributionKey())){
            return deployService.onShutdownGameCluster(gameCluster.distributionKey());
        }
        else{
            return false;
        }
    }
    public <T extends OnAccess> T createGameCluster(String owner,String name,String mode,boolean tournamentEnabled){
        AccessIndex accessIndex = this.tarantulaContext.accessIndexService().set(name,0);//name+"-"+mode
        if(accessIndex==null){
            GameCluster gc = new GameCluster();
            gc.successful(false);
            gc.message("duplicated name ["+name+"]");
            return (T)gc;
        }
        String publishingId = accessIndex.distributionKey();
        GameCluster gameCluster = new GameCluster();
        try {
            DataStore mds = this.tarantulaContext.masterDataStore();
            gameCluster.property(GameCluster.NAME,name);
            gameCluster.property(GameCluster.MODE,mode);
            gameCluster.property(GameCluster.OWNER,owner);
            gameCluster.property(GameCluster.PUBLISHING_ID,publishingId);
            gameCluster.property(GameCluster.ACCESS_KEY,"pending access key");
            gameCluster.property(GameCluster.TIMESTAMP,0);
            gameCluster.property(GameCluster.TOURNAMENT_ENABLED,tournamentEnabled);
            gameCluster.property(GameCluster.DISABLED,true);
            mds.create(gameCluster);//create first and discharge if any errors on loop
            gameCluster.successful(true);
            XMLParser parser = new XMLParser();
            String typePrefix = name.toLowerCase();
            parser.parse(Thread.currentThread().getContextClassLoader().getResourceAsStream(tournamentEnabled?"tournament-game-cluster-basic-plan.xml":"game-cluster-basic-plan.xml"));
            for (LobbyConfiguration configuration : parser.configurations) {
                configuration.descriptor.typeId(configuration.descriptor.typeId().replace("game",typePrefix));//lower case only typeId
                Lobby _lobby = new DefaultLobby(configuration.descriptor);
                if(configuration.descriptor.typeId().endsWith("-lobby")){
                    gameCluster.property(GameCluster.GAME_LOBBY,configuration.descriptor.typeId());
                    gameCluster.gameLobby = _lobby;
                }
                else if(configuration.descriptor.typeId().endsWith("-service")){
                    gameCluster.property(GameCluster.GAME_SERVICE,configuration.descriptor.typeId());
                    this.tarantulaContext.availableServices().forEach((s)-> configuration.applications.add((DeploymentDescriptor)s));
                    gameCluster.serviceLobby = _lobby;
                }
                else if(configuration.descriptor.typeId().endsWith("-data")){
                    gameCluster.property(GameCluster.GAME_DATA,configuration.descriptor.typeId());
                    gameCluster.dataLobby  = _lobby;
                }
                LobbyTypeIdIndex lobbyTypeIdIndex = new LobbyTypeIdIndex(tarantulaContext.node().deploymentId(),configuration.descriptor.typeId());
                if(mds.load(lobbyTypeIdIndex)){//stop existed
                    throw new RuntimeException("["+name+"] duplicated");
                }
                ApplicationPreSetup[] preSetup = {null};
                Configuration setupConfig = this.tarantulaContext.configuration(configuration.descriptor.category()+"-pre-setup-settings");
                if(setupConfig != null){
                    String cname = (String) setupConfig.property(ApplicationPreSetup.SET_UP_NAME);
                    gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME,cname);
                    preSetup[0] = gameCluster.applicationPreSetup();
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
                    a.label(ApplicationProvider.LABEL);
                    a.onEdge(true);
                    a.tournamentEnabled(tournamentEnabled);
                    a.typeId(descriptor.typeId());//replaced with named type id
                    a.tag(a.tag().replaceFirst("game",typePrefix));
                    a.applicationClassName(tarantulaContext.singleModuleApplication);
                    mds.create(a);
                    _lobby.addEntry(a);
                    if(preSetup[0]!=null){
                        preSetup[0].setup(tarantulaContext,a,(String)gameCluster.property(GameCluster.MODE));
                    }
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
            this.integrationCluster.deployService().onCreateGameCluster(gameCluster.distributionKey());
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
    public <T extends OnAccess> T gameCluster(String key){
        GameCluster gc = new GameCluster();
        gc.distributionKey(key);
        gc.dataStore(this.tarantulaContext.masterDataStore());
        if(this.tarantulaContext.masterDataStore().load(gc)){
            gc.gameLobby = this.tarantulaContext.lobby((String) gc.property(GameCluster.GAME_LOBBY));
            gc.serviceLobby = this.tarantulaContext.lobby((String) gc.property(GameCluster.GAME_SERVICE));
            gc.dataLobby = this.tarantulaContext.lobby((String) gc.property(GameCluster.GAME_DATA));
            gc.setup(this.tarantulaContext);
            return (T)gc;
        }
        return null;
    }
    public Lobby lobby(String typeId){
        DataStore mds = this.tarantulaContext.masterDataStore();
        LobbyTypeIdIndex lobbyTypeIdIndex = new LobbyTypeIdIndex(this.tarantulaContext.node().deploymentId(),typeId);
        if(!mds.load(lobbyTypeIdIndex)) return null;
        LobbyDescriptor lb = new LobbyDescriptor();
        lb.distributionKey(lobbyTypeIdIndex.index());
        if(!mds.load(lb)) return null;
        Lobby lobby = new DefaultLobby(lb);
        List<DeploymentDescriptor> apps = this.tarantulaContext.masterDataStore().list(new ApplicationQuery(lb.distributionKey()));//this.tarantulaContext.queryFromDataMaster(PortableRegistry.OID,new ApplicationQuery(lb.distributionKey()),new String[]{lb.distributionKey()},true);
        apps.forEach((a)->{
            lobby.addEntry(a);
        });
        return lobby;
    }
    public String resetCode(String key){
        ClusterProvider icp = this.tarantulaContext.integrationCluster();
        String code = UUID.randomUUID().toString();
        icp.set(code.getBytes(),key.getBytes());
        return code;
    }
    public String checkCode(String resetCode){
        ClusterProvider icp = this.tarantulaContext.integrationCluster();
        byte[] ret = icp.remove(resetCode.getBytes());
        return (ret!=null?new String(ret):"");
    }

    public byte[] serverKey(String typeId){
        byte[] key = new byte[KEY_SIZE];
        secureRandom.nextBytes(key);
        return this.integrationCluster.createIfAbsent(typeId.getBytes(),key);
    }

    public void verifyConnection(String typeId,String serverId){
        this.integrationCluster.deployService().onVerifyConnection(typeId,serverId);
    }
    public String registerGameChannelListener(GameChannelListener gameChannelListener){
        String regKey = UUID.randomUUID().toString();
        cListeners.put(regKey,gameChannelListener);
        return regKey;
    }
    public void unregisterGameChannelListener(String registerKey){
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
    public AccessIndexService.AccessIndexStore accessIndexStore(){
        return new AccessIndexStoreViewer(this.tarantulaContext);
    }
    public void issueDataStoreBackup(){

    }
    public List<String> listDataStore(){
        return this.tarantulaContext.dataStoreProvider().list();
    }
    public DataStore.Summary validDataStore(String dataStore){
        DataStoreSummary summary = new DataStoreSummary();
        summary.name = dataStore;
        summary.partitionNumber = 0;
        summary.totalRecords = 0;
        if(!this.tarantulaContext.dataStoreProvider().existed(dataStore)) return summary;
        DataStore ds = this.tarantulaContext.dataStore(dataStore);
        summary.partitionNumber = ds.partitionNumber();
        summary.totalRecords = ds.count();
        summary.dataStore = ds;
        return summary;
    }
    public ClusterProvider.Summary clusterSummary(){
        return integrationCluster.summary();
    }

    public List<String> listClusterMember(){
        ArrayList<String> mlist = new ArrayList<>();
        return mlist;
    }
    public RecoverableListener registerRecoverableListener(String topic,RecoverableListener recoverableListener){
        tMap.put(topic,recoverableListener);
        return recoverableListener;
    }
    public void unregisterRecoverableListener(String topic){
        tMap.remove(topic);
    }
    public void atMidnight(){

    }
    public DistributionCallback distributionCallback(){
        return this.distributionCallback;
    }
    public PostOffice registerPostOffice(){
        return new PostOfficeSession();
    }


    public <T extends Configurable> void release(T configurable){
        if(configurable instanceof Connection){
            Connection connection = (Connection)configurable;
            this.integrationCluster.deployService().onReleaseConnection(connection);
            return;
        }
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
        lobbyDescriptor.distributionKey(query.index());
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
        lobbyDescriptor.distributionKey(query.index());
        if(!ds.load(lobbyDescriptor)||lobbyDescriptor.disabled()){
            return false;
        }
        lobbyDescriptor.disabled(true);
        ds.update(lobbyDescriptor);
        return true;
    }

    boolean enableGameCluster(String gameClusterId){
        GameCluster gameCluster = new GameCluster();
        gameCluster.distributionKey(gameClusterId);
        DataStore mds = this.tarantulaContext.masterDataStore();
        if(!mds.load(gameCluster)){
            return false;
        }
        String data = (String) gameCluster.property(GameCluster.GAME_DATA);//1
        String lobby = (String) gameCluster.property(GameCluster.GAME_LOBBY); //2
        String service = (String) gameCluster.property(GameCluster.GAME_SERVICE);;//3
        boolean suc1 = enableLobby(data);
        boolean suc2 = enableLobby(lobby);
        boolean suc3 = enableLobby(service);
        gameCluster.property(GameCluster.DISABLED,false);
        mds.update(gameCluster);
        return suc1&&suc2&&suc3;//make sure all enabled
    }
    boolean disableGameCluster(String gameClusterId){
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

    private class PostOfficeSession implements PostOffice{

        public OnTopic onTopic(){
            return (topic,data)->{
                TopicMapStoreSyncEvent event = new TopicMapStoreSyncEvent(NAME,topic,data.getFactoryId(),data.getClassId(),data.key().asString(),data.toBinary());
                publisher.publish(event);
            };
        }
        public OnSMS onSMS(){
            return ((emailAddress, data) ->Email.send(emailAddress,data));
        }
        public OnEmail onEmail(){
            return ((emailAddress, data) -> Email.send(emailAddress,data));
        }

        public OnTag onTag(String tag){
           return (dkey,t)->{
               String key = t.key().asString();
               byte[] payload = t.toBinary();
               RoutingKey routingKey = integrationEventService.routingKey(dkey,tag);
               MapStoreSyncEvent mapStoreSyncEvent = new MapStoreSyncEvent(routingKey.route(),t.owner(),t.getFactoryId(),t.getClassId(),key!=null?key:"",payload);
               integrationEventService.publish(mapStoreSyncEvent);
           };
        }
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
