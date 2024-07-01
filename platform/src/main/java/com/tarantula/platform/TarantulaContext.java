package com.tarantula.platform;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hazelcast.config.ClasspathXmlConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.DiscoveryStrategyConfig;
import com.icodesoftware.*;
import com.icodesoftware.lmdb.EnvSetting;
import com.icodesoftware.lmdb.LocalDistributionIdGenerator;
import com.icodesoftware.lmdb.MetricsLog;
import com.icodesoftware.lmdb.TransactionLogManager;
import com.icodesoftware.service.*;
import com.icodesoftware.service.Metrics;
import com.icodesoftware.util.*;
import com.icodesoftware.logging.JDKLogger;

import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.event.TransactionReplicationEvent;
import com.tarantula.platform.item.ConfigurableTemplate;
import com.tarantula.platform.item.JsonConfigurableTemplateParser;
import com.tarantula.platform.service.*;
import com.tarantula.platform.bootstrap.ServiceBootstrap;
import com.tarantula.platform.service.cluster.*;

import com.tarantula.platform.service.deployment.*;

import com.tarantula.platform.service.metrics.AbstractMetrics;
import com.tarantula.platform.service.metrics.MetricsHomingAgent;
import com.tarantula.platform.service.metrics.MetricsManager;
import com.tarantula.platform.service.persistence.*;



public class TarantulaContext implements Serviceable, ServiceContext, MetricsHomingAgent {


    private static TarantulaLogger log = JDKLogger.getLogger(TarantulaContext.class);
	
	private static final TarantulaContext BC = new TarantulaContext();

	public final static  CountDownLatch _storageInstanceStarted = new CountDownLatch(1);
    public final static  CountDownLatch _storageStarted = new CountDownLatch(1); //data store provider waitForData call finished;
 	public final static  CountDownLatch _integrationClusterStarted = new CountDownLatch(1);
   
    public final static  CountDownLatch _accessIndexServiceStarted = new CountDownLatch(1);
    public final static  CountDownLatch _deployServiceStarted = new CountDownLatch(1);
    public final static  CountDownLatch _recoverServiceStarted = new CountDownLatch(1);
    public final static CountDownLatch _cluster_service_ready = new CountDownLatch(3);
    public final static CountDownLatch _systemServiceStarted = new CountDownLatch(1);
 	public final static  CountDownLatch _tarantulaApplicationStarted = new CountDownLatch(1);


    public AtomicBoolean node_started;


    private static final String CONFIG_INTEGRATION = "hazelcast-integration.xml";

    private static final String DATA_STORE_CONFIG = "tarantula-platform-data-store-config.json";
    private IntegrationCluster integrationCluster;
	
	private final EndpointService endpointService;

    private final ConcurrentHashMap<String,DefaultLobby> _lobbyMapping = new ConcurrentHashMap<>(); //typeId =>


    private final List<DefaultLobby> mlobbyList = new LinkedList();

    private final ConcurrentHashMap<Long, ApplicationProvider> availableApplicationManagers = new ConcurrentHashMap<>(); //id =>

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

    public static int operationRetries;
    public static long operationRejectInterval;


    private final ConcurrentHashMap<String,ServiceProvider> serviceProviders = new ConcurrentHashMap();

    public DataStoreProvider deploymentDataStoreProvider;
    private DataScopeReplicationProxy dataScopeReplicationProxy;
    private IntegrationScopeReplicationProxy integrationScopeReplicationProxy;

    private final ConcurrentHashMap<Integer,RecoverableListener> fMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String,ConfigurableTemplate> cMap = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<Long,GameCluster> gMap = new ConcurrentHashMap<>();

    private final MetricsManager metricsManager;

    public String dataBucketGroup;
    public String dataBucketNode;
    private ClusterNode node;

    public int snowflakeNodeNumber = 1;
    public int[] snowflakeEpochStart = {2020,1,1};// start from 2020 1,1

    public int storeSizeMb = 100;
    public boolean externalKeyValueBufferUsed;
    public int storeKeySize = EnvSetting.KEY_SIZE;
    public int storeValueSize = EnvSetting.VALUE_SIZE;
    public int storePendingBufferSize = EnvSetting.MAX_PENDING_BUFFER_NUMBER;

    public boolean storeNoSync = false;
    public String dataStoreDir;
    public static boolean dataStoreReindexing;

    public boolean dataStoreDailyBackup;

    public int maxIdlesOnInstance;
    public long timeoutOnInstance;

    public boolean kubernetesDiscoveryEnabled;
    public String kubernetesServiceName;
    public String clusterNameSuffix;
    public int clusterInitialSize;
    public int clusterMaxSize;
    public int platformRoutingNumber;
    public int accessIndexRoutingNumber;

    public int maxReplicationNumber = 3;

    public static int operationTimeout = 5;
    public static boolean lobbySubscriptionEnabled = false;

    public String authContext = "localhost";


    public List<String> serviceViewList = new ArrayList<>();
    private PostOfficeSession postOfficeSession;


    private HttpClientProvider httpClientProvider;


    private DataStoreProvider.DistributionIdGenerator distributionIdGenerator;

    public boolean homingAgentEnabled;
    public String homingAgentHost;
    public String homingAgentKey;

    private TarantulaContext(){
         this.endpointService = new EndpointService(this);
 	     this.metricsManager = new MetricsManager(this);
    }

	public static TarantulaContext getInstance(){
 	    return BC;
	}

	public void start() throws Exception {
 	    if(this.dataBucketNode.length() != 3) throw new RuntimeException("Node name must be 3 letters");
        String bin = System.getProperty("user.dir");
        File file = new File(bin+"/data.mdb");
        if(file.exists()){
            log.warn("Replacing index data set with remote data ["+file.getAbsolutePath()+"]");
            String indexPath = DataStoreConfigurationJsonParser.storeIndexDir(DATA_STORE_CONFIG);
            if(!FileUtil.deleteDirectory(deployDir)) throw new RuntimeException("deploy directory must be deleted");
            if(!FileUtil.deleteDirectory(dataStoreDir)) throw new RuntimeException("data store directory must be deleted");
            Path _path = Paths.get(dataStoreDir+"/"+indexPath);
            if(!Files.exists(_path)) Files.createDirectories(_path);
            FileInputStream mdb = new FileInputStream(file);
            FileOutputStream target = new FileOutputStream(new File(_path.toFile(),"data.mdb"));
            mdb.transferTo(target);
            target.flush();
            mdb.close();
            target.close();
            file.delete();
        }
        this.scheduledExecutorService = TarantulaExecutorServiceFactory.createScheduledExecutorService(this.applicationSchedulingPoolSetting);
        this.httpClientProvider = new HttpCaller();
        this.httpClientProvider.start();
        this.node = new ClusterNode(this.dataBucketGroup,this.dataBucketNode,this.accessIndexRoutingNumber,this.platformRoutingNumber);
        this.node.clusterNameSuffix = this.clusterNameSuffix;
        this.node.deployDirectory = this.deployDir;
        this.node.servicePushAddress = this.servicePushAddress;

        this.node.dailyBackupEnabled = this.dataStoreDailyBackup;
        this.node.dataStoreDirectory = this.dataStoreDir;
        this.node.homingAgentEnabled = this.homingAgentEnabled;
        this.node.homingAgentHost = this.homingAgentHost;
        this.node.homingAgentKey = this.homingAgentKey;
        long epochStart = TimeUtil.epochMillisecondsFromMidnight(snowflakeEpochStart[0],snowflakeEpochStart[1],snowflakeEpochStart[2]);
        this.distributionIdGenerator = new LocalDistributionIdGenerator(snowflakeNodeNumber,epochStart);

        node_started = new AtomicBoolean(false);
        PortableProviderConfigurationParser pcs = new PortableProviderConfigurationParser("tarantula-platform-portable-provider.xml");
        pcs.parse().forEach((r)->{
            if(fMap.contains(r.registryId())){
                throw new RuntimeException("Duplicate portable registry ["+r.registryId()+"]");
            }
            fMap.put(r.registryId(),r);
            log.warn("Portable registry ["+r.registryId()+"] added");
        });
        this.dataScopeReplicationProxy = new DataScopeReplicationProxy();
        this.integrationScopeReplicationProxy = new IntegrationScopeReplicationProxy();
        HashMap<String,Object> storeAdditions = new HashMap<>();
        storeAdditions.put("storeSizeMb",storeSizeMb);
        storeAdditions.put("envNoSyncFlag",storeNoSync);
        storeAdditions.put("storeReindexing",dataStoreReindexing);
        storeAdditions.put("externalKeyValueBufferUsed",externalKeyValueBufferUsed);
        storeAdditions.put("storeKeySize",storeKeySize);
        storeAdditions.put("storeValueSize",storeValueSize);
        storeAdditions.put("storePendingBufferSize",storePendingBufferSize);
        DataStoreConfigurationJsonParser sparser = new DataStoreConfigurationJsonParser(DATA_STORE_CONFIG,this,storeAdditions,dataStoreProvider -> {
            try{
                this.deploymentDataStoreProvider = dataStoreProvider;
                this.deploymentDataStoreProvider.registerDistributionIdGenerator(this.distributionIdGenerator);
                this.deploymentDataStoreProvider.registerMapStoreListener(Distributable.INTEGRATION_SCOPE,integrationScopeReplicationProxy);
                this.deploymentDataStoreProvider.registerMapStoreListener(Distributable.DATA_SCOPE,dataScopeReplicationProxy);
                this.deploymentDataStoreProvider.start();
                this.deploymentDataStoreProvider.setup(this);
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

        var hazelcastJoin = gcfg.getNetworkConfig().getJoin();
        if(kubernetesDiscoveryEnabled && kubernetesServiceName != null){
            hazelcastJoin.getMulticastConfig().setEnabled(false);
            hazelcastJoin.getKubernetesConfig().setEnabled(true);
            hazelcastJoin.getKubernetesConfig().setProperty("service-port", "5702");
            hazelcastJoin.getKubernetesConfig().setProperty("service-name", kubernetesServiceName);
        }
        else{
            DiscoveryStrategyConfig discoveryStrategyConfig = new DiscoveryStrategyConfig("com.tarantula.platform.service.cluster.TarantulaDiscoveryStrategy");
            discoveryStrategyConfig.addProperty("tarantula-port", "5702");
            hazelcastJoin.getDiscoveryConfig().addDiscoveryStrategyConfig(discoveryStrategyConfig);
        }
        this.integrationCluster = new IntegrationCluster(gcfg,this.dataBucketGroup,this);
        new ServiceBootstrap(_storageInstanceStarted,_integrationClusterStarted,this.integrationCluster,"integration-cluster",true).start(); //integration cluster start
        new ServiceBootstrap(_systemServiceStarted, _tarantulaApplicationStarted, new TarantulaApplicationDeployer(this),"application-deployer",true).start();
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

    private void setApplicationManager(DeploymentDescriptor c,Lobby lb) throws Exception{
        if(lb.descriptor().accessMode() > c.accessMode) c.accessMode(lb.descriptor().accessMode());
        SingletonApplicationManager singletonApplicationManager = new SingletonApplicationManager(this,c);//pass the class loader
        this.availableApplicationManagers.put(c.distributionId(),singletonApplicationManager);
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
            gameCluster = this.loadGameCluster(lobbyTypeIdIndex.gameClusterId());
            if(gameCluster==null) throw new RuntimeException("no game cluster config data");
        }
        OnLobby _onLobby = new OnLobbyTrack(lb.descriptor().typeId(),lb.descriptor().deployCode(),lb.descriptor().resetEnabled(),false,lobbyTypeIdIndex.gameClusterId(),gameCluster.accountId());
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
 	    return this.integrationCluster.partitionSet();
    }
    public OnPartition[] buckets(){
        return this.integrationCluster.bucketSet();
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
            gMap.put(gameCluster.distributionId(),gameCluster);
        }catch (Exception ex){
            log.error("error on set game service provider",ex);
            throw new RuntimeException("failed to start game service provider->"+gameCluster.property(GameCluster.NAME));
        }
    }
    public synchronized void setGameClusterOnLobby(GameCluster gameCluster,Configurable.Listener listener){
 	    //long publishingId = gameCluster.publishingId;//(String) gameCluster.property(GameCluster.PUBLISHING_ID);
 	    List<LobbyDescriptor> bList = masterDataStore().list(new LobbyQuery(gameCluster.distributionId()));
        List<LobbyConfiguration> configurations = new ArrayList<>();
        bList.forEach((lb)->configurations.add(new LobbyConfiguration(lb)));
        Collections.sort(configurations,new LobbyComparator());
        configurations.forEach((c)->_setOnLobby(c,listener));
    }
    private void _setOnLobby(LobbyConfiguration lc,OnLobby.Listener listener){
        if(this._lobbyMapping.containsKey(lc.descriptor.typeId)){
            return;
        }
        LobbyDescriptor d = lc.descriptor;
        this.setLobby(d);//
        lc.applications = masterDataStore().list(new ApplicationQuery(d.distributionId()));
        //lc.views = masterDataStore().list(new OnViewQuery(d.distributionKey()));
        //this.configureViews(lc);
        try{
            OnLobby ob = this.configure(lc);
            listener.onUpdated(ob);
        } catch (Exception ex){
            log.error("error on _setOnLobby",ex);
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
                ApplicationProvider ap = this.availableApplicationManagers.get(d.distributionId());
                if(d.codebase()!=null&&d.moduleName()!=null){
                    _codeBase.putIfAbsent(d.codebase(),d);
                }
                if(d.type().equals(Descriptor.TYPE_APPLICATION)){ //shut down app
                    ap.shutdown();
                    this.availableApplicationManagers.remove(d.distributionId());
                }
                else{
                    lab = d;
                }
            }
            if(lab!=null){
                ApplicationProvider lbb = this.availableApplicationManagers.remove(lab.distributionId());
                lbb.shutdown();
                listener.onLobby(lab);
            }
            _codeBase.forEach((k,v)-> listener.onLobby(v)); //clean module class loader
        }catch (Exception ex){
            log.error("Error on unsetLobby",ex);
        }
    }
    public synchronized void setApplicationOnLobby(String typeId,long applicationId){
 	    Lobby lb = this._lobbyMapping.get(typeId);
 	    if(lb==null||this.availableApplicationManagers.containsKey(applicationId)){
 	        return;
        }
        try{
            DeploymentDescriptor deploymentDescriptor = new DeploymentDescriptor();
            deploymentDescriptor.distributionId(applicationId);
            if(!masterDataStore().load(deploymentDescriptor)) throw new RuntimeException("no application config data");
            try{setApplicationManager(deploymentDescriptor,lb);}catch (Exception exx){
                throw new RuntimeException(exx);
            }
        } catch (Exception ex){
            log.error("error on setApplicationOnLobby",ex);
        }
    }
    public synchronized void unsetApplication(String typeId,long applicationId,Lobby.Listener listener){
 	    Lobby lb = this._lobbyMapping.get(typeId);
        if(lb==null){
            return;
        }
 	    try{
            HashMap<String,Descriptor> _codeBase = new HashMap<>();
            Descriptor lab = null;
            for(Descriptor d : lb.entryList()){
                if(d.type().equals(Descriptor.TYPE_APPLICATION) && d.distributionId()==(applicationId)){
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
                ApplicationProvider lbb = this.availableApplicationManagers.remove(lab.distributionId());
                lbb.shutdown();
                listener.onLobby(lab);
                _codeBase.forEach((k,v)-> listener.onLobby(v));
            }

        }catch (Exception ex){
            log.error("Error on unsetApplication",ex);
        }
    }

    public DataStore dataStore(int scope,String name){
         if(scope==Distributable.LOCAL_SCOPE){
             return this.deploymentDataStoreProvider.createLocalDataStore(name);
         }
         if(scope==Distributable.DATA_SCOPE){
            return this.deploymentDataStoreProvider.createDataStore(name);
         }
         if(scope==Distributable.INTEGRATION_SCOPE){
             return this.deploymentDataStoreProvider.createAccessIndexDataStore(name);
         }
         if(scope==Distributable.INDEX_SCOPE){
             return this.deploymentDataStoreProvider.createKeyIndexDataStore(name);
         }
        if(scope==Distributable.LOG_SCOPE){
            return this.deploymentDataStoreProvider.createLogDataStore(name);
        }
         throw new IllegalArgumentException("scope ["+scope+"] not supported");
    }

    public DataStore dataStore(ApplicationSchema applicationSchema,int scope,String name){
        String storeName = applicationSchema.serviceType().replaceAll("-","_")+"_app_"+name;
        return dataStore(scope,storeName);
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

    private AccessIndexService accessIndexService(){
        return this.integrationCluster.accessIndexService();
    }

    @Override
    public DeploymentServiceProvider deploymentServiceProvider(){
 	    return this.deploymentServiceProvider;
    }

    public DataStore masterDataStore(){
        return this.deploymentDataStoreProvider.createDataStore(DeploymentServiceProvider.DEPLOY_DATA_STORE);
    }
    public <T extends Recoverable> RecoverableRegistry<T> recoverableRegistry(int registryId){
 	    return fMap.get(registryId);
    }
    public void recoverableRegistry(RecoverableListener recoverableRegistry){
        if(fMap.contains(recoverableRegistry.registryId())) throw new RuntimeException("registry already exist ["+recoverableRegistry.registryId()+"]");
        fMap.put(recoverableRegistry.registryId(),recoverableRegistry);
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
        node.bucketId = bid.distributionId();
        AccessIndex nid = this.accessIndexService().setIfAbsent(node.nodeName,AccessIndex.SYSTEM_INDEX);
        node.nodeId = nid.distributionId();
        AccessIndex did = this.accessIndexService().setIfAbsent(this.clusterNameSuffix+"/deploymentId",AccessIndex.SYSTEM_INDEX);
        node.deploymentId = did.distributionId();
        log.warn("Using local deployment id ["+node.deploymentId+"]");
        if(bid==null || nid==null || did==null) throw new RuntimeException("Need to restart the server again");
        log.info("Bucket->"+dataBucketGroup+" is registered on ["+node.bucketId+"]");
        log.info("Node->"+dataBucketNode+" is registered on ["+node.nodeId+"]");
        log.info("Backup Development id ["+node.deploymentId+"] is registered on node ["+node.nodeName+"]");
        log.info("Current directory : "+FileUtil.currentDirectory());
        integrationCluster.registerNode(this.node);//may throw node already registered runtime exception

        initMetricsProvider();
 	    this.serviceProviders.forEach((k,v)->{ //synchronize data and setup
            v.setup(this);
            v.waitForData();//block for global data sync
        });
 	    this.serviceProviders.put(this.integrationCluster.name(),integrationCluster);
 	    this.serviceProviders.put(this.deploymentDataStoreProvider.name(),this.deploymentDataStoreProvider);
        this.serviceProviders.put(AccessIndexService.NAME,accessIndexService());
        this.serviceProviders.put(DeployService.NAME,integrationCluster.deployService());

        ServiceProviderConfigurationParser spc = new ServiceProviderConfigurationParser("tarantula-platform-service-provider-config.xml",serviceProviders);
        spc.start(this);

        this.deploymentDataStoreProvider.registerMetricsListener(this.metrics(Metrics.DATA_STORE));
        this.integrationCluster.registerMetricsListener(this.metrics(Metrics.CLUSTER));
        this.serviceProvider(UserService.NAME).registerMetricsListener(this.metrics(Metrics.SYSTEM));
        this.deploymentServiceProvider.registerMetricsListener(this.metrics(Metrics.DEPLOYMENT));
        this.postOfficeSession = new PostOfficeSession(this.integrationCluster.publisher());
    }
    public void _syncNodeData() throws Exception{
 	    this.accessIndexService().onDisable();
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
            log.error("Error on at midnight task",ex);
 	    }
        this.schedule(new MidnightCheck(this));
    }
    public AuthVendorRegistry authVendor(String name){
 	    if(name.equals(OnAccess.GOOGLE)) return new ThirdPartyServiceProvider(OnAccess.GOOGLE);

 	    if(name.equals(OnAccess.FACEBOOK)) return new ThirdPartyServiceProvider(OnAccess.FACEBOOK);

        if(name.equals(OnAccess.APPLE_STORE)) return new ThirdPartyServiceProvider(OnAccess.APPLE_STORE);

        if(name.equals(OnAccess.DEVELOPER_STORE)) return new ThirdPartyServiceProvider(OnAccess.DEVELOPER_STORE);

        if(name.equals(OnAccess.GAME_CENTER)) return new ThirdPartyServiceProvider(OnAccess.GAME_CENTER);

        if(name.equals(OnAccess.GOOGLE_STORE)) return new ThirdPartyServiceProvider(OnAccess.GOOGLE_STORE);

        if(name.equals(OnAccess.AMAZON)) return new ThirdPartyServiceProvider(OnAccess.AMAZON);

        if(name.equals(OnAccess.APPLICATION_STORE)) return new ThirdPartyServiceProvider(OnAccess.APPLICATION_STORE);

        if(name.equals(OnAccess.WEB_HOOK)) return new ThirdPartyServiceProvider(OnAccess.WEB_HOOK);

        if(name.equals(OnAccess.JDBC_SQL)) return new ThirdPartyServiceProvider(OnAccess.JDBC_SQL);

        if(name.equals(OnAccess.DOWNLOAD_CENTER)) return new ThirdPartyServiceProvider(OnAccess.DOWNLOAD_CENTER);
        return null;

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
                    File f = new File(this.deployDir+"/conf/"+gameCluster.name()+"/"+config);
                    return fromDir(f);
                }
                //if(config.equals(GameCluster.GAME_UPGRADE_CATEGORY_TEMPLATE)){
                    //URL src = Thread.currentThread().getContextClassLoader().getResource("config-template/"+GameCluster.GAME_UPGRADE_CATEGORY_TEMPLATE);
                    //File fd = new File(src.getFile());
                    //return fromDir(fd);
                //}
                FileInputStream fileInputStream = new FileInputStream(this.deployDir+"/conf/"+gameCluster.name()+"/"+config+".json");
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
 	    //try{_systemServiceStarted.await();}catch (Exception ex){}
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
 	    return metricsManager.listMetrics();
    }

    private void initMetricsProvider() throws Exception{
 	    JsonObject json = JsonUtil.parse(Thread.currentThread().getContextClassLoader().getResourceAsStream("tarantula-metrics-provider.json"));
        JsonArray mlist = json.getAsJsonArray("metrics-list");
        for(JsonElement e : mlist){
            String cln = e.getAsJsonObject().get("class-name").getAsString();
            boolean enabled = e.getAsJsonObject().get("enabled").getAsBoolean();
            if(!enabled) continue;
            Metrics metrics = (Metrics) Class.forName(cln).getConstructor().newInstance();
            if(metrics instanceof AbstractMetrics){
                ((AbstractMetrics)metrics).registerMetricsHomeAgent(this);
            }
            metrics.setup(this);
            metricsManager.addMetrics(metrics);
        }
        mlist = json.getAsJsonArray("monitor-list");
        for(JsonElement e : mlist){
            String cln = e.getAsJsonObject().get("class-name").getAsString();
            boolean enabled = e.getAsJsonObject().get("enabled").getAsBoolean();
            if(!enabled) continue;
            ServiceProvider monitor = (ServiceProvider)Class.forName(cln).getConstructor().newInstance();
            monitor.setup(this);
            monitor.start();
            serviceProviders.put(monitor.name(),monitor);
            serviceViewList.add(monitor.name());
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

    public GameCluster loadGameCluster(long key){
         GameCluster gameCluster = gMap.computeIfAbsent(key,k->{
             GameCluster gc = new GameCluster();
             gc.distributionId(key);
             gc.dataStore(this.masterDataStore());
             if(!this.masterDataStore().load(gc)){
                 log.warn("Game cluster not existed ["+key+"]");
                 return null;
             }
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
    public void unloadGameCluster(long key){
         gMap.remove(key);
    }



    @Override
    public long distributionId() {
        return distributionIdGenerator.id();
    }

    public Transaction transaction(int scope){
         return dataStoreProvider().transaction(scope);
    }

    public void onTransactionEvent(int scope,TransactionReplicationEvent event){
         if(scope==Distributable.DATA_SCOPE){
             dataScopeReplicationProxy.onTransactionReplicationEvent(event);
             return;
         }
         if(scope==Distributable.INTEGRATION_SCOPE){
             integrationScopeReplicationProxy.onTransactionReplicationEvent(event);
             return;
         }
         log.warn("Event on scope ["+scope+"] not supported");
    }

    public TransactionLogManager transactionLogManager(int scope){
        if(scope==Distributable.DATA_SCOPE){
            return dataScopeReplicationProxy.transactionLogManager();
        }
        if(scope==Distributable.INTEGRATION_SCOPE){
            return integrationScopeReplicationProxy.transactionLogManager();
        }
        throw new RuntimeException("transaction manager on scope ["+scope+"] not supported");
    }

    public Recoverable.DataBufferPair dataBufferPair(){
         return deploymentDataStoreProvider.dataBufferPair();
    }

    @Override
    public void onMetrics(String name, List<Statistics.Entry> updated) {
        if(!node.homingAgentEnabled) return;
        log.warn(node.nodeName+ " :: "+name+" :: "+updated.isEmpty());
        schedule(new ScheduleRunner(100,()->{
            try {
                String[] headers = new String[]{
                        Session.TARANTULA_ACCESS_KEY,node().homingAgentKey()
                };
                httpClientProvider().post(node().homingAgentHost(), "metrics", headers, MetricsLog.metricsLog(node.nodeName,name,updated).toBinary());
            }catch (Exception ex){
                log.warn("error on homing agent metrics log: "+ex.getMessage());
            }
        }));
    }
}
