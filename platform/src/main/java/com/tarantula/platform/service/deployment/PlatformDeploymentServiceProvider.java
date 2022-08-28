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

public class PlatformDeploymentServiceProvider implements DeploymentServiceProvider, DeploymentServiceProvider.DistributionCallback {

    private TarantulaLogger log = JDKLogger.getLogger(PlatformDeploymentServiceProvider.class);

    private EventService integrationEventService;
    private ClusterProvider integrationCluster;
    private SecureRandom secureRandom;

    private ConcurrentHashMap<String,TypedListener> oListeners = new ConcurrentHashMap<>();

    //callback on access index service
    private CopyOnWriteArrayList<AccessIndexService.Listener> aListeners = new CopyOnWriteArrayList<>();

    //on view, on lobby , configs mappings
    private ConcurrentHashMap<String,Configurable> vMap = new ConcurrentHashMap<>();

    //push event cache mappings
    private ConcurrentHashMap<String,GameChannelListener> cListeners = new ConcurrentHashMap<>();


    //module class loader mappings
    private ConcurrentHashMap<String,DynamicModuleClassLoader> cMap = new ConcurrentHashMap<>();

    //content cache ( web admin )
    private ConcurrentHashMap<String,Content> rMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String,ExposedGameService> eMap = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String,RecoverableListener> tMap = new ConcurrentHashMap<>();
    private EventService publisher;
    private TarantulaContext tarantulaContext;


    private String contentDir;

    private AtomicBoolean onAccessIndex;



    @Override
    public void start() throws Exception {
        this.secureRandom = new SecureRandom();
        this.onAccessIndex = new AtomicBoolean(true);
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
        DeployService deployService = this.tarantulaContext.integrationCluster().deployService();
        boolean suc = deployService.resetModule(descriptor);
        if(suc){
            this.integrationCluster.deployService().updateModule(descriptor);
        }
        return suc;
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

        cMap.computeIfPresent(descriptor.moduleId(),(k,c)->{
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
        DeployService deployService = this.tarantulaContext.integrationCluster().deployService();
        LobbyConfiguration a = xmlParser.configurations.get(0);
        AccessIndex publishId = this.tarantulaContext.accessIndexService().set(a.descriptor.typeId(),0);
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
            b.applicationClassName(this.tarantulaContext.singleModuleApplication);
            String x = deployService.addApplication(b,null,null);
            if(x==null){
                log.warn("Failed to add application ->"+b.toString());
            }
        });
        response.successful(true);
        response.message("module created");
        return response;
    }

    public boolean createApplication(Descriptor descriptor, String postSetup,String configName,boolean launching){
        DeployService deployService = this.tarantulaContext.integrationCluster().deployService();
        String  suc = deployService.addApplication(descriptor,postSetup,configName);
        if(suc!=null&&launching){//launch if lobby on line
            this.integrationCluster.deployService().launchApplication(descriptor.typeId(),suc);
        }
        return suc!=null;
    }
    public void addApplication(String typeId,String applicationId){
        this.tarantulaContext.setApplicationOnLobby(typeId,applicationId);
    }

    public boolean enableApplication(String applicationId){
        DeployService deployService = this.tarantulaContext.integrationCluster().deployService();
        String suc = deployService.enableApplication(applicationId);
        if(suc!=null){//return the lobby typeId
            deployService = this.tarantulaContext.integrationCluster().deployService();
            return deployService.launchApplication(suc,applicationId);
        }
        return suc!=null;
    }
    public boolean disableApplication(String applicationId){
        DeployService deployService = this.tarantulaContext.integrationCluster().deployService();
        String suc = deployService.disableApplication(applicationId);
        if(suc!=null){//return the lobby typeId
            deployService = this.tarantulaContext.integrationCluster().deployService();
            return deployService.shutdownApplication(suc,applicationId);
        }
        return suc!=null;
    }
    public void  removeApplication(String typeId,String applicationId){
        this.tarantulaContext.unsetApplication(typeId,applicationId,(d)->{
            if(d.type().equals(Descriptor.TYPE_LOBBY)){
                this.oListeners.forEach((k,ol)->{ //remove lobby entry
                    OnLobby onLobby = (OnLobby) vMap.get(d.typeId());
                    onLobby.closed(true);
                    if(onLobby.typeId().equals(ol.type)){
                        ol.listener.onUpdated(onLobby);
                    }//removed lobby entry
                });
                //rListeners.remove(d.tag()); //remove instance entry
                this.tarantulaContext.integrationCluster().deployService().disableLobby(d.typeId());
            }
            if(d.moduleName()!=null&&d.codebase()!=null){ //clean class loader if all apps removed on the class loader
                DynamicModuleClassLoader dynamicModuleClassLoader = cMap.remove(d.moduleId());
                if(dynamicModuleClassLoader!=null){
                    log.warn("Module resource clear on ["+d.codebase()+"/"+d.moduleArtifact()+"/"+d.moduleVersion()+"]");
                    dynamicModuleClassLoader._clear();
                }
            }
        });
    }
    public boolean launchModule(String typeId){
        DeployService deployService = this.tarantulaContext.integrationCluster().deployService();
        boolean suc = deployService.enableLobby(typeId);
        if(suc){
            this.integrationCluster.deployService().launchModule(typeId);
        }
        return suc;
    }
    public boolean shutdownModule(String typeId){
        DeployService deployService = this.tarantulaContext.integrationCluster().deployService();
        boolean suc = deployService.disableLobby(typeId);
        if(suc){
            this.integrationCluster.deployService().shutdownModule(typeId);
        }
        return suc;
    }
    public void removeLobby(String typeId){
        this.oListeners.forEach((k,ol)->{
            if(vMap.containsKey(typeId)){//skip system level modules
                OnLobby onLobby =(OnLobby) vMap.get(typeId);
                onLobby.closed(true);
                if(ol.type.equals(onLobby.configurationType())){
                    ol.listener.onUpdated(onLobby);
                }
            }
        });
        this.tarantulaContext.unsetLobby(typeId,(d)->{//clean up from runtime context
            //remove modules
            if(d.moduleName()!=null&&d.codebase()!=null){ //clean class loader if all apps removed on the class loader
                DynamicModuleClassLoader dynamicModuleClassLoader = cMap.remove(d.moduleId());
                if(dynamicModuleClassLoader!=null){
                    log.warn("Module resource clear on ["+d.codebase()+"/"+d.moduleArtifact()+"/"+d.moduleVersion()+"]");
                    dynamicModuleClassLoader._clear();
                }
            }
        });
    }
    public <T extends OnAccess> void addGameService(T gameCluster){
        if(!this.tarantulaContext.masterDataStore().load(gameCluster)){
            log.warn("No game cluster found ["+gameCluster.distributionKey()+"]");
            return;
        }
        this.tarantulaContext.setGameServiceProvider((GameCluster)gameCluster);
    }
    public <T extends OnAccess> void addGameCluster(T gameCluster){
        if(!this.tarantulaContext.masterDataStore().load(gameCluster)) return;
        this.tarantulaContext.setGameClusterOnLobby((GameCluster)gameCluster,new OnLobbyListener());
    }
    public <T extends OnAccess> void closeGameCluster(T gameCluster){
        if(!tarantulaContext.masterDataStore().load(gameCluster)){
            log.warn("No game cluster found ["+gameCluster.distributionKey()+"]");
            return;
        }
        this.tarantulaContext.releaseServiceProvider((String) gameCluster.property(GameCluster.GAME_SERVICE));
        removeLobby((String)gameCluster.property(GameCluster.GAME_DATA));
        removeLobby((String)gameCluster.property(GameCluster.GAME_LOBBY));
        removeLobby((String)gameCluster.property(GameCluster.GAME_SERVICE));
    }
    public void addLobby(String typeId){
        AccessIndex accessIndex = this.tarantulaContext.accessIndexService().get(typeId);
        this.tarantulaContext.setOnLobby(typeId,accessIndex.distributionKey(),new OnLobbyListener());
    }
    @Override
    public void setup(ServiceContext serviceContext){
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
        log.info("Platform deployment provider started");
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
        DeployService deployService = this.tarantulaContext.integrationCluster().deployService();
        boolean updated = deployService.addView(onView);
        if(updated){
            this.tarantulaContext.integrationCluster().deployService().updateView(onView);
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
        if(configurable instanceof Connection){
            Connection connection = (Connection)configurable;
            this.integrationCluster.index(connection.configurationTypeId(),connection.toBinary());
            this.integrationCluster.deployService().registerConnection(connection);
            return;
        }
        if(configurable instanceof Channel){
            ChannelStub channelStub = (ChannelStub)configurable;
            this.integrationCluster.index(channelStub.serverId,channelStub.toBinary());
            this.integrationCluster.deployService().registerChannel(channelStub.configurationTypeId(),channelStub);
            return;
        }
        vMap.putIfAbsent(configurable.key().asString(),configurable);
        configurable.registered();
    }
    public void configure(String key){
        if(vMap.containsKey(key)){
            this.tarantulaContext.integrationCluster().deployService().sync(key);
        }
    }


    public <T extends OnAccess> boolean launchGameCluster(T gameCluster){
        DeployService deployService = this.tarantulaContext.integrationCluster().deployService();
        if(deployService.enableGameCluster(gameCluster.distributionKey())&&deployService.startGameService(gameCluster.distributionKey())){
            return tarantulaContext.integrationCluster().deployService().launchGameCluster(gameCluster.distributionKey());
        }
        else{
            return false;
        }
    }
    public <T extends OnAccess> boolean shutdownGameCluster(T gameCluster){
        DeployService deployService = this.tarantulaContext.integrationCluster().deployService();
        if(deployService.disableGameCluster(gameCluster.distributionKey())){
            return this.tarantulaContext.integrationCluster().deployService().shutdownGameCluster(gameCluster.distributionKey());
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
        GameCluster gameCluster = this.tarantulaContext.integrationCluster().deployService().createGameCluster(owner,name,mode,tournamentEnabled,accessIndex.distributionKey());
        if(gameCluster.successful()){
            gameCluster.setup(tarantulaContext);
            oListeners.forEach((k,o)->
                    {
                        if(o.type.equals(GameCluster.GAME_CLUSTER_CONFIGURATION_TYPE)){
                            o.listener.onCreated(gameCluster);
                        }
                    }
            );
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
        LobbyTypeIdIndex lobbyTypeIdIndex = new LobbyTypeIdIndex(this.tarantulaContext.bucketId(),typeId);
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

    public void ping(String typeId,String serverId){
        this.integrationCluster.deployService().ping(typeId,serverId);
    }
    public String registerGameChannelListener(GameChannelListener gameChannelListener){
        String regKey = UUID.randomUUID().toString();
        cListeners.put(regKey,gameChannelListener);
        return regKey;
    }
    public void unregisterGameChannelListener(String registerKey){
        cListeners.remove(registerKey);
    }
    public boolean addChannel(String typeId,Channel channel){
        try{
            cListeners.forEach((k,v)->{
                if(v.typeId().equals(typeId)) v.onChannel(channel);
            });
            return true;
        }catch (Exception ex){
            log.error("error on add channel",ex);
            return false;
        }
    }
    public void addConnection(String typeId,Connection connection){
        cListeners.forEach((k,v)->{
            if(v.typeId().equals(typeId)) v.onConnection(connection);
        });
    }
    public void removeConnection(String typeId,Connection connection){
        cListeners.forEach((k,v)->{
            if(v.typeId().equals(typeId)) v.onDisConnection(connection);
        });
    }
    public void pingConnection(String typeId,String serverId){
        try{
            cListeners.forEach((k,v)->{
                if(v.typeId().equals(typeId)) v.onPing(serverId);
            });
        }catch (Exception ex){
            log.error("error on ping",ex);
        }
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
        return summary;
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
        return this;
    }
    public PostOffice registerPostOffice(){
        return new PostOfficeSession();
    }




    public <T extends Configurable> void release(T configurable){
        if(configurable instanceof Connection){
            Connection connection = (Connection)configurable;
            this.integrationCluster.deployService().releaseConnection(connection);
            return;
        }
        Configurable removed = this.vMap.remove(configurable.distributionKey());
        removed.released();
    }
    public void syncKey(String key){
        if(vMap.containsKey(key)){
            Configurable configurable = vMap.get(key);
            configurable.updated(new ServiceContextProxy(this.tarantulaContext));
        }
    }

    public void onUpdated(String mkey,double delta){

    }

    private class OnLobbyListener implements Configurable.Listener<OnLobby>{
        @Override
        public void onUpdated(OnLobby onLobby){
            register(onLobby);
        }
    }
    private class PostOfficeSession implements PostOffice{

        //public OnChannel onChannel(Session session){
            //PresenceIndex presenceIndex = (PresenceIndex) tarantulaContext.tokenValidatorProvider().presence(systemId);
            //return (label,data)->{
                //UDPEndpoint udp = (UDPEndpoint) tarantulaContext.serviceProvider(EndPoint.UDP_ENDPOINT);
                //Channel c = udp.channel(presenceIndex.sessionId());
                //c.write();
                //log.warn("sending message from->>>"+presenceIndex.sessionId());
            //};
        //}

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
