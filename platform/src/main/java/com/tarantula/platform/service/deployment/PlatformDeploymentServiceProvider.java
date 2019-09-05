package com.tarantula.platform.service.deployment;

import com.google.gson.GsonBuilder;
import com.tarantula.*;
import com.tarantula.Module;
import com.tarantula.logging.JDKLogger;
import com.tarantula.platform.*;
import com.tarantula.platform.event.*;
import com.tarantula.platform.service.*;
import com.tarantula.platform.service.DeploymentServiceProvider;
import com.tarantula.platform.service.persistence.RecoverableMetadata;
import com.tarantula.platform.util.ConfigurationDeserializer;
import com.tarantula.platform.util.ResponseDeserializer;
import com.tarantula.platform.util.ResponseSerializer;
import com.tarantula.platform.util.SystemUtil;

import java.io.BufferedInputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * updated by yinghu lu on 6/19/2019.
 */
public class PlatformDeploymentServiceProvider implements DeploymentServiceProvider,EventListener{

    private TarantulaLogger log = JDKLogger.getLogger(PlatformDeploymentServiceProvider.class);

    private EventService integrationEventService;

    private String eventTopic = DEPLOY_TOPIC;
    private String localTopic;
    private String registerKey;

    private ConcurrentHashMap<String,InstanceRegistry.Listener> rListeners = new ConcurrentHashMap<>();
    private CopyOnWriteArrayList<OnLobby.Listener> oListeners = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<OnView.Listener> vListeners = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<Configuration.Listener> cListeners = new CopyOnWriteArrayList<>();

    private ConcurrentHashMap<String,Recoverable> vMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String,Event> pushRegistry = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String,DynamicModuleClassLoader> cMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String,byte[]> rMap = new ConcurrentHashMap<>();

    private TarantulaContext tarantulaContext;
    private GsonBuilder builder;
    @Override
    public void start() throws Exception {
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(ApplicationConfiguration.class,new ConfigurationDeserializer());
        this.builder.registerTypeAdapter(ResponseHeader.class,new ResponseDeserializer());
        this.builder.registerTypeAdapter(ResponseHeader.class,new ResponseSerializer());
    }

    @Override
    public void shutdown() throws Exception {
        log.info("deployment provider shut down");
    }
    @Override
    public String name() {
        return DeploymentServiceProvider.NAME;
    }
    public DataStoreProvider dataStoreProvider(){
        return this.tarantulaContext.dataStoreProvider();
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
    public byte[] resource(String name,String flag){
        if(flag!=null){
            log.warn("load resource ["+name+"] from ["+flag+"]");
            String rid = flag.split("=")[1].trim();
            DynamicModuleClassLoader dc = cMap.get(rid);
            byte[][] ret = {new byte[0]};
            dc.loadResource(name,in -> {
                try{
                    ret[0] = new byte[in.available()];
                    in.read(ret[0]);
                }catch (Exception ex){
                    log.warn("Resource ["+name+"] failed to load",ex);
                }
            });
            return ret[0];
        }
        return rMap.computeIfAbsent(name,(rk)->{
                byte[] ret = new byte[0];
                BufferedInputStream in = new BufferedInputStream(Thread.currentThread().getContextClassLoader().getResourceAsStream(name));
                try{
                    ret = new byte[in.available()];
                    in.read(ret);
                }catch (Exception ex){
                    log.warn("Resource ["+name+"] not existed",ex);
                }
                finally {
                    if(in!=null){
                        try{in.close();}catch (Exception ex){}
                    }
                }
                return ret;
            }
        );
    }
    public void resource(Descriptor descriptor, String name, Module.OnResource onResource){
        DynamicModuleClassLoader dyn = cMap.get(descriptor.subtypeId());
        dyn.loadResource(name,onResource);
    }
    public String reset(Descriptor descriptor){
        //update app desc via subtypeId
        Lobby lobby = tarantulaContext.lobby(descriptor.typeId());
        String suc = this.tarantulaContext.tarantulaCluster().deployService().resetModule(lobby.descriptor().distributionKey(),descriptor);
        ResponseHeader resp = this.builder.create().fromJson(suc,ResponseHeader.class);
        if(resp.successful()){
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
    public String createModule(Descriptor descriptor){
        DynamicModuleClassLoader mc = new DynamicModuleClassLoader(descriptor);
        XMLParser xmlParser = new XMLParser();
        ResponseHeader resp = new ResponseHeader("createModule");
        mc.loadResource("descriptor.xml",(in)->{
            try{
                xmlParser.parse(in);
            }catch (Exception ex){
                log.warn("failed to parse descriptor.xml",ex);
                resp.message("failed to parse descriptor.xml");
                resp.successful(false);
            }
        });
        if(!resp.successful()){
            return this.builder.create().toJson(resp);
        }
        DeployService deployService = this.tarantulaContext.tarantulaCluster().deployService();
        xmlParser.configurations.forEach((a)->{
            ResponseHeader r = this.builder.create().fromJson(deployService.addLobby(a.descriptor),ResponseHeader.class);
            if(!r.successful()){
                resp.successful(false);
                resp.message(r.message());
                return;
            }
            a.applications.forEach((b)->{
                b.codebase(descriptor.codebase());
                b.moduleArtifact(descriptor.moduleArtifact());
                b.moduleVersion(descriptor.moduleVersion());
                ResponseHeader x = this.builder.create().fromJson(deployService.addApplication(b),ResponseHeader.class);
                if(!x.successful()){
                    log.warn("Failed to add application ->"+b.toString());
                }
            });
            a.views.forEach(v->this.deploy(v));
        });
        return this.builder.create().toJson(resp);
    }
    public String createLobby(Descriptor descriptor){
        return this.tarantulaContext.tarantulaCluster().deployService().addLobby(descriptor);
    }
    public String createApplication(Descriptor descriptor){
        String resp = this.tarantulaContext.tarantulaCluster().deployService().addApplication(descriptor);
        ResponseHeader suc = this.builder.create().fromJson(resp,ResponseHeader.class);
        if(suc.successful()){//launch if lobby on line
            this.integrationEventService.publish(new ModuleApplicationEvent(this.eventTopic,descriptor.typeId(),(String)suc.toMap().get("applicationId"),false));
        }
        return resp;
    }
    private void _setApplicationOnLobby(String typeId,String applicationId){
        this.tarantulaContext.setApplicationOnLobby(typeId,applicationId);
    }
    public String enableApplication(String applicationId,boolean enabled){
        String suc = this.tarantulaContext.tarantulaCluster().deployService().enableApplication(applicationId,enabled);
        ResponseHeader resp = this.builder.create().fromJson(suc,ResponseHeader.class);
        this.integrationEventService.publish(new ModuleApplicationEvent(this.eventTopic,(String)resp.toMap().get("typeId"),applicationId,!enabled));
        return suc;
    }
    private void  _unsetApplicationOnLobby(String typeId,String applicationId){
        this.tarantulaContext.unsetApplication(typeId,applicationId,(d)->{
            if(d.singleton()&&d.category().equals("lobby")){
                this.oListeners.forEach((ol)->{ //remove lobby entry
                    ol.onLobby(new OnLobbyTrack(d.typeId(),true));//removed lobby entry
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
    public String launch(String typeId){
        String suc = this.tarantulaContext.tarantulaCluster().deployService().enableLobby(typeId,true);
        ResponseHeader resp = this.builder.create().fromJson(suc,ResponseHeader.class);
        if(resp.successful()){
            this.integrationEventService.publish(new ModuleLaunchEvent(this.eventTopic,typeId));
        }
        return suc;
    }
    public String shutdown(String typeId){
        String suc = this.tarantulaContext.tarantulaCluster().deployService().enableLobby(typeId,false);
        ResponseHeader resp = this.builder.create().fromJson(suc,ResponseHeader.class);
        if(resp.successful()){
            this.integrationEventService.publish(new ModuleShutdownEvent(this.eventTopic,typeId));
        }
        return suc;
    }
    private void _shutdown(String typeId){
        this.oListeners.forEach((ol)->{
            ol.onLobby(new OnLobbyTrack(typeId,true));//removed lobby entry
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
        ClusterProvider ics = serviceContext.clusterProvider(Distributable.INTEGRATION_SCOPE);
        this.integrationEventService = ics.subscribe(eventTopic,this);
        localTopic = ics.subscription();
        registerKey = ics.addEventListener(null,this);
    }

    @Override
    public void waitForData() {
        this.integrationEventService.publish(new MapStoreVotingEvent(this.eventTopic,localTopic,registerKey,Distributable.INTEGRATION_SCOPE));
        log.info("Platform deployment service started on ["+localTopic+"/"+registerKey+"]");
    }


    @Override
    public boolean deploy(ServiceProvider serviceProvider) {
        return this.tarantulaContext.deployServiceProvider(serviceProvider);
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
            if(ot instanceof OnView){
                this.vListeners.forEach((cl)->{
                    cl.onView((OnView) ot);
                });
            }
            else if(ot instanceof Configuration){
                this.cListeners.forEach((cl)->{
                    cl.onConfiguration((Configuration)ot);
                });
            }
            if(!ot.disabled()){
                vMap.put(new String(mse.key),ot);
                //log.warn(new String(mse.key)+" added");
            }
            else{
                vMap.remove(new String(mse.key));
                //log.warn(new String(mse.key)+" removed");
            }
        }
        else if(event instanceof MapStoreVotingEvent){
            if(!event.trackId().equals(registerKey)){
                //log.warn("VOTING EVENT->"+event.source()+"/"+event.trackId());
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
            //log.warn(event.toString()+" Updated");
            ApplicationConfiguration cfg = this.builder.create().fromJson(new String(event.payload()),ApplicationConfiguration.class);
            cfg.tag(event.owner());
            cfg.type("websocket");
            cfg.disabled(event.disabled());
            cfg.bucket(event.bucket());
            cfg.oid(cfg.property("serverId"));
            this.cListeners.forEach((l)->{
                l.onConfiguration(cfg);
            });
            if(!event.disabled()){
                pushRegistry.put(event.sessionId(),event);
                vMap.putIfAbsent(cfg.key().asString(),cfg);
            }else{
                pushRegistry.remove(event.sessionId());
                vMap.remove(cfg.key().asString());
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
        return false;
    }
    public void registerInstanceRegistryListener(InstanceRegistry.Listener instanceRegistryListener){
        rListeners.put(instanceRegistryListener.onLobby(),instanceRegistryListener);
    }
    public void deploy(OnView onView){
        RecoverableMetadata mt = new RecoverableMetadata(onView.getFactoryId(),onView.getClassId());
        byte[] k = onView.key().asString()!=null?onView.key().asString().getBytes():"".getBytes();
        MapStoreSyncEvent mse = new MapStoreSyncEvent(this.eventTopic,this.localTopic,k,SystemUtil.toJson(onView.toMap()),mt);
        this.integrationEventService.publish(mse);
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
        oListeners.forEach((o)->o.onLobby(onLobby));
    }
    public void registerOnLobbyListener(OnLobby.Listener onLobbyListener){
        oListeners.add(onLobbyListener);
    }

    public void deploy(Configuration configuration){
        RecoverableMetadata mt = new RecoverableMetadata(configuration.getFactoryId(),configuration.getClassId());
        byte[] k = configuration.key().asString()!=null?configuration.key().asString().getBytes():"".getBytes();
        byte[] v = SystemUtil.toJson(configuration.toMap());
        if(configuration.disabled()){
            Configuration r = (Configuration) vMap.get(configuration.key().asString());
            r.disabled(true);
            v = SystemUtil.toJson(r.toMap());
        }
        MapStoreSyncEvent mse = new MapStoreSyncEvent(this.eventTopic,localTopic,k, v,mt);
        this.integrationEventService.publish(mse);
    }
    public void registerConfigurationListener(Configuration.Listener listener){
        vMap.forEach((k,v)->{
            if(v instanceof Configuration){
                listener.onConfiguration((Configuration)v);
            }
        });
        this.cListeners.add(listener);
    }
    public PostOffice registerPostOffice(){
        return new PostOfficeSession();
    }
    private class PostOfficeSession implements PostOffice{

        public OnLabel onLabel(){
            return (label,data)-> pushRegistry.forEach((k,v)-> v.write(data,label));
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
        public void onJoin(Session session) throws Exception {
            this.module.onJoin(session);
        }

        @Override
        public boolean onRequest(Session session, byte[] payload, OnUpdate update) throws Exception {
            return this.module.onRequest(session,payload,update);
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
