package com.tarantula.platform;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hazelcast.config.ClasspathXmlConfig;
import com.hazelcast.config.Config;
import com.icodesoftware.*;
import com.icodesoftware.service.*;
import com.icodesoftware.service.Metrics;
import com.icodesoftware.util.HttpCaller;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.TarantulaExecutorServiceFactory;
import com.icodesoftware.logging.JDKLogger;
import com.tarantula.cci.udp.UDPEndpoint;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.item.ConfigurableTemplate;
import com.tarantula.platform.item.JsonConfigurableTemplateParser;
import com.tarantula.platform.service.*;
import com.tarantula.platform.bootstrap.ServiceBootstrap;
import com.tarantula.platform.service.cluster.*;
import com.tarantula.platform.service.deployment.*;

import com.tarantula.platform.service.metrics.JVMMonitor;
import com.tarantula.platform.service.metrics.MetricsManager;
import com.tarantula.platform.service.persistence.DataStoreConfigurationJsonParser;
import com.tarantula.platform.service.persistence.ClusterNode;
import com.tarantula.platform.service.persistence.MirrorClusterBackupProvider;
import com.tarantula.platform.util.*;


public class TarantulaContext implements Serviceable, ServiceContext {


    private static TarantulaLogger log = JDKLogger.getLogger(TarantulaContext.class);
	
	private static final TarantulaContext BC = new TarantulaContext();

	public static  CountDownLatch _storageInstanceStarted ;
 	
 	public static  CountDownLatch _integrationClusterStarted ;
 	
 	public static  CountDownLatch _tarantulaApplicationStarted ;

    public static  CountDownLatch _tarantulaInstanceStarted;

    public static  CountDownLatch _accessIndexServiceStarted ;

    public static  CountDownLatch _storageStarted;

    public static  CountDownLatch _deployServiceStarted;

    public static CountDownLatch _systemServiceStarted;


    public AtomicBoolean node_started;

    public static CountDownLatch _access_index_syc_finished;

    private static final String CONFIG_INTEGRATION = "hazelcast-integration.xml";

    private IntegrationCluster integrationCluster;
	
	private final EndpointService endpointService;

    private final ConcurrentHashMap<String,DefaultLobby> _lobbyMapping = new ConcurrentHashMap<>();


    private final List<DefaultLobby> mlobbyList = new LinkedList();

    private final ConcurrentHashMap<String, ApplicationProvider> availableApplicationManagers = new ConcurrentHashMap<>();

    public static String releaseVersion;
    public String applicationSchedulingPoolSetting;
    private ScheduledExecutorService scheduledExecutorService;

    public String tarantulaServerValidator;
    public String tarantulaDeploymentProvider;
    public int maxActiveSessionNumber;
    public int tokenTimeout;
    public int ticketTimeout;

    private TokenValidatorProvider tokenValidatorProvider;
    private DeploymentServiceProvider deploymentServiceProvider;
    public String deployDir;

    public String servicePushAddress;
    public String singleModuleApplication;

    public  String eventThreadPoolSetting;

    public int retries; //event retries
    public long retryInterval; //event retry interval time
    public int recoverBatchSize = 10;

    public int operationRetries;
    public long operationRejectInterval;


    private final ConcurrentHashMap<String,ServiceProvider> serviceProviders = new ConcurrentHashMap();

    public DataStoreProvider deploymentDataStoreProvider;

    private final ConcurrentHashMap<Integer,RecoverableListener> fMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String,ConfigurableTemplate> cMap = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String,GameCluster> gMap = new ConcurrentHashMap<>();

    private final MetricsManager metricsManager;

    public String dataBucketGroup;
    public String dataBucketNode;
    private ClusterNode node;

    public String dataStoreDir;

    public boolean dataStoreDailyBackup;

    public int maxIdlesOnInstance;
    public long timeoutOnInstance;


    public String clusterNameSuffix;
    public int clusterInitialSize;
    public int clusterMaxSize;
    public int platformRoutingNumber;
    public int accessIndexRoutingNumber;

    public static ScopedMemberDiscovery memberDiscovery;
    public static int operationTimeout = 5;
    public static boolean lobbySubscriptionEnabled = false;

    public String authContext = "localhost";

    public ConcurrentHashMap<String,CountDownLatch> _syncLatch = new ConcurrentHashMap<>();

    public boolean runAsMirror;
    public boolean backupEnabled;
    public String backupUrl;
    private static String deploymentIdPath = "backup/deployment";
    public String backupAccessKey;

    private MirrorClusterBackupProvider mirrorBackupProvider;

    public List<String> serviceViewList = new ArrayList<>();
    private PostOfficeSession postOfficeSession;


    private HttpClientProvider httpClientProvider;

    public boolean tarantulaServiceEventLogPersistenceEnable;
    private String serviceEventLogStore = "tarantula_service_event_log";
    //private int maxPendingEventSize = 10;

    private ServiceEventLogger serviceEventLogger;

 	private TarantulaContext(){
         this.endpointService = new EndpointService(this);
 	     this.metricsManager = new MetricsManager(this);
    }

	public static TarantulaContext getInstance(){
 	    return BC;
	}

	public void start() throws Exception {
 	    if(this.dataBucketNode.length() != 3) throw new RuntimeException("Node name must be 3 letters");
         this.scheduledExecutorService = TarantulaExecutorServiceFactory.createScheduledExecutorService(this.applicationSchedulingPoolSetting);
 	     _storageInstanceStarted = new CountDownLatch(1);
         _integrationClusterStarted = new CountDownLatch(1);
         _tarantulaApplicationStarted = new CountDownLatch(1);
        _tarantulaInstanceStarted = new CountDownLatch(1);
        _accessIndexServiceStarted = new CountDownLatch(2);
        _storageStarted = new CountDownLatch(1);
        _deployServiceStarted = new CountDownLatch(1);
        _systemServiceStarted = new CountDownLatch(1);
        _access_index_syc_finished = new CountDownLatch(2);
        this.httpClientProvider = new HttpCaller();
        this.httpClientProvider.start();
        this.node = new ClusterNode(this.dataBucketGroup,this.dataBucketNode,this.platformRoutingNumber);
        this.node.clusterNameSuffix = this.clusterNameSuffix;
        this.node.deployDirectory = this.deployDir;
        this.node.servicePushAddress = this.servicePushAddress;
        this.node.runAsMirror = this.runAsMirror;
        this.node.backupEnabled = this.backupEnabled;
        this.node.dailyBackupEnabled = this.dataStoreDailyBackup;
        this.node.dataStoreDirectory = this.dataStoreDir;
        if(backupEnabled){//using backup deployment id
            String resp = this.httpClientProvider.get(this.backupUrl,deploymentIdPath,new String[]{Session.TARANTULA_ACCESS_KEY,this.backupAccessKey});
            JsonObject json = JsonUtil.parse(resp);
            if(!json.get("successful").getAsBoolean()) throw new RuntimeException("failed to fetch remote deployment id");
            node.deploymentId = json.get("message").getAsString();
            log.warn("Using backup deployment id ["+node.deploymentId+"]");
        }
        node_started = new AtomicBoolean(false);
        PortableProviderConfigurationParser pcs = new PortableProviderConfigurationParser("tarantula-platform-portable-provider.xml");
        pcs.parse().forEach((r)->{
            fMap.put(r.registryId(),r);
        });
        DataStoreConfigurationJsonParser sparser = new DataStoreConfigurationJsonParser("tarantula-platform-data-store-config.json",this,dataStoreProvider -> {
            try{
                this.deploymentDataStoreProvider = dataStoreProvider;
                this.deploymentDataStoreProvider.start();
                this.deploymentDataStoreProvider.setup(this);
                this._initMirrorClusterBackup();
                log.warn("Tarantula data store provider started");
            }catch (Exception ex){
                throw new RuntimeException(ex);
            }
        });
        new ServiceBootstrap(new CountDownLatch(0),_storageInstanceStarted,sparser,"system-data-store-parser",true).start();
        Config gcfg = new ClasspathXmlConfig(Thread.currentThread().getContextClassLoader(),CONFIG_INTEGRATION);
        gcfg.getProperties().setProperty("hazelcast.partition.count",""+accessIndexRoutingNumber);
        gcfg.getProperties().setProperty("hazelcast.initial.min.cluster.size",""+clusterInitialSize);
        gcfg.getGroupConfig().setName("tarantula-integration-"+this.clusterNameSuffix);
        this.integrationCluster = new IntegrationCluster(gcfg,this.dataBucketGroup,this);
        new ServiceBootstrap(_storageInstanceStarted,_integrationClusterStarted,this.integrationCluster,"integration-cluster",true).start(); //integration cluster start
        new ServiceBootstrap(_accessIndexServiceStarted, _tarantulaApplicationStarted, new TarantulaApplicationDeployer(this),"application-deployer",true).start();
        this.tokenValidatorProvider = (TokenValidatorProvider)Class.forName(this.tarantulaServerValidator).getConstructor().newInstance();
        this.tokenValidatorProvider.timeout(this.tokenTimeout,this.ticketTimeout);
        this.tokenValidatorProvider.start();
        this.serviceProviders.put(TokenValidatorProvider.NAME,tokenValidatorProvider);
        this.deploymentServiceProvider = (DeploymentServiceProvider) Class.forName(this.tarantulaDeploymentProvider).getConstructor().newInstance();
        this.deploymentServiceProvider.start();
        this.serviceProviders.put(DeploymentServiceProvider.NAME,this.deploymentServiceProvider);
        new ServiceBootstrap(_tarantulaApplicationStarted,null,this.endpointService,"endPointService",true).start();
	}
	public void shutdown() throws Exception {
        metricsManager.shutdown();
        this.scheduledExecutorService.shutdown();
        this.endpointService.shutdown();
        this.integrationCluster.shutdown();
        serviceProviders.remove(DataStoreProvider.NAME);
        for(ServiceProvider ds : serviceProviders.values()){
            ds.shutdown();
        }
        this.deploymentDataStoreProvider.shutdown();
    }

	public ScheduledFuture<?> schedule(SchedulingTask task){
        if(task.oneTime()){
            return this.scheduledExecutorService.schedule(task,task.initialDelay()+task.delay(),TimeUnit.MILLISECONDS);
        }else{
            return this.scheduledExecutorService.scheduleAtFixedRate(task,task.initialDelay(),task.delay(),TimeUnit.MILLISECONDS);
        }
    }


    public void _initMirrorClusterBackup(){
 	    this.mirrorBackupProvider = new MirrorClusterBackupProvider(this.deploymentDataStoreProvider);
 	    Configuration config = this.configuration("mirror-backup-provider-settings");
 	    Map<String,Object> map = new HashMap<>();
 	    config.properties().forEach(p-> map.put(p.name(),p.value()));
 	    this.mirrorBackupProvider.configure(map);
 	    this.mirrorBackupProvider.enabled(runAsMirror);
 	    this.mirrorBackupProvider.setup(this);
    }

    private void setApplicationManager(DeploymentDescriptor c,Lobby lb) throws Exception{
        if(lb.descriptor().accessMode() > c.accessMode) c.accessMode(lb.descriptor().accessMode());
        SingletonApplicationManager singletonApplicationManager = new SingletonApplicationManager(this,c);//pass the class loader
        this.availableApplicationManagers.put(c.distributionKey(),singletonApplicationManager);
        singletonApplicationManager.start();
        lb.addEntry(c);
    }

    public void configureViews(LobbyConfiguration conf){
 	    conf.views.forEach((v)->{
 	        this.deploymentServiceProvider.register(v);
        });
    }
	public OnLobby configure(LobbyConfiguration conf) throws Exception{
		DefaultLobby lb = this.setLobby(conf.descriptor);
        LobbyTypeIdIndex lobbyTypeIdIndex = new LobbyTypeIdIndex(this.node.deploymentId(),lb.descriptor().typeId());
        if(!masterDataStore().load(lobbyTypeIdIndex)) {
            lobbyTypeIdIndex = new LobbyTypeIdIndex(this.node.bucketId,lb.descriptor().typeId());
            if(!masterDataStore().load(lobbyTypeIdIndex)) throw new RuntimeException("no lobby config data");
        }
        GameCluster gameCluster = new GameCluster();
        if(conf.descriptor.resetEnabled && conf.descriptor.deployCode == DeployCode.USER_GAME_CLUSTER){
            gameCluster = this.loadGameCluster(lobbyTypeIdIndex.owner());
            if(gameCluster==null) throw new RuntimeException("no game cluster config data");
        }
        OnLobby _onLobby = new OnLobbyTrack(lb.descriptor().typeId(),lb.descriptor().deployCode(),lb.descriptor().resetEnabled(),false,lobbyTypeIdIndex.owner(),gameCluster.owner());
		Collections.sort(conf.applications, new DeploymentDescriptorComparator());//deploy by priority
        for (DeploymentDescriptor c : conf.applications) {
            if(c.disabled()) {
                log.warn("Application is disabled->"+c.tag);
                continue;
            }
            this.setApplicationManager(c, lb);
        }
        if(lb.descriptor().accessMode()==Access.PUBLIC_ACCESS_MODE){
            this.mlobbyList.add(lb);
        }
        return _onLobby;
	}
    public OnPartition[] partitions(){
 	    return this.integrationCluster.partitionStates;
    }

	public List<Lobby> lobbyList(){
		ArrayList<Lobby> blist = new ArrayList<>();
        this.mlobbyList.forEach((DefaultLobby l)->{
            blist.add(l);
        });
        return blist;
	}

    public Lobby lobby(String lobbyId){
        DefaultLobby lobby = this._lobbyMapping.get(lobbyId);
        return lobby;
    }
    public DefaultLobby setLobby(Descriptor ldescriptor){
 	    return this._lobbyMapping.computeIfAbsent(ldescriptor.typeId(),(String tid)->{
            DefaultLobby lb = new DefaultLobby(ldescriptor);
            return lb;
        });
    }
    public DeploymentServiceProvider deploymentService(){
 	    return this.deploymentServiceProvider;
    }
    public TokenValidatorProvider tokenValidatorProvider(){
 	    return this.tokenValidatorProvider;
    }
    public synchronized void setGameServiceProvider(GameCluster gameCluster){
        try{
            PlatformGameServiceProvider gameServiceProvider = new PlatformGameServiceProvider(gameCluster);
            gameCluster.dataStore(masterDataStore());
            this.deployServiceProvider(gameServiceProvider);
            gameServiceProvider.registerMetricsListener(metrics(Metrics.SYSTEM));
            gameServiceProvider.start();
        }catch (Exception ex){
            log.error("error on set game service provider",ex);
            throw new RuntimeException("failed to start game service provider->"+gameCluster.property(GameCluster.NAME));
        }
    }
    public synchronized void setGameClusterOnLobby(GameCluster gameCluster,Configurable.Listener listener){
 	    String publishingId = (String) gameCluster.property(GameCluster.PUBLISHING_ID);
 	    List<LobbyDescriptor> bList = masterDataStore().list(new LobbyQuery(publishingId));
        List<LobbyConfiguration> configurations = new ArrayList<>();
        bList.forEach((lb)->configurations.add(new LobbyConfiguration(lb)));
        Collections.sort(configurations,new LobbyComparator());
        configurations.forEach((c)->_setOnLobby(c,listener));
        IndexSet indexSet = new IndexSet();
        indexSet.distributionKey(this.node.deploymentId());
        indexSet.label(Account.GameClusterLabel);
        indexSet.keySet.add(gameCluster.distributionKey());
        if(!this.masterDataStore().createIfAbsent(indexSet,true)){
            indexSet.keySet.add(gameCluster.distributionKey());
            this.masterDataStore().update(indexSet);
        }
    }
    private void _setOnLobby(LobbyConfiguration lc,OnLobby.Listener listener){
        if(this._lobbyMapping.containsKey(lc.descriptor.typeId)){
            return;
        }
        LobbyDescriptor d = lc.descriptor;
        this.setLobby(d);//
        lc.applications = masterDataStore().list(new ApplicationQuery(d.distributionKey()));
        lc.views = masterDataStore().list(new OnViewQuery(d.distributionKey()));
        this.configureViews(lc);
        try{
            OnLobby ob = this.configure(lc);
            listener.onUpdated(ob);
        }catch (Exception ex){ex.printStackTrace();}
    }
    public synchronized void setOnLobby(LobbyDescriptor lobbyDescriptor,OnLobby.Listener listener){
 	    if(this._lobbyMapping.containsKey(lobbyDescriptor.typeId())){
 	        return;
        }
 	    this.setLobby(lobbyDescriptor);
        LobbyConfiguration lc = new LobbyConfiguration();
        lc.descriptor = lobbyDescriptor;
        lc.applications = this.masterDataStore().list(new ApplicationQuery(lobbyDescriptor.distributionKey()));
        //this.configureViews(lc);
        try{
            OnLobby ob = this.configure(lc);
            listener.onUpdated(ob);
        }catch (Exception ex){ex.printStackTrace();}
    }
    public synchronized void setOnLobby(String typeId,String publishingId,Configurable.Listener listener){
        if(this._lobbyMapping.containsKey(typeId)){
            return;
        }
        List<LobbyDescriptor> bList = masterDataStore().list(new LobbyQuery(publishingId));
        bList.forEach((d)->{
            this.setLobby(d);//
            LobbyConfiguration lc = new LobbyConfiguration();
            lc.descriptor = d;
            lc.applications = masterDataStore().list(new ApplicationQuery(d.distributionKey()));
            try{
                OnLobby ob = this.configure(lc);
                listener.onUpdated(ob);
            }catch (Exception ex){ex.printStackTrace();}
        });
        IndexSet indexSet = new IndexSet();
        indexSet.distributionKey(this.node.deploymentId
                ());
        indexSet.label(Account.ModuleLabel);
        indexSet.keySet.add(publishingId);
        if(!this.masterDataStore().createIfAbsent(indexSet,true)){
            indexSet.keySet.add(publishingId);
            this.masterDataStore().update(indexSet);
        }
    }
    public synchronized void unsetLobby(String typeId,Lobby.Listener listener){
        try{
            Lobby lb = this._lobbyMapping.remove(typeId);
            if(lb==null){
                return;
            }
            HashMap<String, Descriptor> _codeBase = new HashMap<>();
            Descriptor lab = null;
            for(Descriptor d : lb.entryList()){
                ApplicationProvider ap = this.availableApplicationManagers.get(d.distributionKey());
                if(d.codebase()!=null&&d.moduleName()!=null){
                    _codeBase.putIfAbsent(d.codebase(),d);
                }
                if(d.type().equals(Descriptor.TYPE_APPLICATION)){ //shut down app
                    ap.shutdown();
                    this.availableApplicationManagers.remove(d.distributionKey());
                }
                else{
                    lab = d;
                }
            }
            if(lab!=null){
                ApplicationProvider lbb = this.availableApplicationManagers.remove(lab.distributionKey());
                lbb.shutdown();
                listener.onLobby(lab);
            }
            _codeBase.forEach((k,v)-> listener.onLobby(v)); //clean module class loader
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
    public synchronized void setApplicationOnLobby(String typeId,String applicationId){
 	    Lobby lb = this._lobbyMapping.get(typeId);
 	    if(lb==null||this.availableApplicationManagers.containsKey(applicationId)){
 	        return;
        }
        try{
            DeploymentDescriptor deploymentDescriptor = new DeploymentDescriptor();
            deploymentDescriptor.distributionKey(applicationId);
            if(!masterDataStore().load(deploymentDescriptor)) throw new RuntimeException("no application config data");
            try{setApplicationManager(deploymentDescriptor,lb);}catch (Exception exx){
                throw new RuntimeException(exx);
            }
        } catch (Exception ex){
            log.error("error on setApplicationOnLobby",ex);
        }
    }
    public synchronized void unsetApplication(String typeId,String applicationId,Lobby.Listener listener){
 	    Lobby lb = this._lobbyMapping.get(typeId);
        if(lb==null){
            return;
        }
 	    try{
            HashMap<String,Descriptor> _codeBase = new HashMap<>();
            Descriptor lab = null;
            for(Descriptor d : lb.entryList()){
                if(d.type().equals(Descriptor.TYPE_APPLICATION)&&d.distributionKey().equals(applicationId)){
                    lb.removeEntry(applicationId);
                    ApplicationProvider app = this.availableApplicationManagers.remove(applicationId);
                    app.shutdown();
                }
                if(d.codebase()!=null&&d.moduleName()!=null){
                    _codeBase.putIfAbsent(d.codebase(),d);
                }
                if(d.type().equals(Descriptor.TYPE_LOBBY)){
                    lab = d;
                }
            }
            if(lb.entryList().size()==1&&(lab!=null)){//clean lobby and clean module class loaders
                this._lobbyMapping.remove(typeId);
                ApplicationProvider lbb = this.availableApplicationManagers.remove(lab.distributionKey());
                lbb.shutdown();
                listener.onLobby(lab);
                _codeBase.forEach((k,v)-> listener.onLobby(v));
            }

        }catch (Exception ex){
 	        ex.printStackTrace();
        }
    }

    public DataStore dataStore(String name,int partition){
        return this.deploymentDataStoreProvider.create(name,partition);
    }

    //list the database list on deploy service
    public DataStoreProvider dataStoreProvider(){
 	    return this.deploymentDataStoreProvider;
    }
    @Override
    public ClusterProvider clusterProvider(){
        return integrationCluster;
    }

    public EventService eventService(){
 	    return integrationCluster.publisher();
    }
    @Override
    public AccessIndexService accessIndexService(){
        return this.integrationCluster.accessIndexService();
    }
    @Override
    public DeploymentServiceProvider deploymentServiceProvider(){
 	    return this.deploymentServiceProvider;
    }

    public DataStore masterDataStore(){
        return this.deploymentDataStoreProvider.create(DeploymentServiceProvider.DEPLOY_DATA_STORE,this.node.partitionNumber());
    }
    public RecoverableRegistry recoverableRegistry(int registryId){
 	    return fMap.get(registryId);
    }


    public EndpointService endpointService(){
 	    return this.endpointService;
    }

    public IntegrationCluster integrationCluster(){
        return integrationCluster;
    }

    public ServiceProvider serviceProvider(String name){
        return this.serviceProviders.get(name);
    }
    public void serviceProvider(ServiceProvider serviceProvider){
 	    this.serviceProviders.put(serviceProvider.name(),serviceProvider);
    }
    public void _setup() throws Exception{

        AccessIndex bid = this.accessIndexService().setIfAbsent(this.clusterNameSuffix+"/"+node.bucketName,AccessIndex.SYSTEM_INDEX);
        node.bucketId = bid.distributionKey();
        AccessIndex nid = this.accessIndexService().setIfAbsent(node.nodeName,AccessIndex.SYSTEM_INDEX);
        node.nodeId = nid.distributionKey();
        AccessIndex did = this.accessIndexService().setIfAbsent(this.clusterNameSuffix+"/deploymentId",AccessIndex.SYSTEM_INDEX);
        if(!backupEnabled){//using local deployment id
            node.deploymentId = did.distributionKey();
            log.warn("Using local deployment id ["+node.deploymentId+"]");
        }
        if(bid==null || nid==null || did==null) throw new RuntimeException("Need to restart the server again");

        integrationCluster.registerNode(this.node);//may throw node already registered runtime exception
        //
        log.info("Bucket->"+dataBucketGroup+" is registered on ["+node.bucketId+"]");
        log.info("Node->"+dataBucketNode+" is registered on ["+node.nodeId+"]");
        log.info("Backup Development id ["+node.deploymentId+"] is registered on node ["+node.nodeName+"]");
        initMetricsProvider();

 	    this.serviceProviders.forEach((k,v)->{ //synchronize data and setup
            v.setup(this);
            v.waitForData();//block for global data sync
        });
 	    this.serviceProviders.put(this.integrationCluster.name(),integrationCluster);
 	    this.serviceProviders.put(this.deploymentDataStoreProvider.name(),this.deploymentDataStoreProvider);
        this.serviceProviders.put(AccessIndexService.NAME,accessIndexService());
        this.serviceProviders.put(mirrorBackupProvider.name(),mirrorBackupProvider);
        this.serviceProviders.put(JVMMonitor.NAME,new JVMMonitor());
        ServiceProviderConfigurationParser spc = new ServiceProviderConfigurationParser("tarantula-platform-service-provider-config.xml",serviceProviders);
        spc.start(this);
        serviceViewList.add(this.deploymentDataStoreProvider.name());
        serviceViewList.add(JVMMonitor.NAME);
        serviceViewList.add(UDPEndpoint.UDP_ENDPOINT);
        serviceViewList.add(this.integrationCluster.name());
        this.deploymentDataStoreProvider.registerMetricsListener(this.metrics(Metrics.PERFORMANCE));
        this.integrationCluster.registerMetricsListener(this.metrics(Metrics.PERFORMANCE));
        this.serviceProvider(UserService.NAME).registerMetricsListener(this.metrics(Metrics.ACCESS));
        this.deploymentServiceProvider.registerMetricsListener(this.metrics(Metrics.DEPLOYMENT));
        this.postOfficeSession = new PostOfficeSession(this.integrationCluster.publisher());
    }
    public void _syncNodeData() throws Exception{
        _systemServiceStarted.await();
 	    this.accessIndexService().onDisable();
 	    _access_index_syc_finished.await();
 	    for(int i=0;i<accessIndexRoutingNumber;i++){
 	        CountDownLatch countDownLatch = new CountDownLatch(1);
 	        String _pk = "p"+i;
 	        _syncLatch.put(_pk,countDownLatch);
 	        if(this.accessIndexService().onStartSync(i,_pk)==-1){
 	            countDownLatch.countDown();
            }
 	        countDownLatch.await();
 	        _syncLatch.remove(_pk);
        }
 	    log.warn("Access index data sync has finished");
        //sync data store
        List<String> dataStoreList = this.deploymentDataStoreProvider.list();
        for (String ds : dataStoreList){
            CountDownLatch countDownLatch = new CountDownLatch(1);
            String syncKey = "sync_"+ds;
            _syncLatch.put(syncKey,countDownLatch);
            this.integrationCluster.recoverService().onStartSync(ds,syncKey);
            countDownLatch.await();
            log.warn("Data store sync ended->"+syncKey);
            _syncLatch.remove(syncKey);
        }
        for(String s : this.integrationCluster.recoverService().onListModules()){
            log.warn("Loading module files from master node ["+s+"]");
            byte[] ret = this.integrationCluster.recoverService().onLoadModuleJarFile(s);
            if(ret.length>0){
                _writeContent(s,ret);
            }
        }
        this.accessIndexService().onEnable();
        this.schedule(new MidnightCheck(this));
        metricsManager.start();
        DataStore dataStore = this.dataStore(serviceEventLogStore,node.partitionNumber);
        serviceEventLogger = new PlatformServiceEventLogger(dataStore,tarantulaServiceEventLogPersistenceEnable);
 	}


    public boolean deployServiceProvider(ServiceProvider serviceProvider){
        try{
            this.serviceProviders.computeIfAbsent(serviceProvider.name(),(sn)->{
                serviceProvider.setup(new ServiceContextProxy(this));
                serviceProvider.waitForData();
                return serviceProvider;
            });
            return true;
        }catch (Exception ex){
            log.error("error->",ex);
            throw new RuntimeException("Failed to deploy service provider ["+serviceProvider.name()+"]");
        }
    }
    public void releaseServiceProvider(String name){
 	    ServiceProvider serviceProvider = this.serviceProviders.remove(name);
 	    if(serviceProvider!=null){
 	        try{serviceProvider.shutdown();}catch (Exception ex){}//ignore exception
        }
    }
    public HttpClientProvider httpClientProvider(){
 	    return this.httpClientProvider;
    }
    public TarantulaLogger logger(Class target){
        return JDKLogger.getLogger(target);
    }
    public TarantulaLogger logger(String target){
        return JDKLogger.getLogger(target);
    }


    public ClusterProvider.Node node(){
         return this.node;
    }

    public static MemberDiscovery memberDiscovery(int scope){
 	    memberDiscovery.scope(scope);
 	    return memberDiscovery;
    }
    public void atMidnight() {
 	    try{
            serviceProviders.forEach((k,v)->{
                v.atMidnight();
            });
            //this.deploymentDataStoreProvider.atMidnight();
            availableApplicationManagers.forEach((k,v)->{
                v.atMidnight();
            });
            endpointService.atMidnight();

 	    }catch (Exception ex){
 	        ex.printStackTrace();
 	    }
        this.schedule(new MidnightCheck(this));
    }
    public AuthVendorRegistry authVendor(String name){
 	    if(name.equals(OnAccess.GOOGLE)) return new ThirdPartyServiceProvider(OnAccess.GOOGLE);

 	    if(name.equals(OnAccess.FACEBOOK)) return new ThirdPartyServiceProvider(OnAccess.FACEBOOK);

        if(name.equals(OnAccess.APPLE_STORE)) return new ThirdPartyServiceProvider(OnAccess.APPLE_STORE);

 	    if(name.equals(OnAccess.STRIPE)) return loadStripeCredentials();//system config

        if(name.equals(OnAccess.DEVELOPER_STORE)) return new ThirdPartyServiceProvider(OnAccess.DEVELOPER_STORE);

        if(name.equals(OnAccess.GAME_CENTER)) return new ThirdPartyServiceProvider(OnAccess.GAME_CENTER);

        if(name.equals(OnAccess.GOOGLE_STORE)) return new ThirdPartyServiceProvider(OnAccess.GOOGLE_STORE);

        if(name.equals(OnAccess.AMAZON)) return new ThirdPartyServiceProvider(OnAccess.AMAZON);

        if(name.equals(OnAccess.APPLICATION_STORE)) return new ThirdPartyServiceProvider(OnAccess.APPLICATION_STORE);
        return null;

    }

    private AuthVendorRegistry loadStripeCredentials(){
        try{
            String config = this.authContext+"-stripe-credentials.json";
            File f = new File("/etc/tarantula/"+config);
            InputStream in = f.exists()?new FileInputStream(f):Thread.currentThread().getContextClassLoader().getResourceAsStream(config);
            byte[] data = new byte[in.available()];
            in.read(data);
            in.close();
            GsonBuilder gb = new GsonBuilder();
            gb.registerTypeAdapter(AuthVendorRegistry.class,new StripePaymentCredentialsDeserializer());
            return gb.create().fromJson(new String(data),AuthVendorRegistry.class);
        }catch (Exception ex){
            return null;
        }
    }

    //file name web/[game cluster name]/file.png etc
    public void _writeContent(String fileName,byte[] content){
        try{
            //write to local deploy dir to be ready for deployment
            BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(this.deployDir+"/"+fileName));
            fos.write(content);
            fos.flush();
            fos.close();
        }catch (Exception ex){
            log.error("error on content write",ex);
        }
    }
    public String[] _listModuleContent(){
 	    File file = new File(this.deployDir);
        return file.list((d,f)->f.endsWith(".jar"));
    }
    public byte[] _readContent(String fileName){
 	    try{
 	        BufferedInputStream fin = new BufferedInputStream(new FileInputStream(this.deployDir+"/"+fileName));
 	        byte[] ret = fin.readAllBytes();
 	        fin.close();
 	        return ret;
        }catch (Exception ex){
 	        log.error("error on read content",ex);
 	        return new byte[0];
        }
    }

    public Response checkResource(OnView pending,String targetFolder){
 	    Response response = new ResponseHeader();
 	    try{
 	        int ix = pending.moduleResourceFile().lastIndexOf('/');
 	        String checkFile = ix<0?pending.moduleResourceFile():pending.moduleResourceFile().substring(ix+1);
 	        File rfile = new File(deployDir+"/"+checkFile);
 	        if(!rfile.exists()){
 	            log.warn("File not existed->"+checkFile);
 	            response.message("file not existed->"+checkFile);
 	            return response;
            }
            String moduleContext = pending.moduleContext();
            String webContext = "/";
 	        if(!moduleContext.startsWith("root")){
                ix = moduleContext.indexOf('/');
                webContext = ix<0?"/"+moduleContext:"/"+moduleContext.substring(0,ix);
            }
            RequestHandler resource = endpointService.requestHandler(webContext);
 	        if(resource==null||!resource.deployable()){
 	            response.message(moduleContext+" not existed");
 	            return response;
            }
 	        File ft = new File(deployDir+"/"+targetFolder+"/"+pending.moduleResourceFile());
 	        if(ft.exists()&&ft.lastModified()>=rfile.lastModified()){
 	            response.message("File already has latest version");
 	            return response;
            }
            response.successful(true);
            response.message("resource->"+moduleContext);
            return response;
 	    }catch (Exception ex) {
            response.message(ex.getMessage());
            return response;
        }
    }
    public Response checkModule(String context,String moduleFile){
        Response response = new ResponseHeader();
        try{
            File checkFile = new File(deployDir+"/"+moduleFile);
            if(!checkFile.exists()){
                log.warn("File not existed->"+checkFile);
                response.message("file not existed->"+checkFile);
                return response;
            }
            File ft = new File(deployDir+"/module/"+context+"/"+moduleFile);
            if(ft.exists()&&ft.lastModified()>=checkFile.lastModified()){
                response.message("File already has latest version");
                return response;
            }
            response.successful(true);
            response.message("module validated->"+moduleFile);
            return response;
        }catch (Exception ex){
 	        response.message(ex.getMessage());
 	        return response;
        }
    }
    public ModuleClassLoader moduleClassLoader(String moduleId){
 	    return (ModuleClassLoader)this.deploymentService().classLoader(moduleId);
    }

    public Configuration configuration(String config){
 	    try{

 	        Map<String,Object> kv = loadConfigurationFromEtc(config);
 	        if(kv==null) kv = JsonUtil.toMap(Thread.currentThread().getContextClassLoader().getResourceAsStream(config+".json"));
            ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration();
            kv.forEach((k,v)->applicationConfiguration.property(k,v));
            return applicationConfiguration;
 	    }catch (Exception ex){
            log.warn("no master config->"+config);
 	        return  null;
        }
    }
    private Map<String,Object> loadConfigurationFromEtc(String config){
 	    try{
            File f = new File("/etc/tarantula/"+config+".json");
            if(!f.exists()) return null;
            InputStream in = new FileInputStream(f);
            Map<String,Object> ret = JsonUtil.toMap(in);
            in.close();
            return ret;
        }catch (Exception ex){
 	        log.warn("using default config->"+config);
 	        return null;
        }
    }
    public List<Descriptor> availableServices(){
 	    URL url = Thread.currentThread().getContextClassLoader().getResource("application/deploy");
 	    File f = new File(url.getFile());
        ArrayList<Descriptor> alist = new ArrayList<>();
 	    if(!f.exists()) return alist;
        f.list((m,n)->{
 	        if(n.endsWith(".json")){
 	            Descriptor app = JsonServiceParser.descriptor("application/deploy",n);
 	            if(!app.disabled()) alist.add(app);
            }
 	        return false;
        });
 	    alist.addAll(availableServicesUpgraded());
 	    return alist;
    }
    public List<Descriptor> availableServicesUpgraded(){
        ArrayList<Descriptor> alist = new ArrayList<>();
        try{
            URL url = Thread.currentThread().getContextClassLoader().getResource("application/upgrade");
            File f = new File(url.getFile());
            f.list((m,n)->{
                if(n.endsWith(".json")){
                    Descriptor app = JsonServiceParser.descriptor("application/upgrade",n);
                    if(!app.disabled()) alist.add(app);
                }
                return false;
            });
        }
        catch (Exception ex){
            log.error("failed to load upgrade services",ex);
        }
        return alist;
    }

    public Configuration configuration(GameCluster gameCluster,String config){
        return cMap.computeIfAbsent(config,(k)->{
            try{
                if(config.equals(GameCluster.GAME_APPLICATION_CATEGORY_TEMPLATE)
                        || config.equals(GameCluster.GAME_ITEM_CATEGORY_TEMPLATE)
                        || config.equals(GameCluster.GAME_COMMODITY_CATEGORY_TEMPLATE)
                        || config.equals(GameCluster.GAME_COMPONENT_CATEGORY_TEMPLATE)
                        || config.equals(GameCluster.GAME_ASSET_CATEGORY_TEMPLATE)){
                    File f = new File(this.deployDir+"/conf/"+gameCluster.property(GameCluster.NAME)+"/"+config);
                    return fromDir(f);
                }
                if(config.equals(GameCluster.GAME_UPGRADE_CATEGORY_TEMPLATE)){
                    URL src = Thread.currentThread().getContextClassLoader().getResource("config-template/"+GameCluster.GAME_UPGRADE_CATEGORY_TEMPLATE);
                    File fd = new File(src.getFile());
                    return fromDir(fd);
                }
                FileInputStream fileInputStream = new FileInputStream(this.deployDir+"/conf/"+gameCluster.property(GameCluster.NAME)+"/"+config+".json");
                ConfigurableTemplate item = JsonConfigurableTemplateParser.itemSet(fileInputStream);
                fileInputStream.close();
                return item;
            }catch (Exception exx){
                log.warn("configuration not existed->"+config,exx);
                return null;
            }
        });
    }

    public List<OnView> loadViewList(String typeId){
 	    ArrayList<OnView> _vlist = new ArrayList<>();
        InputStream fin = Thread.currentThread().getContextClassLoader().getResourceAsStream("view/view-"+typeId+"-settings.json");
        try{
 	        JsonObject jview = JsonUtil.parse(fin);
            String context = jview.get("context").getAsString();
            JsonArray views = jview.get("viewList").getAsJsonArray();
 	        views.forEach((je)->{
 	            JsonObject jv = je.getAsJsonObject();
 	            OnViewTrack view = new OnViewTrack();
 	            view.moduleContext(context);
 	            view.viewId(jv.get("type").getAsString());
 	            view.moduleResourceFile(jv.get("moduleResourceFile").getAsString());
 	            _vlist.add(view);
            });
 	    }catch (Exception ex){
 	        log.warn("no view config->"+typeId);
        }
        finally {
            if(fin!=null){
                try{fin.close();}catch (IOException ioex){}
            }
        }
        return _vlist;
    }

    public void registerAuthVendor(TokenValidatorProvider.AuthVendor authVendor){
 	    try{_systemServiceStarted.await();}catch (Exception ex){}
        TokenValidatorProvider tokenValidatorProvider = (TokenValidatorProvider)this.serviceProvider(TokenValidatorProvider.NAME);
 	    ThirdPartyServiceProvider thirdPartyServiceProvider = (ThirdPartyServiceProvider)tokenValidatorProvider.authVendor(authVendor.name());
        if(thirdPartyServiceProvider == null) throw new RuntimeException("third party provider not existed ["+authVendor.name()+"]");
        log.warn("Third party provider ["+authVendor.name()+"] registered with type id ["+authVendor.typeId()+"]");
        thirdPartyServiceProvider.registerAuthVendor(authVendor);
 	}
    public void unregisterAuthVendor(TokenValidatorProvider.AuthVendor authVendor){
        TokenValidatorProvider tokenValidatorProvider = (TokenValidatorProvider)this.serviceProvider(TokenValidatorProvider.NAME);
        ThirdPartyServiceProvider thirdPartyServiceProvider = (ThirdPartyServiceProvider)tokenValidatorProvider.authVendor(authVendor.name());
        if(thirdPartyServiceProvider == null) throw new RuntimeException("third party provider not existed ["+authVendor.name()+"]");
        log.warn("Third party provider ["+authVendor.name()+"] unregistered with type id ["+authVendor.typeId()+"]");
        thirdPartyServiceProvider.releaseAuthVendor(authVendor);
    }

    public void registerBackupProvider(BackupProvider backupProvider){
 	    this.mirrorBackupProvider.addBackupProvider(backupProvider);
 	}
    public void unregisterBackupProvider(BackupProvider backupProvider){
 	    this.mirrorBackupProvider.removeBackupProvider(backupProvider);
    }
    public BackupProvider backupProvider(){
 	    return this.mirrorBackupProvider;
    }

    public Metrics metrics(String name){
         return metricsManager.metrics(name);
    }

    public void registerMetrics(Metrics metrics){
        metricsManager.addMetrics(metrics);
    }
    public void unregisterMetrics(Metrics metrics){
        metricsManager.removeMetrics(metrics);
    }
    public List<String> metricsList(){
 	    return metricsManager.listMestrics();
    }

    private void initMetricsProvider() throws Exception{
 	    JsonObject json = JsonUtil.parse(Thread.currentThread().getContextClassLoader().getResourceAsStream("tarantula-metrics-provider.json"));
        JsonArray mlist = json.getAsJsonArray("metrics-list");
        for(JsonElement e : mlist){
            String cln = e.getAsJsonObject().get("class-name").getAsString();
            boolean enabled = e.getAsJsonObject().get("enabled").getAsBoolean();
            if(!enabled) continue;
            Metrics metrics = (Metrics) Class.forName(cln).getConstructor().newInstance();
            metrics.setup(this);
            metricsManager.addMetrics(metrics);
        }
 	}

 	private ConfigurableTemplate fromDir(File d) throws Exception{
        ConfigurableTemplate itemSet = new ConfigurableTemplate();
        JsonArray items = new JsonArray();
        for(String fn : d.list()){
            FileInputStream fin = new FileInputStream(d.getAbsoluteFile()+"/"+fn);
            ConfigurableTemplate temps = JsonConfigurableTemplateParser.itemSet(fin);
            fin.close();
            itemSet.type = temps.type;
            itemSet.name = temps.name;
            itemSet.description = temps.description;
            itemSet.version = temps.version;
            itemSet.category = temps.category;
            items.addAll((JsonArray)temps.property("itemList"));
        }
        itemSet.property("itemList",items);
        return itemSet;
    }

    public PostOffice postOffice(){
        return this.postOfficeSession;
    }

    public void log(String message,int level){
        switch (level){
            case OnLog.DEBUG:
                this.log.debug(message);
                break;
            case OnLog.INFO:
                this.log.info(message);
                break;
            case OnLog.WARN:
                this.log.warn(message);
                break;
        }

    }
    public void log(String message,Exception error,int level){
        switch (level){
            case OnLog.WARN:
                if(error!=null){
                    this.log.warn(message);
                }
                else{
                    this.log.warn(message,error);
                }
                break;
            case OnLog.ERROR:
                this.log.error(message,error);
                break;
        }
    }

    public GameCluster loadGameCluster(String key){
         GameCluster gameCluster = gMap.computeIfAbsent(key,k->{
             GameCluster gc = new GameCluster();
             gc.distributionKey(key);
             gc.dataStore(this.masterDataStore());
             if(!this.masterDataStore().load(gc)) return null;
             gc.gameLobby = this.lobby(gc.lobbyType());
             gc.serviceLobby = this.lobby(gc.serviceType());
             gc.dataLobby = this.lobby(gc.dataType());
             gc.setup(this);
             this.deploymentServiceProvider.registerConfigurableListener(OnLobby.TYPE,gc);
             return gc;
         });
         if(gameCluster.disabled()) unloadGameCluster(key);
         return gameCluster;
    }
    public void unloadGameCluster(String key){
         gMap.remove(key);
    }

    public ServiceEventLogger serviceEventLogger(){
        return serviceEventLogger;
    }

}
