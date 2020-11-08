package com.tarantula.platform.service.deployment;

import com.google.gson.GsonBuilder;
import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.icodesoftware.protocol.InboundMessage;
import com.icodesoftware.service.*;
import com.icodesoftware.util.TarantulaExecutorServiceFactory;
import com.tarantula.cci.udp.UDPSessionService;
import com.tarantula.cci.webhook.WebhookSessionService;
import com.tarantula.cci.websocket.WebSocketSessionService;
import com.icodesoftware.logging.JDKLogger;
import com.tarantula.platform.*;
import com.tarantula.platform.event.*;
import com.tarantula.platform.service.*;
import com.tarantula.platform.service.cluster.OneTimeRunner;
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

    private CopyOnWriteArrayList<Connection.Listener> wListeners = new CopyOnWriteArrayList<>();

    //callback on access index service
    private CopyOnWriteArrayList<AccessIndexService.Listener> aListeners = new CopyOnWriteArrayList<>();

    //on view, on lobby , configs mappings
    private ConcurrentHashMap<String,Configurable> vMap = new ConcurrentHashMap<>();

    //push event cache mappings
    private ConcurrentHashMap<String,ServerPushEvent> pushRegistry = new ConcurrentHashMap<>();

    //module class loader mappings
    private ConcurrentHashMap<String,DynamicModuleClassLoader> cMap = new ConcurrentHashMap<>();

    //content cache ( web admin )
    private ConcurrentHashMap<String,byte[]> rMap = new ConcurrentHashMap<>();

    private TarantulaContext tarantulaContext;
    private GsonBuilder builder;

    private String contentTemDir;
    private String contentDir;

    private AtomicBoolean onAccessIndex;

    private ConcurrentLinkedDeque<InboundMessage> pendingData;
    //private ConcurrentHashMap<Long,Connection> connections;
    private ExecutorService udpPool;
    private int workSize;
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
            DynamicModuleClassLoader mc = cMap.computeIfAbsent(descriptor.typeId(),(k)-> {
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
        DynamicModuleClassLoader dyn = cMap.get(descriptor.typeId());
        dyn.loadResource(name,onResource);
    }
    public boolean resetModule(Descriptor descriptor){
        //update app desc via typeId
        DeployService deployService = this.tarantulaContext.tarantulaCluster().deployService();
        boolean suc = deployService.resetModule(descriptor);
        if(suc){
            deployService.updateModule(descriptor);
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
    public void updateModule(Descriptor descriptor){
        DynamicModuleClassLoader mc = cMap.computeIfPresent(descriptor.typeId(),(k,c)->{
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
            suc[0] = deployService.addLobby(a.descriptor);
            if(!suc[0]){
                return;
            }
            a.applications.forEach((b)->{
                b.codebase(descriptor.codebase());
                b.moduleArtifact(descriptor.moduleArtifact());
                b.moduleVersion(descriptor.moduleVersion());
                if(b.singleton()){
                    b.applicationClassName("com.tarantula.platform.module.SingletonModuleApplication");
                }
                else{
                    b.applicationClassName("com.tarantula.platform.module.DynamicModuleApplication");
                }
                String x = deployService.addApplication(b);
                if(x==null){
                    log.warn("Failed to add application ->"+b.toString());
                }
            });
            //skip view and configs
        });
        return suc[0];
    }

    public boolean createApplication(Descriptor descriptor, boolean launching){
        DeployService deployService = this.tarantulaContext.tarantulaCluster().deployService();
        String  suc = deployService.addApplication(descriptor);
        if(suc!=null&&launching){//launch if lobby on line
            deployService.launchApplication(descriptor.typeId(),suc);
        }
        return suc!=null;
    }
    public void addApplication(String typeId,String applicationId){
        this.tarantulaContext.setApplicationOnLobby(typeId,applicationId);
    }

    public boolean enableApplication(String applicationId,boolean enabled){
        DeployService deployService = this.tarantulaContext.tarantulaCluster().deployService();
        String suc = deployService.enableApplication(applicationId,enabled);
        if(suc!=null){//return the lobby typeId
            return enabled?deployService.launchApplication(suc,applicationId):deployService.shutdownApplication(suc,applicationId);
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
                this.tarantulaContext.tarantulaCluster().deployService().enableLobby(d.typeId(),false);
            }
            if(d.moduleName()!=null&&d.codebase()!=null){ //clean class loader if all apps removed on the class loader
                DynamicModuleClassLoader dynamicModuleClassLoader = cMap.remove(d.typeId());
                if(dynamicModuleClassLoader!=null){
                    log.warn("Module resource clear on ["+d.codebase()+"/"+d.moduleArtifact()+"/"+d.moduleVersion()+"/"+d.subtypeId()+"]");
                    dynamicModuleClassLoader._clear();
                }
            }
        });
    }
    public boolean launchModule(String typeId){
        DeployService deployService = this.tarantulaContext.tarantulaCluster().deployService();
        boolean suc = deployService.enableLobby(typeId,true);
        if(suc){
            deployService.launchModule(typeId);
        }
        return suc;
    }
    public boolean shutdownModule(String typeId){
        DeployService deployService = this.tarantulaContext.tarantulaCluster().deployService();
        boolean suc = deployService.enableLobby(typeId,false);
        if(suc){
            deployService.shutdownModule(typeId);
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
                DynamicModuleClassLoader dynamicModuleClassLoader = cMap.remove(d.typeId());
                if(dynamicModuleClassLoader!=null){
                    log.warn("Module resource clear on ["+d.codebase()+"/"+d.moduleArtifact()+"/"+d.moduleVersion()+"/"+d.subtypeId()+"]");
                    dynamicModuleClassLoader._clear();
                }
            }
        });
    }
    public void addLobby(String typeId){
        this.tarantulaContext.setOnLobby(typeId,(ob)->{
            this.register(ob);
        });
    }
    @Override
    public void setup(ServiceContext serviceContext){
        this.tarantulaContext = (TarantulaContext)serviceContext;
        this.integrationCluster = serviceContext.clusterProvider(Distributable.INTEGRATION_SCOPE);
        this.integrationEventService = integrationCluster.publisher();
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
        this.tarantulaContext.schedule(this);
        this.tarantulaContext.tarantulaCluster().deployService().syncServerPushEvent();
        if(this.tarantulaContext.udpEndpointEnabled){
            TarantulaExecutorServiceFactory.createExecutorService(this.tarantulaContext.udpReceiverThreadPoolSetting,(pool, psize, rh)->{
                this.udpPool = pool;
                this.workSize = psize;
            });
            for(int i=0;i<workSize;i++){
                udpPool.execute(()->{
                    while (true){
                        try{
                            InboundMessage pending = pendingData.poll();
                            if(pending!=null){
                                ServerPushEvent pushEvent = pushRegistry.get(pending.serverId);
                                pushEvent.onMessage(pending);
                            }
                            else{
                                Thread.sleep(100);
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
    private void update(OnView onView){
        checkContent(onView);
        vMap.putIfAbsent(onView.viewId(),onView);
        //remove caches
        rMap.remove(onView.moduleResourceFile());
        if(onView.moduleFile()!=null){
            rMap.remove(onView.moduleFile());
        }
    }
    public OnView onView(String viewId){
        return (OnView)vMap.get(viewId);
    }

    public boolean createView(OnView onView){
        DeployService deployService = this.tarantulaContext.tarantulaCluster().deployService();
        OnView _v = (OnView)vMap.get(onView.viewId());
        if(_v==null){
            return deployService.addView(onView);
        }
        else{
            return this.tarantulaContext.masterDataStore().update(onView);
        }
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
            log.warn("add server push->"+occ.connectionId()+"//"+occ.sequence());
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
            else if(occ.type().equals(Connection.WEB_HOOK)){
                serverPushEvent.eventService(new WebhookSessionService());
            }
            else if(occ.type().equals(Connection.WEB_SOCKET)){
                serverPushEvent.eventService(new WebSocketSessionService());
            }
            else{
                serverPushEvent.eventService(this.integrationEventService);
            }
            serverPushEvent.addConnection(occ);
            pushRegistry.put(occ.serverId(),serverPushEvent);//serverId cache
            this.wListeners.forEach((l) -> {
                if(l.typeId().equals(serverPushEvent.typeId())){
                    l.onState(occ);
                }
            });
        }
    }
    public void releaseServerPushEvent(String serverId){
        log.warn("remove server push->"+serverId);
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
        log.warn("ack serverid->"+serverId);
    }
    public void registerOnConnectionListener(Connection.Listener listener){
        pushRegistry.forEach((k,v)->{
            Connection connection = this.builder.create().fromJson(new String(v.payload()), Connection.class);
            if(v.typeId().equals(listener.typeId())){
                listener.onState(connection);
            }
        });
        wListeners.add(listener);
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
            update((OnView)configurable);
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
            this.tarantulaContext.tarantulaCluster().deployService().sync(key);
        }
    }
    //dedicated server methods
    public Connection onConnection(String typeId,Connection connection){
        this.tarantulaContext.integrationCluster().index(typeId,this.builder.create().toJson(connection).getBytes());
        return connection;
    }
    public Connection onConnection(String typeId,Connection.InboundMessageListener listener){
        ClusterProvider icp = this.tarantulaContext.integrationCluster();
        byte[] ret = icp.firstIndex(typeId);
        if(ret==null){
            return null;
        }
        Connection connection = this.builder.create().fromJson(new String(ret),Connection.class);
        connection.registerInboundMessageListener(listener);
        pushRegistry.get(connection.serverId()).addConnection(connection);
        return connection;
    }
    /**
    public void onStartedConnection(String serverId,byte[] started){
        String _serverId = serverId+"_v";
        this.tarantulaContext.integrationCluster().set(_serverId.getBytes(),started);
    }
    public void onUpdatedConnection(String serverId,byte[] updated){
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
    public byte[] onStartedConnection(String serverId){
        String _serverId = serverId+"_v";
        return this.tarantulaContext.integrationCluster().remove(_serverId.getBytes());
    }
    public void onEndedConnection(String serverId,byte[] ended) {
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
    public void onEndedConnection(String serverId){
        ClusterProvider icp = this.tarantulaContext.integrationCluster();
        icp.remove(serverId.getBytes());
    }**/
    //end of dedicated server methods
    public <T extends OnAccess> boolean launchGameCluster(T gameCluster){
        DeployService deployService = this.tarantulaContext.tarantulaCluster().deployService();
        if(deployService.enableGameCluster(gameCluster.distributionKey())){
            return deployService.launchGameCluster(gameCluster.distributionKey());
        }
        else{
            return false;
        }
    }
    public <T extends OnAccess> boolean shutdownGameCluster(T gameCluster){
        DeployService deployService = this.tarantulaContext.tarantulaCluster().deployService();
        if(deployService.disableGameCluster(gameCluster.distributionKey())){
            return deployService.shutdownGameCluster(gameCluster.distributionKey());
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
        //DataStore mds = this.tarantulaContext.masterDataStore();
        List<LobbyDescriptor> lbl = this.tarantulaContext.query(new String[]{this.tarantulaContext.bucketId(),typeId},new LobbyQuery(this.tarantulaContext.bucketId()));
        if(lbl.size()==0){
            return null;
        }
        LobbyDescriptor lb = lbl.get(0);
        Lobby lobby = new DefaultLobby(lb);
        List<DeploymentDescriptor> apps = this.tarantulaContext.query(new String[]{lb.distributionKey(),"all"},new ApplicationQuery(lb.distributionKey()));
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
        //SecretKey secretKey = new SecretKeySpec(key, SERVER_KEY_SPEC);
        connection.server().connectionId(integrationCluster.sequence());
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
        return this.tarantulaContext.metricsUpdateIntervalMinutes*1000*60;
    }

    @Override
    public long delay() {
        return this.tarantulaContext.metricsUpdateIntervalMinutes*1000*60;
    }

    @Override
    public void run() {
        this.tarantulaContext.metrics().summary((e)->e.update());
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
            return (label,data)-> pushRegistry.forEach((k,v)-> v.write(data,label));
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
               byte[] payload = SystemUtil.toJson(t.toMap());
               RoutingKey routingKey = integrationEventService.routingKey(dkey,tag);
               MapStoreSyncEvent mapStoreSyncEvent = new MapStoreSyncEvent(routingKey.route(),t.owner(),t.getFactoryId(),t.getClassId(),key!=null?key:"",payload);
               integrationEventService.publish(mapStoreSyncEvent);
           };
        }
        public OnApplication onApplication(String applicationId){
            return (dkey,t)->{
                String key = t.key().asString();
                byte[] payload = SystemUtil.toJson(t.toMap());
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
            DynamicModuleClassLoader moduleClassLoader = cMap.get(descriptor.typeId());
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
                DynamicModuleClassLoader moduleClassLoader = cMap.get(descriptor.typeId());
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
