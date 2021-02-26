package com.tarantula.platform.service.deployment;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.icodesoftware.service.*;
import com.icodesoftware.util.TarantulaExecutorServiceFactory;
import com.tarantula.cci.udp.PendingServerPushMessage;
import com.tarantula.cci.udp.UDPSessionService;
import com.tarantula.cci.webhook.WebhookSessionService;
import com.tarantula.cci.websocket.WebSocketSessionService;
import com.icodesoftware.logging.JDKLogger;
import com.tarantula.platform.*;
import com.tarantula.platform.event.*;
import com.tarantula.platform.service.*;
import com.tarantula.platform.service.cluster.OneTimeRunner;
import com.tarantula.platform.service.cluster.PortableRegistry;
import com.tarantula.platform.util.*;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * updated by yinghu lu on 5/30/2020
 */
public class PlatformDeploymentServiceProvider implements DeploymentServiceProvider,SchedulingTask, DeploymentServiceProvider.DistributionCallback {

    private TarantulaLogger log = JDKLogger.getLogger(PlatformDeploymentServiceProvider.class);

    private EventService integrationEventService;
    private ClusterProvider integrationCluster;
    private SecureRandom secureRandom;

    private ConcurrentHashMap<String,InstanceRegistry.Listener> rListeners = new ConcurrentHashMap<>();
    private CopyOnWriteArrayList<OnLobby.Listener> oListeners = new CopyOnWriteArrayList<>();

    private CopyOnWriteArrayList<Connection.OnStateListener> wListeners = new CopyOnWriteArrayList<>();

    //callback on access index service
    private CopyOnWriteArrayList<AccessIndexService.Listener> aListeners = new CopyOnWriteArrayList<>();

    //on view, on lobby , configs mappings
    private ConcurrentHashMap<String,Configurable> vMap = new ConcurrentHashMap<>();

    //push event cache mappings
    private ConcurrentHashMap<String,ServerPushEvent> pushRegistry = new ConcurrentHashMap<>();

    //module class loader mappings
    private ConcurrentHashMap<String,DynamicModuleClassLoader> cMap = new ConcurrentHashMap<>();

    //content cache ( web admin )
    private ConcurrentHashMap<String,Content> rMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String,ExposedGameService> eMap = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String,RecoverableListener> tMap = new ConcurrentHashMap<>();
    private EventService publisher;
    private TarantulaContext tarantulaContext;
    private GsonBuilder builder;


    private String contentDir;

    private AtomicBoolean onAccessIndex;

    private ConcurrentLinkedDeque<PendingMessage> pendingData;
    private ConcurrentHashMap<String,Connection.OnConnectionListener> cCallbacks = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String,QueryCallbacks> qCallbacks = new ConcurrentHashMap<>();
    private ExecutorService udpPool;
    private int workSize;
    private long metricsFreshRate;
    private static long TIMER = 10000;


    @Override
    public void start() throws Exception {
        this.secureRandom = new SecureRandom();
        this.pendingData = new ConcurrentLinkedDeque<>();
        this.onAccessIndex = new AtomicBoolean(true);
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(Connection.class,new ConnectionDeserializer());
        this.builder.registerTypeAdapter(Connection.class,new ConnectionSerializer());
    }

    @Override
    public void shutdown() throws Exception {
        if(tarantulaContext.udpEndpointEnabled){
            udpPool.shutdown();
        }
        log.info("Platform deployment service provider shut down");
    }
    @Override
    public String name() {
        return DeploymentServiceProvider.NAME;
    }

    public <T extends OnAccess> T metrics(){
        return (T)new Metrics(this.tarantulaContext.metrics());
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
    private void checkContent(OnView onView){
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
        DeployService deployService = this.tarantulaContext.tarantulaCluster().deployService();
        boolean suc = deployService.resetModule(descriptor);
        if(suc){
            this.integrationCluster.deployService().updateModule(descriptor);
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
    public Response exportModule(Descriptor descriptor){
        DynamicModuleClassLoader mc = new DynamicModuleClassLoader(descriptor);
        Response response = new ResponseHeader();
        mc.loadResource("export.json",(in)->{
            try{
                JsonParser parser = new JsonParser();
                JsonObject jo = parser.parse(new InputStreamReader(in)).getAsJsonObject();
                String _moduleId = jo.get(ExposedGameService.MODULE_ID).getAsString();
                response.message(_moduleId);
                AccessIndex accessIndex = this.tarantulaContext.accessIndexService().setIfAbsent(_moduleId);
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
    public <T extends OnAccess> List<T> gameServiceList(){
        ArrayList<T> arrayList = new ArrayList<>();
        eMap.forEach((k,es)->{
            arrayList.add((T)es);
        });
        return arrayList;
    }
    public <T extends OnAccess> T gameService(String name){
        return (T)eMap.get(name);
    }
    public Response deployModule(String contextUrl,String resourceName){
        Response checked =  this.tarantulaContext.checkModule(contextUrl,resourceName);
        if(!checked.successful()){
            return checked;
        }
        boolean suc = this.tarantulaContext.integrationCluster().deployService().deployModule(contextUrl,resourceName);
        checked.successful(suc);
        checked.message(suc?"deployed->"+resourceName:"failed->"+resourceName);
        return checked;
    }
    public void updateModule(Descriptor descriptor){
        DynamicModuleClassLoader mc = cMap.computeIfPresent(descriptor.moduleId(),(k,c)->{
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
        DeployService deployService = this.tarantulaContext.tarantulaCluster().deployService();
        LobbyConfiguration a = xmlParser.configurations.get(0);
        AccessIndex publishId = this.tarantulaContext.accessIndexService().set(a.descriptor.typeId());
        if(publishId==null){
            response.successful(false);
            response.message("module already existed");
            return response;
        }
        if(!deployService.addLobby(a.descriptor,publishId.distributionKey())){
            response.successful(false);
            response.message("cannot create module");
            return response;
        }
        a.applications.forEach((b)->{
            b.codebase(descriptor.codebase());
            b.moduleArtifact(descriptor.moduleArtifact());
            b.moduleVersion(descriptor.moduleVersion());
            if(b.singleton()){
                b.applicationClassName(this.tarantulaContext.singleModuleApplication);
            }
            else{
                b.applicationClassName(this.tarantulaContext.moduleApplication);
            }
            String x = deployService.addApplication(b);
            if(x==null){
                log.warn("Failed to add application ->"+b.toString());
            }
        });
        response.successful(true);
        response.message("module created");
        return response;
    }

    public boolean createApplication(Descriptor descriptor, boolean launching){
        DeployService deployService = this.tarantulaContext.tarantulaCluster().deployService();
        String  suc = deployService.addApplication(descriptor);
        if(suc!=null&&launching){//launch if lobby on line
            this.integrationCluster.deployService().launchApplication(descriptor.typeId(),suc);
        }
        return suc!=null;
    }
    public void addApplication(String typeId,String applicationId){
        this.tarantulaContext.setApplicationOnLobby(typeId,applicationId);
    }

    public boolean enableApplication(String applicationId){
        DeployService deployService = this.tarantulaContext.tarantulaCluster().deployService();
        String suc = deployService.enableApplication(applicationId);
        if(suc!=null){//return the lobby typeId
            deployService = this.tarantulaContext.integrationCluster().deployService();
            return deployService.launchApplication(suc,applicationId);
        }
        return suc!=null;
    }
    public boolean disableApplication(String applicationId){
        DeployService deployService = this.tarantulaContext.tarantulaCluster().deployService();
        String suc = deployService.disableApplication(applicationId);
        if(suc!=null){//return the lobby typeId
            deployService = this.tarantulaContext.integrationCluster().deployService();
            return deployService.shutdownApplication(suc,applicationId);
        }
        return suc!=null;
    }
    public void  removeApplication(String typeId,String applicationId){
        this.tarantulaContext.unsetApplication(typeId,applicationId,(d)->{
            if(d.singleton()&&d.category().equals("lobby")){
                this.oListeners.forEach((ol)->{ //remove lobby entry
                    OnLobby onLobby = (OnLobby) vMap.get(d.typeId());
                    onLobby.closed(true);
                    ol.onLobby(onLobby);//removed lobby entry
                });
                rListeners.remove(d.tag()); //remove instance entry
                this.tarantulaContext.tarantulaCluster().deployService().disableLobby(d.typeId());
            }
            if(d.moduleName()!=null&&d.codebase()!=null){ //clean class loader if all apps removed on the class loader
                DynamicModuleClassLoader dynamicModuleClassLoader = cMap.remove(d.moduleId());
                if(dynamicModuleClassLoader!=null){
                    log.warn("Module resource clear on ["+d.codebase()+"/"+d.moduleArtifact()+"/"+d.moduleVersion()+"/"+d.subtypeId()+"]");
                    dynamicModuleClassLoader._clear();
                }
            }
        });
    }
    public boolean launchModule(String typeId){
        DeployService deployService = this.tarantulaContext.tarantulaCluster().deployService();
        boolean suc = deployService.enableLobby(typeId);
        if(suc){
            this.integrationCluster.deployService().launchModule(typeId);
        }
        return suc;
    }
    public boolean shutdownModule(String typeId){
        DeployService deployService = this.tarantulaContext.tarantulaCluster().deployService();
        boolean suc = deployService.disableLobby(typeId);
        if(suc){
            this.integrationCluster.deployService().shutdownModule(typeId);
        }
        return suc;
    }
    public void removeLobby(String typeId){
        this.oListeners.forEach((ol)->{
            if(vMap.containsKey(typeId)){//skip system level modules
                OnLobby onLobby =(OnLobby) vMap.get(typeId);
                onLobby.closed(true);
                ol.onLobby(onLobby);
            }
        });
        this.tarantulaContext.unsetLobby(typeId,(d)->{//clean up from runtime context
            //remove modules
            if(d.singleton()){
                rListeners.remove(d.tag());
            }
            if(d.moduleName()!=null&&d.codebase()!=null){ //clean class loader if all apps removed on the class loader
                DynamicModuleClassLoader dynamicModuleClassLoader = cMap.remove(d.moduleId());
                if(dynamicModuleClassLoader!=null){
                    log.warn("Module resource clear on ["+d.codebase()+"/"+d.moduleArtifact()+"/"+d.moduleVersion()+"/"+d.subtypeId()+"]");
                    dynamicModuleClassLoader._clear();
                }
            }
        });
    }
    public <T extends OnAccess> void addGameCluster(T gameCluster){
        byte[] key = gameCluster.distributionKey().getBytes();
        String memberId = this.tarantulaContext.integrationCluster().recoverService().findDataNode(this.tarantulaContext.dataStoreMaster,key);
        if(memberId==null){
            log.warn("No game cluster found ["+gameCluster.distributionKey()+"]");
            return;
        }
        byte[] ret = this.tarantulaContext.integrationCluster().recoverService().load(memberId,this.tarantulaContext.dataStoreMaster,key);
        gameCluster.fromBinary(ret);
        this.tarantulaContext.setGameClusterOnLobby(memberId,(GameCluster)gameCluster,(ob)->this.register(ob));
    }
    public <T extends OnAccess> void closeGameCluster(T gameCluster){
        byte[] key = gameCluster.distributionKey().getBytes();
        String memberId = this.tarantulaContext.integrationCluster().recoverService().findDataNode(this.tarantulaContext.dataStoreMaster,key);
        if(memberId==null){
            log.warn("No game cluster found ["+gameCluster.distributionKey()+"]");
            return;
        }
        byte[] ret = this.tarantulaContext.integrationCluster().recoverService().load(memberId,this.tarantulaContext.dataStoreMaster,key);
        gameCluster.fromBinary(ret);
        removeLobby((String)gameCluster.property(GameCluster.GAME_DATA));
        removeLobby((String)gameCluster.property(GameCluster.GAME_LOBBY));
        removeLobby((String)gameCluster.property(GameCluster.GAME_SERVICE));
    }
    public void addLobby(String typeId){
        AccessIndex accessIndex = this.tarantulaContext.accessIndexService().get(typeId);
        this.tarantulaContext.setOnLobby(typeId,accessIndex.distributionKey(),(ob)->this.register(ob));
    }
    @Override
    public void setup(ServiceContext serviceContext){
        this.tarantulaContext = (TarantulaContext)serviceContext;
        this.metricsFreshRate = this.tarantulaContext.metricsUpdateIntervalMinutes*1000*60;
        this.integrationCluster = serviceContext.clusterProvider(Distributable.INTEGRATION_SCOPE);
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
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
        log.info("Platform deployment provider started");
    }

    @Override
    public void waitForData() {
        this.tarantulaContext.schedule(this);
        this.tarantulaContext.tarantulaCluster().deployService().syncServerPushEvent();
        this.publisher = this.tarantulaContext.integrationCluster().subscribe(NAME,(e)->{
            String tp = e.trackId();
            RecoverableListener listener = tMap.get(tp);
            if(listener!=null){
                listener.onUpdated(e.stub(),e.trackId(),e.index(),e.payload());
            }
            return false;
        });
        if(this.tarantulaContext.udpEndpointEnabled){
            TarantulaExecutorServiceFactory.createExecutorService(this.tarantulaContext.udpReceiverThreadPoolSetting,(pool, psize, rh)->{
                this.udpPool = pool;
                this.workSize = psize;
            });
            for(int i=0;i<workSize;i++){
                udpPool.execute(()->{
                    while (true){
                        try{
                            PendingMessage pendingMessage = pendingData.poll();
                            if(pendingMessage!=null){
                                if(pendingMessage.outbound){
                                    PendingServerPushMessage pending = pendingMessage.pendingServerPushMessage;
                                    pending.ack();
                                    if(pending.retry()){
                                        pendingData.offer(pendingMessage);
                                    }
                                }
                                else{
                                    pendingMessage.runnable.run();
                                }
                            }
                            else{
                                Thread.sleep(50);
                            }
                        }catch (Exception ex){
                            ex.printStackTrace();
                        }
                    }
                });
            }
        }
        log.info("Platform deployment service started on ["+this.tarantulaContext.dataBucketNode+"/"+this.tarantulaContext.dataBucketGroup+"]");
    }
    public void memberRemoved(String memberId){
        //removed push event from the dead node
        this.pushRegistry.forEach((k,v)->{
            if(v.clientId().equals(memberId)){
                log.warn("Member removed->"+k);
                this.pushRegistry.remove(k);
            }
        });
    }
    public void memberAdded(String memberId){
        log.warn("Member added->"+memberId);
    }
    @Override
    public void register(ServiceProvider serviceProvider) {
        this.tarantulaContext.deployServiceProvider(serviceProvider);
    }
    public void release(ServiceProvider serviceProvider){
        this.tarantulaContext.releaseServiceProvider(serviceProvider.name());
    }

    public void registerInstanceRegistryListener(InstanceRegistry.Listener instanceRegistryListener){
        rListeners.put(instanceRegistryListener.onLobby(),instanceRegistryListener);
    }
    public void updateView(OnView onView){
        checkContent(onView);
        OnView removed = (OnView) vMap.remove(onView.viewId());
        if(removed!=null){
            rMap.remove(removed.moduleResourceFile());
        }
        rMap.remove(onView.moduleResourceFile());
        vMap.put(onView.viewId(),onView);
        //log.warn("View deployed->"+onView.toString());
    }
    public OnView onView(String viewId){
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
        boolean suc = this.tarantulaContext.integrationCluster().deployService().updateResource(contentUrl,resourceName);
        response.successful(suc);
        response.message(suc?"ok":"failed");
        return  response;
    }
    public void updateResource(String contentUrl,String resourceName){
        try{
            //content dir deployDir/web
            Path _path = Paths.get(this.contentDir+"/"+contentUrl);
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
                rMap.remove(contentUrl+"/"+resourceName);//clear cache
            }
        }catch (Exception ex){
            log.error(contentUrl+"/"+resourceName,ex);
        }
    }
    public void updateModule(String contentUrl,String resourceName){
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
                //rMap.remove(contentUrl+"/"+resourceName);//clear cache
            }
        }catch (Exception ex){
            log.error(contentUrl+"/"+resourceName,ex);
        }
    }
    public Response createView(OnView onView){
        Response response = this.tarantulaContext.checkResource(onView,"web");
        if(!response.successful()){
            return response;
        }
        DeployService deployService = this.tarantulaContext.tarantulaCluster().deployService();
        boolean updated = deployService.addView(onView);
        if(updated){
            this.tarantulaContext.integrationCluster().deployService().updateView(onView);
        }
        return new ResponseHeader("create/update view",updated?"view deployed->"+onView.moduleContext():"cannot create view",updated);
    }

    private void register(InstanceRegistry registry){
        rListeners.forEach((k,l)->{
            if(l.onLobby().equals(registry.subtypeId())){
                try{l.onRegistry(registry);}catch (Exception ex){}//ignore ex
            }
        });
    }
    private void register(OnLobby onLobby){
        if(onLobby.deployCode()<2){
            return;
        }
        vMap.put(onLobby.typeId(),onLobby);
        if(onLobby.resetEnabled()){
            this.tarantulaContext.tokenValidatorProvider().onCheck(onLobby);
        }
        oListeners.forEach((o)->o.onLobby(onLobby));
    }
    public void registerOnLobbyListener(OnLobby.Listener onLobbyListener){
        oListeners.add(onLobbyListener);
    }

    public void registerServerPushEvent(Event event){
        if(event instanceof ServerPushEvent){
            ServerPushEvent serverPushEvent = (ServerPushEvent)event;
            Connection occ = this.builder.create().fromJson(new String(serverPushEvent.payload()), Connection.class);
            occ.disabled(false);
            serverPushEvent.connection(occ);
            //log.warn("add server push->"+occ.connectionId()+"//"+occ.sequence()+"//"+occ.messageId()+"//"+occ.messageIdOffset()+"//"+serverPushEvent.payload().length);
            if(occ.server().type().equals(Connection.UDP)){
                try{
                    byte[] key = tarantulaContext.integrationCluster().get(occ.serverId().getBytes());
                    IvParameterSpec iv = new IvParameterSpec(key);
                    SecretKey secretKey = new SecretKeySpec(key,DeploymentServiceProvider.SERVER_KEY_SPEC);
                    Cipher encrypt = Cipher.getInstance(DeploymentServiceProvider.CIPHER_NAME_CBC_PKC5PADDING);
                    encrypt.init(Cipher.ENCRYPT_MODE,secretKey,iv);
                    Cipher decrypt = Cipher.getInstance(DeploymentServiceProvider.CIPHER_NAME_CBC_PKC5PADDING);
                    decrypt.init(Cipher.DECRYPT_MODE,secretKey,iv);
                    UDPSessionService udpSessionService = new UDPSessionService(occ.server(),pendingData,encrypt,decrypt);
                    udpSessionService.start();
                    serverPushEvent.eventService(udpSessionService);
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
            else if(occ.type().equals(Connection.WEB_SOCKET)){
                try{
                    AccessIndex _serverPush = this.tarantulaContext.accessIndexService().get("serverPush");
                    _serverPush.owner("serverPush");
                    WebSocketSessionService wss = new WebSocketSessionService(occ.server(),this.tarantulaContext.tokenValidatorProvider(),_serverPush);
                    wss.start();
                    serverPushEvent.eventService(wss);
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
            else if(occ.type().equals(Connection.WEB_HOOK)){
                serverPushEvent.eventService(new WebhookSessionService());
            }
            else{
                serverPushEvent.eventService(this.integrationEventService);
            }
            ///serverPushEvent.addConnection(occ);
            pushRegistry.put(occ.serverId(),serverPushEvent);//serverId cache
            this.wListeners.forEach((l) -> {
                if(l.typeId().equals(serverPushEvent.typeId())){
                    l.onState(occ);
                }
            });
        }
    }
    public void releaseServerPushEvent(String serverId){
        //log.warn("remove server push->"+serverId);
        ServerPushEvent pes = pushRegistry.remove(serverId);
        if(pes!=null){
            this.integrationCluster.remove(serverId.getBytes());//remove key
            this.integrationCluster.removeIndex(pes.typeId());
            Connection occ = this.builder.create().fromJson(new String(pes.payload()), Connection.class);
            occ.disabled(true);
            if(occ.type().equals(Connection.UDP)){
                try{pes.eventService().shutdown();}catch (Exception ex){}
            }
            this.wListeners.forEach((l)->{
                if(pes.typeId().equals(l.typeId())){
                    l.onState(occ);
                }
            });
        }
    }
    public void syncServerPushEvent(String memberId){
        this.tarantulaContext.schedule(new OneTimeRunner(1,()->{
            log.warn("push event distributing...");
            pushRegistry.forEach((k,v)->{
                tarantulaContext.tarantulaCluster().deployService().addServerPushEvent(memberId,v);
            });
        }));
    }
    public void ackServerPushEvent(String serverId){
        //log.warn("ack->"+serverId);
        ServerPushEvent serverPushEvent = pushRegistry.get(serverId);
        serverPushEvent.ack();
    }
    public void registerOnConnectionStateListener(Connection.OnStateListener listener){
        pushRegistry.forEach((k,v)->{
            Connection connection = this.builder.create().fromJson(new String(v.payload()), Connection.class);
            if(v.typeId().equals(listener.typeId())){
                listener.onState(connection);
            }
        });
        wListeners.add(listener);
    }
    public void registerOnConnectionListener(Connection.OnConnectionListener listener){
        this.cCallbacks.put(listener.lobbyTag(),listener);
    }
    public List<Configuration> configuration(){
        ArrayList<Configuration> clist = new ArrayList<>();
        vMap.forEach((k,v)->{
            if(v instanceof Configuration){
                clist.add((Configuration) v);
            }
        });
        return clist;
    }
    public void register(Configurable configurable){
        if(configurable instanceof OnView){
            updateView((OnView)configurable);
            return;
        }
        else if(configurable instanceof InstanceRegistry){
            register((InstanceRegistry)configurable);
            return;
        }
        else if(configurable instanceof OnLobby){
            register((OnLobby)configurable);
            return;
        }
        vMap.putIfAbsent(configurable.key().asString(),configurable);
    }
    public void configure(String key){
        if(vMap.containsKey(key)){
            this.tarantulaContext.integrationCluster().deployService().sync(key);
        }
    }
    //register/cache connection
    public Connection addConnection(String typeId,Connection connection){
        byte[] bytes = connection.toBinary();
        this.integrationCluster.index(typeId,bytes);
        //log.warn("add connection->["+ connection.connectionId()+"] on "+typeId);
        return connection;
    }
    public Connection addConnection(String serverId,int connectionId){
        ServerPushEvent serverPushEvent = pushRegistry.get(serverId);
        if(serverPushEvent==null){
            return null;
        }
        Connection client = serverPushEvent.connection();
        client.connectionId(connectionId);
        this.integrationCluster.index(serverPushEvent.typeId(),client.toBinary());
        //log.warn("add connection->["+ client.connectionId()+"] on "+serverPushEvent.typeId());
        return client;
    }
    //use connection
    public Connection onConnection(String typeId){
        ClusterProvider icp = this.tarantulaContext.integrationCluster();
        byte[] ret = icp.firstIndex(typeId);
        if(ret==null){
            return null;
        }
        Connection connection = new ClientConnection();
        connection.fromBinary(ret);
        return connection;
    }
    public void onRemoteConnection(Session session,Descriptor descriptor){
        this.integrationCluster.deployService().getConnection(descriptor.typeId(),descriptor.tag(),session);
    }
    public void getConnection(String lobbyTag,Session session){
        ((Event)session).eventService(this.integrationEventService);
        pendingData.offer(new PendingMessage(()-> cCallbacks.get(lobbyTag).onConnection(session)));
    }
    public <T extends OnAccess> boolean launchGameCluster(T gameCluster){
        DeployService deployService = this.tarantulaContext.tarantulaCluster().deployService();
        if(deployService.enableGameCluster(gameCluster.distributionKey())){
            return tarantulaContext.integrationCluster().deployService().launchGameCluster(gameCluster.distributionKey());
        }
        else{
            return false;
        }
    }
    public <T extends OnAccess> boolean shutdownGameCluster(T gameCluster){
        DeployService deployService = this.tarantulaContext.tarantulaCluster().deployService();
        if(deployService.disableGameCluster(gameCluster.distributionKey())){
            return this.tarantulaContext.integrationCluster().deployService().shutdownGameCluster(gameCluster.distributionKey());
        }
        else{
            return false;
        }
    }
    public <T extends OnAccess> T createGameCluster(String owner,String name,boolean tournamentEnabled){
        AccessIndex accessIndex = this.tarantulaContext.accessIndexService().set(name);
        if(accessIndex==null){
            GameCluster gc = new GameCluster();
            gc.successful(false);
            gc.message("duplicated name ["+name+"]");
            return (T)gc;
        }
        return this.tarantulaContext.tarantulaCluster().deployService().createGameCluster(owner,name,tournamentEnabled,accessIndex.distributionKey());
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
        //DataStore mds = this.tarantulaContext.masterDataStore();
        LobbyTypeIdIndex lobbyTypeIdIndex = new LobbyTypeIdIndex(this.tarantulaContext.bucketId(),typeId);
        byte[] v = this.tarantulaContext.tarantulaCluster().recoverService().load(null,tarantulaContext.dataStoreMaster,lobbyTypeIdIndex.key().asString().getBytes());
        if(v==null){
            return null;
        }
        lobbyTypeIdIndex.fromBinary(v);
        v = this.tarantulaContext.tarantulaCluster().recoverService().load(null,this.tarantulaContext.dataStoreMaster,lobbyTypeIdIndex.index().getBytes());
        if(v==null){
            return null;
        }
        LobbyDescriptor lb = new LobbyDescriptor();
        lb.fromBinary(v);
        lb.distributionKey(lobbyTypeIdIndex.index());
        Lobby lobby = new DefaultLobby(lb);
        List<DeploymentDescriptor> apps = this.tarantulaContext.queryFromDataMaster(PortableRegistry.OID,new ApplicationQuery(lb.distributionKey()),new String[]{lb.distributionKey()},true);
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
    public byte[] serverKey(Connection connection){
        byte[] key = this.tarantulaContext.integrationCluster().get(connection.serverId().getBytes());
        if(key!=null){
            return key;
        }
        key = new byte[KEY_SIZE];
        secureRandom.nextBytes(key);
        //connection.server().connectionId(integrationCluster.sequence());
        connection.connectionId(connection.server().connectionId());
        connection.server().sequence(secureRandom.nextInt());
        this.tarantulaContext.integrationCluster().set(connection.serverId().getBytes(),key);
        return key;
    }
    public void stopAccessIndex(){
        onAccessIndex.set(false);
        aListeners.forEach((a)->a.onStop());
    }
    public void startAccessIndex(){
        onAccessIndex.set(true);
        aListeners.forEach((a)->a.onStart());
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
    public void issueDataStoreBackup(){

    }
    public List<String> listDataStore(){
        return this.tarantulaContext.dataStoreProvider().list();
    }
    public boolean validDataStore(String dataStore){
        return this.tarantulaContext.dataStoreProvider().existed(dataStore);
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
        return this;
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
        return TIMER;
    }

    @Override
    public long delay() {
        return TIMER;
    }

    @Override
    public void run() {
        pushRegistry.forEach((k,e)->{
            if(!e.check()){
                log.warn("Server push event ack timeout->["+k+"]");
                this.tarantulaContext.schedule(new OneTimeRunner(1000,()->{
                    this.tarantulaContext.integrationCluster().deployService().removeServerPushEvent(k);
                }));
            }
        });
        metricsFreshRate -= TIMER;
        if(metricsFreshRate<=0){
            metricsFreshRate = this.tarantulaContext.metricsUpdateIntervalMinutes*1000*60;
            this.tarantulaContext.metrics().summary((e)->e.update());
        }
    }
    //metrics update call
    public void onUpdated(String key,double value){
        this.tarantulaContext.onUpdated(key,value);
    }

    public void release(Configurable configurable){
        this.vMap.remove(configurable.distributionKey());
    }
    public void syncKey(String key){
        if(vMap.containsKey(key)){
            Configurable configurable = vMap.get(key);
            configurable.update(new ServiceContextProxy(this.tarantulaContext));
        }
    }
    public String registerQueryCallback(RecoverService.QueryCallback queryCallback, RecoverService.QueryEndCallback queryEndCallback){
        String callId = UUID.randomUUID().toString();
        qCallbacks.put(callId,new QueryCallbacks(queryCallback,queryEndCallback));
        return callId;
    }
    public RecoverService.QueryCallback queryCallback(String source){
        return qCallbacks.get(source).queryCallback;
    }
    public RecoverService.QueryEndCallback queryEndCallback(String source){
        return qCallbacks.get(source).queryEndCallback;
    }
    public void removeQueryCallback(String callId){
        qCallbacks.remove(callId);
    }

    private class PostOfficeSession implements PostOffice{

        public OnConnection onConnection(Connection connection){
            return (label,data)->{
                //lookup push event via serverId
                ServerPushEvent sc = pushRegistry.get(connection.serverId());
                if(sc!=null){
                    sc.onMessage(data,label,connection);
                }
            };
        }

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
        public OnApplication onApplication(String applicationId){
            return (dkey,t)->{
                String key = t.key().asString();
                byte[] payload = t.toBinary();
                RoutingKey routingKey = integrationEventService.instanceRoutingKey(applicationId,dkey);
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
        public void onJoin(Session session,OnUpdate onUpdate) throws Exception {
            this.module.onJoin(session,onUpdate);
        }

        @Override
        public boolean onRequest(Session session, byte[] payload, OnUpdate update) throws Exception {
            return this.module.onRequest(session,payload,update);
        }
        @Override
        public void onTimeout(Session session, OnUpdate onUpdate){
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
            DynamicModuleClassLoader moduleClassLoader = cMap.get(descriptor.moduleId());
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
                DynamicModuleClassLoader moduleClassLoader = cMap.get(descriptor.moduleId());
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
