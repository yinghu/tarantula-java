package com.tarantula.platform.service.deployment;

import com.google.gson.GsonBuilder;
import com.tarantula.*;
import com.tarantula.Module;
import com.tarantula.logging.JDKLogger;
import com.tarantula.platform.*;
import com.tarantula.platform.event.*;
import com.tarantula.platform.presence.GameCluster;
import com.tarantula.platform.service.*;
import com.tarantula.platform.service.DeploymentServiceProvider;
import com.tarantula.platform.service.persistence.RecoverableMetadata;
import com.tarantula.platform.statistics.StatisticsIndex;
import com.tarantula.platform.util.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * updated by yinghu lu on 5/30/2020
 */
public class PlatformDeploymentServiceProvider implements DeploymentServiceProvider,EventListener,SchedulingTask{

    private TarantulaLogger log = JDKLogger.getLogger(PlatformDeploymentServiceProvider.class);

    private EventService integrationEventService;

    private String eventTopic = DEPLOY_TOPIC;
    private String localTopic;
    private String registerKey;

    private ConcurrentHashMap<String,InstanceRegistry.Listener> rListeners = new ConcurrentHashMap<>();
    private CopyOnWriteArrayList<OnLobby.Listener> oListeners = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<OnView.Listener> vListeners = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<Configuration.Listener> cListeners = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<Connection.Listener> wListeners = new CopyOnWriteArrayList<>();

    private ConcurrentHashMap<String,Recoverable> vMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String,Event> pushRegistry = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String,DynamicModuleClassLoader> cMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String,byte[]> rMap = new ConcurrentHashMap<>();
    private TarantulaContext tarantulaContext;
    private GsonBuilder builder;

    private Mode deploymentMode = Mode.ALL;
    private String contentTemDir;
    private String contentDir;
    private Metrics metrics;

    public Mode deploymentMode(){
        return deploymentMode;
    }

    @Override
    public void start() throws Exception {
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(ApplicationConfiguration.class,new ConfigurationDeserializer());
        this.builder.registerTypeAdapter(Connection.class,new ConnectionDeserializer());
        this.builder.registerTypeAdapter(ResponseHeader.class,new ResponseDeserializer());
        this.builder.registerTypeAdapter(ResponseHeader.class,new ResponseSerializer());
    }

    @Override
    public void shutdown() throws Exception {
        log.info("Platform deployment service provider shut down");
    }
    @Override
    public String name() {
        return DeploymentServiceProvider.NAME;
    }

    public <T extends OnAccess> T metrics(){
        return (T)metrics;
    }
    public String upload(InputStream inputStream,String fname) throws Exception{
        //save to local deploy/tem dir
        BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(this.contentTemDir+"/"+fname));
        int b;
        do{
            b = inputStream.read();
            if(b!=-1){
                fos.write(b);
            }
        }while (b!=-1);
        fos.flush();
        fos.close();
        this.tarantulaContext.schedule(new ContentReplicator(this,fname));
        ResponseHeader resp = new ResponseHeader("upload [",fname+"] saved successfully",true);
        return this.builder.create().toJson(resp);
    }
    void _pushContent(String fname){
        try{
            BufferedInputStream fin = new BufferedInputStream(new FileInputStream(this.contentTemDir+"/"+fname));
            byte[] ret = fin.readAllBytes();
            fin.close();
            OnUploadEvent onUploadEvent = new OnUploadEvent(this.eventTopic,this.localTopic,fname,ret);
            this.integrationEventService.publish(onUploadEvent);
        }catch (Exception ex){
            log.error("error on content push",ex);
        }
    }
    private void writeContent(OnUploadEvent onUploadEvent){
        try{
            //write to local deploy dir to be ready for deployment
            BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(this.tarantulaContext.deployDir+"/"+onUploadEvent.trackId()));
            fos.write(onUploadEvent.payload());
            fos.flush();
            fos.close();
        }catch (Exception ex){
            log.error("error on content write",ex);
        }
    }
    public Module module(Descriptor descriptor){
        if(descriptor.codebase()!=null){
            DynamicModuleClassLoader mc = cMap.computeIfAbsent(descriptor.subtypeId(),(k)-> {
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
    private byte[] fromModule(String name,String flag){
        String rid = flag.split("=")[1].trim();
        DynamicModuleClassLoader dc = cMap.get(rid);
        byte[][] ret = {new byte[0]};
        dc.loadResource(name,in -> {
            try{
                ret[0] = in.readAllBytes();
            }catch (Exception ex){
                log.warn("Resource ["+name+"] failed to load",ex);
            }
        });
        return ret[0];
    }
    private byte[] fromContext(String name){
        return rMap.computeIfAbsent(name,(rk)->{
                byte[] ret = new byte[0];
                BufferedInputStream cin=null;
                try {//read from deploy dir
                    cin = new BufferedInputStream(new FileInputStream(contentDir + "/" + name));
                    ret = cin.readAllBytes();
                    cin.close();
                }catch (Exception ex1){
                    //log.warn("Read resource ["+name+"] from backup");
                    try {if(cin!=null){cin.close();}}catch (Exception ex2){}
                    //read from backup
                    try{
                        cin = new BufferedInputStream(Thread.currentThread().getContextClassLoader().getResourceAsStream(name));
                        ret = cin.readAllBytes();
                    }catch (Exception ex3){
                        log.warn("Resource ["+name+"] not existed",ex3);
                    }
                    finally {
                        if(cin!=null){
                            try{cin.close();}catch (Exception ex4){}
                        }
                    }
                }
                return ret;
            }
        );
    }
    private void checkContent(OnView onView){
        try{
            //log.warn("CHECK VIEW->"+onView.toString());
            boolean isRoot = !onView.moduleResourceFile().contains("/");
            String rn = isRoot?onView.moduleResourceFile():(onView.moduleResourceFile().split("/")[1]);
            //log.warn("CHECK 1->"+rn);
            File f = new File(this.tarantulaContext.deployDir+"/"+rn);
            if(!f.exists()){
                return;
            }
            String x = isRoot?(contentDir+"/"+onView.contentBaseUrl()+"/"+onView.moduleResourceFile()):(contentDir+"/"+onView.moduleResourceFile());
            //log.warn("CHECK 2->"+x);
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
    public byte[] resource(String name,String flag){
        //log.warn("load resource ["+name+"] from ["+flag+"]");
        return flag==null?fromContext(name):fromModule(name,flag);
    }
    public void resource(Descriptor descriptor, String name, Module.OnResource onResource){
        DynamicModuleClassLoader dyn = cMap.get(descriptor.subtypeId());
        dyn.loadResource(name,onResource);
    }
    public boolean reset(Descriptor descriptor){
        //update app desc via subtypeId
        Lobby lobby = tarantulaContext.lobby(descriptor.typeId());
        boolean suc = this.tarantulaContext.tarantulaCluster().deployService().resetModule(lobby.descriptor().distributionKey(),descriptor);

        if(suc){
            this.integrationEventService.publish(new ModuleResetEvent(this.eventTopic,(DeploymentDescriptor) descriptor));
        }
        return suc;
    }
    private Module _internalModule(String mname){
        try{
            return (Module)Class.forName(mname).getConstructor().newInstance();
        }catch (Exception ex){
            log.error(mname,ex);
            throw new RuntimeException(ex);
        }
    }
    private void _reset(Descriptor descriptor){
        DynamicModuleClassLoader mc = cMap.computeIfPresent(descriptor.subtypeId(),(k,c)->{
            DynamicModuleClassLoader nmc = new DynamicModuleClassLoader(descriptor);
            nmc.proxies.addAll(c.proxies);
            c._clear();
            nmc._load();
            return nmc;
        });
        mc.proxies.forEach((mp)->{
            mp.reset();
        });
    }
    public boolean createModule(Descriptor descriptor){
        DynamicModuleClassLoader mc = new DynamicModuleClassLoader(descriptor);
        XMLParser xmlParser = new XMLParser();
        //ResponseHeader resp = new ResponseHeader("createModule");
        boolean[] suc = {true};
        mc.loadResource("descriptor.xml",(in)->{
            try{
                xmlParser.parse(in);
            }catch (Exception ex){
                log.warn("failed to parse descriptor.xml",ex);
                suc[0] = false;
            }
        });
        if(!suc[0]){
            return false;
        }
        DeployService deployService = this.tarantulaContext.tarantulaCluster().deployService();
        xmlParser.configurations.forEach((a)->{
            //ResponseHeader r = this.builder.create().fromJson(deployService.addLobby(a.descriptor),ResponseHeader.class);
            suc[0] = deployService.addLobby(a.descriptor);
            if(!suc[0]){
                return;
            }
            a.applications.forEach((b)->{
                b.codebase(descriptor.codebase());
                b.moduleArtifact(descriptor.moduleArtifact());
                b.moduleVersion(descriptor.moduleVersion());
                String x = deployService.addApplication(b);
                if(x==null){
                    log.warn("Failed to add application ->"+b.toString());
                }
            });
            a.views.forEach(v->{
                //add view to app
                v.owner(a.descriptor.typeId());
                boolean xv = deployService.addView(v);
                //log.warn(xv.message());
                if(!xv){
                    log.warn("Failed to add view ->"+v.toString());
                }
            });
        });
        return suc[0];//this.builder.create().toJson(resp);
    }
    public boolean createLobby(Descriptor descriptor){
        return this.tarantulaContext.tarantulaCluster().deployService().addLobby(descriptor);
    }
    public boolean createApplication(Descriptor descriptor){
        String  suc = this.tarantulaContext.tarantulaCluster().deployService().addApplication(descriptor);
        if(suc!=null){//launch if lobby on line
            this.integrationEventService.publish(new ModuleApplicationEvent(this.eventTopic,descriptor.typeId(),suc,false));
        }
        return suc!=null;
    }
    private void _setApplicationOnLobby(String typeId,String applicationId){
        this.tarantulaContext.setApplicationOnLobby(typeId,applicationId);
    }
    public boolean enableApplication(String applicationId,boolean enabled){
        String suc = this.tarantulaContext.tarantulaCluster().deployService().enableApplication(applicationId,enabled);
        if(suc!=null){
            this.integrationEventService.publish(new ModuleApplicationEvent(this.eventTopic,suc,applicationId,!enabled));
        }
        return suc!=null;
    }
    private void  _unsetApplicationOnLobby(String typeId,String applicationId){
        this.tarantulaContext.unsetApplication(typeId,applicationId,(d)->{
            if(d.singleton()&&d.category().equals("lobby")){
                this.oListeners.forEach((ol)->{ //remove lobby entry
                    OnLobby onLobby = (OnLobby) vMap.get(d.typeId());
                    onLobby.closed(true);
                    ol.onLobby(onLobby);//removed lobby entry
                });
                rListeners.remove(d.tag()); //remove instance entry
                this.tarantulaContext.tarantulaCluster().deployService().enableLobby(d.typeId(),false);
            }
            if(d.moduleName()!=null&&d.codebase()!=null){ //clean class loader if all apps removed on the class loader
                DynamicModuleClassLoader dynamicModuleClassLoader = cMap.remove(d.subtypeId());
                if(dynamicModuleClassLoader!=null){
                    log.warn("Module resource clear on ["+d.codebase()+"/"+d.moduleArtifact()+"/"+d.moduleVersion()+"/"+d.subtypeId()+"]");
                    dynamicModuleClassLoader._clear();
                }
            }
        });
    }
    public boolean launch(String typeId){
        boolean suc = this.tarantulaContext.tarantulaCluster().deployService().enableLobby(typeId,true);
        if(suc){
            this.integrationEventService.publish(new ModuleLaunchEvent(this.eventTopic,typeId));
        }
        return suc;
    }
    public boolean shutdown(String typeId){
        boolean suc = this.tarantulaContext.tarantulaCluster().deployService().enableLobby(typeId,false);
        if(suc){
            this.integrationEventService.publish(new ModuleShutdownEvent(this.eventTopic,typeId));
        }
        return suc;
    }
    private void _shutdown(String typeId){
        this.oListeners.forEach((ol)->{
            OnLobby onLobby =(OnLobby) vMap.get(typeId);
            onLobby.closed(true);
            ol.onLobby(onLobby);//removed lobby entry
        });
        this.tarantulaContext.unsetLobby(typeId,(d)->{//clean up from runtime context
            //remove modules
            if(d.singleton()){
                rListeners.remove(d.tag());
            }
            if(d.moduleName()!=null&&d.codebase()!=null){ //clean class loader if all apps removed on the class loader
                DynamicModuleClassLoader dynamicModuleClassLoader = cMap.remove(d.subtypeId());
                if(dynamicModuleClassLoader!=null){
                    log.warn("Module resource clear on ["+d.codebase()+"/"+d.moduleArtifact()+"/"+d.moduleVersion()+"/"+d.subtypeId()+"]");
                    dynamicModuleClassLoader._clear();
                    vMap.forEach((k,v)->{
                        if(v instanceof OnView){
                            OnView ov = (OnView)v;
                            if(ov.flag().equals(d.subtypeId())){
                                ov.disabled(true);
                                vMap.remove(k);
                                this.vListeners.forEach(listener -> listener.onView(ov));
                            }
                        }
                    });
                }
            }
        });
    }
    private void _launch(String typeId){
        this.tarantulaContext.setOnLobby(typeId,(ob)->{
            this.deploy(ob);
        });
    }
    public void clusterUpdated(int scope,String nodeId,boolean state){
        log.warn("Cluster updated->"+nodeId+"/"+state+"/"+scope);
        if(scope==Distributable.INTEGRATION_SCOPE&&(!state)){
            pushRegistry.forEach((k,v)->{
                ApplicationConfiguration ac = new ApplicationConfiguration();
                ac.oid(v.clientId());
                ac.bucket(v.bucket());
                Configuration c = (Configuration) vMap.get(ac.key().asString());
                //log.warn("Configuration ->"+c.tag()+"/"+c.disabled()+"/"+nodeId+"/"+v.owner());
                if(c.tag().equals(nodeId)){
                    vMap.remove(ac.key());
                    c.disabled(true);
                    this.cListeners.forEach((l)->{
                        l.onConfiguration(c);
                    });
                }
            });
        }
    }
    @Override
    public void setup(ServiceContext serviceContext){
        this.tarantulaContext = (TarantulaContext)serviceContext;
        this.deploymentMode = Mode.valueOf(this.tarantulaContext.deploymentMode);
        ClusterProvider ics = serviceContext.clusterProvider(Distributable.INTEGRATION_SCOPE);
        this.integrationEventService = ics.subscribe(eventTopic,this);
        localTopic = ics.subscription();
        registerKey = ics.addEventListener(null,this);
        try{
            contentTemDir = this.tarantulaContext.deployDir+"/tem";
            contentDir = this.tarantulaContext.deployDir+"/web";
            Path _path = Paths.get(this.tarantulaContext.deployDir);
            if(!Files.exists(_path)){
                Files.createDirectories(_path);
            }
            Path _web_root = Paths.get(this.tarantulaContext.deployDir+"/web/root");
            if(!Files.exists(_web_root)){
                Files.createDirectories(_web_root);
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
            Path _tem = Paths.get(contentTemDir);
            if(!Files.exists(_tem)){
                Files.createDirectories(_tem);
            }

        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
        log.info("Platform deployment provider started");
    }

    @Override
    public void waitForData() {
        this.integrationEventService.publish(new MapStoreVotingEvent(this.eventTopic,localTopic,registerKey,Distributable.INTEGRATION_SCOPE));
        DataStore mds = this.tarantulaContext.masterDataStore();
        this.metrics = new Metrics(this.tarantulaContext.dataBucketNode);
        this.metrics.property(Metrics.STATS_KEY,mds.bucket()+Recoverable.PATH_SEPARATOR+SystemUtil.oid());
        metrics.dataStore(mds);
        mds.createIfAbsent(metrics,true);
        StatisticsIndex statistics = new StatisticsIndex();
        statistics.distributionKey((String)metrics.property(Metrics.STATS_KEY));
        statistics.dataStore(mds);
        mds.createIfAbsent(statistics,true);
        this.metrics.statistics = statistics;
        this.metrics.property(Metrics.START_TIME,SystemUtil.toUTCMilliseconds(LocalDateTime.now()));
        this.metrics.update();
        this.tarantulaContext.integrationCluster().registerMetricsListener((k,v)->metrics.statistics.entry(k).update(v));
        this.tarantulaContext.tarantulaCluster().registerMetricsListener((k,v)->metrics.statistics.entry(k).update(v));
        this.tarantulaContext.schedule(this);
        log.info("Platform deployment service started on ["+localTopic+"/"+registerKey+"]");
    }


    @Override
    public boolean deploy(ServiceProvider serviceProvider) {
        return this.tarantulaContext.deployServiceProvider(serviceProvider);
    }
    public void release(ServiceProvider serviceProvider){
        this.tarantulaContext.releaseServiceProvider(serviceProvider.name());
    }
    @Override
    public boolean onEvent(Event event) {
       if(event instanceof MapStoreSyncEvent){
            //log.warn("Map Sync EVENT->"+event.source()+"/"+event.destination()+"/"+event.trackId());
            MapStoreSyncEvent mse = (MapStoreSyncEvent)event;
            Metadata mt = mse.metadata;
            RecoverableRegistry r = tarantulaContext.recoverableRegistry(mt.factoryId());
            Recoverable ot = r.create(mt.classId());
            if(ot.binary()){
                ot.fromByteArray(event.payload());
            }
            else{
                ot.fromMap(SystemUtil.toMap(event.payload()));
            }
            if(ot instanceof Configuration){
                Configuration ov = (Configuration) ot;
                vMap.put(new String(mse.key),ot);
                ov.distributionKey(new String(mse.key));
                this.cListeners.forEach((cl)->{
                    cl.onConfiguration(ov);
                });
            }
            else{
                log.warn("Not supported type->"+ot.toString());
            }
        }
        else if(event instanceof OnViewEvent){
           OnView onView = (OnView)((OnViewEvent) event).portable();
           checkContent(onView);
           vMap.putIfAbsent(onView.viewId(),onView);
           //remove caches
           rMap.remove(onView.moduleResourceFile());
           if(onView.moduleFile()!=null){
               rMap.remove(onView.moduleFile());
           }
           this.vListeners.forEach((cl)->{
               cl.onView(onView);
           });
        }
        else if(event instanceof OnUploadEvent){
            this.writeContent((OnUploadEvent)event);
        }
        else if(event instanceof MapStoreVotingEvent){
            if(!event.trackId().equals(registerKey)){
                log.warn("VOTING EVENT->"+event.source()+"/"+event.trackId());
                vMap.forEach((ks,v)->{
                    RecoverableMetadata mt = new RecoverableMetadata(v.getFactoryId(),v.getClassId());
                    byte[] k = ks.getBytes();//v.key().asString()!=null?v.key().asString().getBytes():"".getBytes();
                    MapStoreSyncEvent mse = new MapStoreSyncEvent(event.source(),event.trackId(),k,SystemUtil.toJson(v.toMap()),mt);
                    mse.trackId(event.trackId());
                    this.integrationEventService.publish(mse);
                });
                this.pushRegistry.forEach((k,v)->{
                    v.destination(event.source());
                    v.trackId(event.trackId());
                    this.integrationEventService.publish(v);
                });
            }
        }
        else if(event instanceof ServerPushEvent){
            Connection occ = this.builder.create().fromJson(new String(event.payload()), Connection.class);
            occ.disabled(false);
            pushRegistry.put(occ.key().asString(),event);//serverId cache
            this.wListeners.forEach((l)->{
                l.onState(occ);
            });
        }
        else if(event instanceof DisableServerPushEvent){
            Event pes = pushRegistry.remove(event.clientId());
            if(pes!=null){
                Connection occ = this.builder.create().fromJson(new String(pes.payload()), Connection.class);
                occ.disabled(true);
                this.wListeners.forEach((l)->{
                    l.onState(occ);
                });
            }
        }
        else if(event instanceof ModuleApplicationEvent){
            if(!event.disabled()){
                _setApplicationOnLobby(event.typeId(),event.applicationId());
            }
            else{
                _unsetApplicationOnLobby(event.typeId(),event.applicationId());
            }
        }
        else if(event instanceof ModuleResetEvent){
            ModuleResetEvent mse = (ModuleResetEvent)event;
            _reset((Descriptor)mse.portable());
        }
        else if(event instanceof ModuleLaunchEvent){
            _launch(event.typeId());
        }
        else if(event instanceof ModuleShutdownEvent){
            _shutdown(event.typeId());
        }
        else if(event instanceof GameClusterLaunchEvent){
            //log.warn("GAME CLUSTER launch-->"+event.trackId());
            GameCluster gameCluster = new GameCluster();
            gameCluster.distributionKey(event.trackId());
            this.tarantulaContext.masterDataStore().load(gameCluster);
            _launch((String)gameCluster.property(GameCluster.GAME_DATA));
            _launch((String)gameCluster.property(GameCluster.GAME_LOBBY));
            _launch((String)gameCluster.property(GameCluster.GAME_SERVICE));
        }
       else if(event instanceof GameClusterShutdownEvent){
           //log.warn("GAME CLUSTER shutdown-->"+event.trackId());
           GameCluster gameCluster = new GameCluster();
           gameCluster.distributionKey(event.trackId());
           this.tarantulaContext.masterDataStore().load(gameCluster);
           _shutdown((String)gameCluster.property(GameCluster.GAME_DATA));
           _shutdown((String)gameCluster.property(GameCluster.GAME_LOBBY));
           _shutdown((String)gameCluster.property(GameCluster.GAME_SERVICE));
       }
        return false;
    }
    public void registerInstanceRegistryListener(InstanceRegistry.Listener instanceRegistryListener){
        rListeners.put(instanceRegistryListener.onLobby(),instanceRegistryListener);
    }
    public OnView invalidView(){
        return (OnView)vMap.get("invalid.request");
    }
    public boolean deploy(OnView onView){
        if(!vMap.containsKey(onView.viewId())&&onView.distributionKey()==null){
            //create new entry
            boolean suc = tarantulaContext.tarantulaCluster().deployService().addView(onView);
            if(!suc){
                return false;
            }
        }
        //log.warn("VIEW->"+onView.distributionKey()+"<>"+onView.toString());
        OnViewEvent onViewEvent = new OnViewEvent(this.eventTopic,this.localTopic,onView);
        this.integrationEventService.publish(onViewEvent);
        return true;
    }
    public void registerOnViewListener(OnView.Listener onViewListener){
        vMap.forEach((k,v)->{
            if(v instanceof OnView){
                onViewListener.onView((OnView)v);
            }
        });
        vListeners.add(onViewListener);
    }

    public void deploy(InstanceRegistry registry){
        rListeners.forEach((k,l)->{
            if(l.onLobby().equals(registry.subtypeId())){
                try{l.onRegistry(registry);}catch (Exception ex){}//ignore ex
            }
        });
    }
    public void deploy(OnLobby onLobby){
        vMap.put(onLobby.typeId(),onLobby);
        if(onLobby.resetEnabled()){
            this.tarantulaContext.tokenValidatorProvider().onCheck(onLobby);
        }
        oListeners.forEach((o)->o.onLobby(onLobby));
    }
    public void registerOnLobbyListener(OnLobby.Listener onLobbyListener){
        oListeners.add(onLobbyListener);
    }

    public void registerOnConnectionListener(Connection.Listener listener){
        vMap.forEach((k,v)->{
            if(v instanceof Connection){
                listener.onState((Connection) v);
            }
        });
        wListeners.add(listener);
    }
    public void deploy(Configuration configuration){
        RecoverableMetadata mt = new RecoverableMetadata(configuration.getFactoryId(),configuration.getClassId());
        byte[] k = configuration.key().asString()!=null?configuration.key().asString().getBytes():"".getBytes();
        byte[] v = SystemUtil.toJson(configuration.toMap());
        //if(configuration.disabled()){
            //Configuration r = (Configuration) vMap.get(configuration.key().asString());
            //r.disabled(true);
            //v = SystemUtil.toJson(r.toMap());
        //}
        MapStoreSyncEvent mse = new MapStoreSyncEvent(this.eventTopic,localTopic,k, v,mt);
        this.integrationEventService.publish(mse);
    }
    public void registerConfigurationListener(Configuration.Listener listener){
        vMap.forEach((k,v)->{
            if(v instanceof Configuration){
                v.distributionKey(k);
                listener.onConfiguration((Configuration)v);
            }
        });
        this.cListeners.add(listener);
    }
    //dedicated server methods
    public void onUDPConnection(String typeId,Connection connection){
        this.tarantulaContext.integrationCluster().index(typeId,SystemUtil.toJson(connection.toMap()));
    }
    public Connection onUDPConnection(String typeId,Connection.StateListener listener){
        ClusterProvider icp = this.tarantulaContext.integrationCluster();
        byte[] ret = icp.firstIndex(typeId);
        if(ret==null){
            return null;
        }
        Connection connection = new UDPConnection();
        connection.fromMap(SystemUtil.toMap(ret));
        icp.set(connection.serverId().getBytes(),icp.subscription().getBytes());
        icp.addEventListener(connection.serverId(),(e)->{
            if(!e.closed()){
                listener.onUpdated(e.payload());
            }
            else{
                listener.onEnded(e.payload());
            }
            return e.closed();//removed on closed
        });
        return connection;
    }
    public void onStartedUDPConnection(String serverId,byte[] started){
        String _serverId = serverId+"_v";
        this.tarantulaContext.integrationCluster().set(_serverId.getBytes(),started);
    }
    public void onUpdatedUDPConnection(String serverId,byte[] updated){
        ClusterProvider icp = this.tarantulaContext.integrationCluster();
        byte[] sub = icp.get(serverId.getBytes());
        if (sub != null) {
            ConnectionStateEvent connectionStateEvent = new ConnectionStateEvent(new String(sub),serverId,false);
            connectionStateEvent.payload(updated);
            integrationEventService.publish(connectionStateEvent);
        }
        else{
            log.warn("Server connection not existed ->"+serverId);
        }
    }
    public byte[] onStartedUDPConnection(String serverId){
        String _serverId = serverId+"_v";
        return this.tarantulaContext.integrationCluster().remove(_serverId.getBytes());
    }
    public void onEndedUDPConnection(String serverId,byte[] ended) {
        ClusterProvider icp = this.tarantulaContext.integrationCluster();
        byte[] sub = icp.remove(serverId.getBytes());
        if (sub != null) {
            ConnectionStateEvent connectionStateEvent = new ConnectionStateEvent(new String(sub),serverId,true);
            connectionStateEvent.payload(ended);
            integrationEventService.publish(connectionStateEvent);
        }
        else{
            log.warn("Server connection not existed ->"+serverId);
        }
    }
    public void onEndedUDPConnection(String serverId){
        ClusterProvider icp = this.tarantulaContext.integrationCluster();
        icp.remove(serverId.getBytes());
    }
    //end of dedicated server methods
    public <T extends OnAccess> boolean launchGameCluster(T gameCluster){
        if(this.tarantulaContext.tarantulaCluster().deployService().enableGameCluster(gameCluster.distributionKey())){
            this.integrationEventService.publish(new GameClusterLaunchEvent(eventTopic,gameCluster.distributionKey()));
            return true;
        }
        else{
            return false;
        }
    }
    public <T extends OnAccess> boolean shutdownGameCluster(T gameCluster){
        if(this.tarantulaContext.tarantulaCluster().deployService().disableGameCluster(gameCluster.distributionKey())){
            this.integrationEventService.publish(new GameClusterShutdownEvent(eventTopic,gameCluster.distributionKey()));
            return true;
        }
        else{
            return false;
        }
    }
    public <T extends OnAccess> T createGameCluster(String owner,String name){
        return (T)this.tarantulaContext.tarantulaCluster().deployService().createGameCluster(owner,name);
    }
    public <T extends OnAccess> T gameCluster(String key){
        GameCluster gc = new GameCluster();
        gc.distributionKey(key);
        gc.dataStore(this.tarantulaContext.masterDataStore());
        if(this.tarantulaContext.masterDataStore().load(gc)){
            return (T)gc;
        }
        return null;
    }
    public Lobby lobby(String typeId){
        DataStore mds = this.tarantulaContext.masterDataStore();
        List<LobbyDescriptor> lbl = this.tarantulaContext.query(new String[]{mds.bucket(),typeId},new LobbyQuery(mds.bucket()));
        if(lbl.size()==0){
            return null;
        }
        LobbyDescriptor lb = lbl.get(0);
        Lobby lobby = new DefaultLobby(lb);
        List<DeploymentDescriptor> apps = this.tarantulaContext.query(new String[]{lb.distributionKey()},new ApplicationQuery(lb.distributionKey()));
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
    public void atMidnight(){
        //log.warn("MIDNIGHT->");
    }
    public PostOffice registerPostOffice(){
        return new PostOfficeSession();
    }

    @Override
    public boolean oneTime() {
        return false;
    }

    @Override
    public long initialDelay() {
        return this.tarantulaContext.metricsUpdateIntervalMinutes*1000*60;
    }

    @Override
    public long delay() {
        return this.tarantulaContext.metricsUpdateIntervalMinutes*1000*60;
    }

    @Override
    public void run() {
        metrics.update();
        metrics.statistics.summary((e)->e.update());
    }
    public void onUpdated(String key,double value){
        metrics.statistics.entry(key).update(value);
    }
    private class PostOfficeSession implements PostOffice{

        public OnConnection onConnection(String serverId){
            return (label,data)->{
                //lookup push event via serverId
                Event sc = pushRegistry.get(serverId);
                if(sc!=null){
                    sc.write(data,label);
                }
            };
        }

        public OnTopic onTopic(){
            return (label,data)-> pushRegistry.forEach((k,v)-> v.write(data,label));
        }
        public OnSMS onSMS(){
            return ((emailAddress, data) ->Email.send(emailAddress,data));
        }
        public OnEmail onEmail(){
            return ((emailAddress, data) ->Email.send(emailAddress,data));
        }

        public OnTag onTag(String tag){
           return (dkey,t)->{
               String key = t.key().asString();
               RecoverableMetadata m = new RecoverableMetadata(t.getFactoryId(),t.getClassId());
               byte[] payload = t.binary()?t.toByteArray(): SystemUtil.toJson(t.toMap());
               RoutingKey routingKey = integrationEventService.routingKey(dkey,tag);
               MapStoreSyncEvent mapStoreSyncEvent = new MapStoreSyncEvent(routingKey.route(),routingKey.source(),t.owner(),key!=null?key.getBytes():new byte[0],payload,m);
               integrationEventService.publish(mapStoreSyncEvent);
           };
        }
        public OnApplication onApplication(String applicationId){
            return (dkey,t)->{
                String key = t.key().asString();
                RecoverableMetadata m = new RecoverableMetadata(t.getFactoryId(),t.getClassId());
                byte[] payload = t.binary()?t.toByteArray(): SystemUtil.toJson(t.toMap());
                RoutingKey routingKey = integrationEventService.instanceRoutingKey(applicationId,dkey);
                MapStoreSyncEvent mapStoreSyncEvent = new MapStoreSyncEvent(routingKey.route(),routingKey.source(),t.owner(),key!=null?key.getBytes():new byte[0],payload,m);
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
        public void onJoin(Session session,OnUpdate onUpdate) throws Exception {
            this.module.onJoin(session,onUpdate);
        }

        @Override
        public boolean onRequest(Session session, byte[] payload, OnUpdate update) throws Exception {
            return this.module.onRequest(session,payload,update);
        }
        @Override
        public void onTimeout(Session session,OnUpdate onUpdate){
            this.module.onTimeout(session,onUpdate);
        }
        @Override
        public void onIdle(Session session,OnUpdate onUpdate){
            this.module.onIdle(session,onUpdate);
        }
        @Override
        public void setup(ApplicationContext context) throws Exception {
            this.applicationContext = context;
            _setup();
            this.module.setup(context);
        }
        @Override
        public String label(){
            return this.module.label();
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
            DynamicModuleClassLoader moduleClassLoader = cMap.get(descriptor.subtypeId());
            this.module = moduleClassLoader.newModule(descriptor.moduleName());
        }
        @Override
        public void onTimer(OnUpdate update){
            module.onTimer(update);
        }
        @Override
        public void onConnection(Connection connection){
            module.onConnection(connection);
        }
        public void reset(){
            try{
                DynamicModuleClassLoader moduleClassLoader = cMap.get(descriptor.subtypeId());
                this.clear();//clear on old instance
                this.module = moduleClassLoader.newModule(descriptor.moduleName());
                this.module.setup(applicationContext);//inject the limited content to prevent unexpected calls from modules
                if(descriptor.singleton()){
                    log.warn("Module ["+descriptor.moduleName()+"] reset on singleton instance ["+descriptor.tag()+"]");
                }else{
                    log.warn("Module ["+descriptor.moduleName()+"] reset on instance ["+applicationContext.onRegistry().distributionKey()+"]");
                }
            }catch (Exception ex){
                log.error("error on module reset, fix it and reset again",ex);
            }
        }
    }
}
