package com.tarantula.platform;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hazelcast.config.ClasspathXmlConfig;
import com.hazelcast.config.Config;
import com.icodesoftware.*;
import com.icodesoftware.service.*;
import com.icodesoftware.service.Metrics;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.TarantulaExecutorServiceFactory;
import com.icodesoftware.logging.JDKLogger;
import com.tarantula.game.service.GameServiceProvider;
import com.tarantula.platform.item.ConfigurableTemplate;
import com.tarantula.platform.item.JsonConfigurableTemplateParser;
import com.tarantula.platform.service.*;
import com.tarantula.platform.bootstrap.ServiceBootstrap;
import com.tarantula.platform.service.cluster.*;
import com.tarantula.platform.service.deployment.*;
import com.tarantula.platform.service.metrics.PerformanceMetrics;
import com.tarantula.platform.service.persistence.DataStoreConfigurationXMLParser;
import com.tarantula.platform.service.persistence.Node;
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

    public String dataBucketGroup;
    public String dataBucketNode;
    private Node node;
    public String dataReplicationThreadPoolSetting;

    public String dataStoreDir;

    public boolean dataStoreDailyBackup;

    public int maxIdlesOnInstance;
    public long timeoutOnInstance;

    public PerformanceMetrics performanceMetrics;

    public String clusterNameSuffix;

    public String platformVersion;
    public int platformRoutingNumber;
    public int accessIndexRoutingNumber;

    public static ScopedMemberDiscovery memberDiscovery;
    public static int operationTimeout = 5;
    public static boolean lobbySubscriptionEnabled = false;

    public String authContext = "localhost";

    public ConcurrentHashMap<String,CountDownLatch> _syncLatch = new ConcurrentHashMap<>();

 	private TarantulaContext(){
 	    this.endpointService = new EndpointService(this);
    }

	public static TarantulaContext getInstance(){
 	    return BC;
	}

	public void start() throws Exception {
        this.scheduledExecutorService = TarantulaExecutorServiceFactory.createScheduledExecutorService(this.applicationSchedulingPoolSetting);
        this.node = new Node(this.dataBucketGroup,this.dataBucketNode);
 	     _storageInstanceStarted = new CountDownLatch(1);
         _integrationClusterStarted = new CountDownLatch(1);
         _tarantulaApplicationStarted = new CountDownLatch(1);
        _tarantulaInstanceStarted = new CountDownLatch(1);
        _accessIndexServiceStarted = new CountDownLatch(1);
        _storageStarted = new CountDownLatch(1);
        _deployServiceStarted = new CountDownLatch(1);
        _systemServiceStarted = new CountDownLatch(1);
        _access_index_syc_finished = new CountDownLatch(1);
        node_started = new AtomicBoolean(false);

        ServiceProviderConfigurationParser spc = new ServiceProviderConfigurationParser("tarantula-platform-service-provider-config.xml",serviceProviders);
        PortableProviderConfigurationParser pcs = new PortableProviderConfigurationParser("tarantula-platform-portable-provider.xml");
        pcs.parse().forEach((r)->{
            fMap.put(r.registryId(),r);
        });
        new ServiceBootstrap(new CountDownLatch(0),null,spc,"service-provider",true).start();
        DataStoreConfigurationXMLParser sparser = new DataStoreConfigurationXMLParser("tarantula-platform-data-store-config.xml",this);
        new ServiceBootstrap(new CountDownLatch(0),_storageInstanceStarted,sparser,"system-data-store-parser",true).start();
        Config gcfg = new ClasspathXmlConfig(Thread.currentThread().getContextClassLoader(),CONFIG_INTEGRATION);
        gcfg.getProperties().setProperty("hazelcast.partition.count",""+accessIndexRoutingNumber);
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
        this.schedule(new MidnightCheck(this));
	}
	public void shutdown() throws Exception {
        performanceMetrics.shutdown();
	    this.scheduledExecutorService.shutdown();
        this.endpointService.shutdown();
        this.integrationCluster.shutdown();
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
    public ApplicationProvider applicationManager(String applicationId){
       return this.availableApplicationManagers.get(applicationId);
    }

    private void setApplicationManager(DeploymentDescriptor c,Lobby lb) throws Exception{
        //c.instanceId(UUID.randomUUID().toString());
        c.accessMode(lb.descriptor().accessMode());
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
        LobbyTypeIdIndex lobbyTypeIdIndex = new LobbyTypeIdIndex(this.bucketId(),lb.descriptor().typeId());
        if(!masterDataStore().load(lobbyTypeIdIndex)) throw new RuntimeException("no lobby config data");
        GameCluster gameCluster = new GameCluster();
        if(conf.descriptor.resetEnabled&&conf.descriptor.deployCode==DeployCode.USER_GAME_CLUSTER){
            gameCluster.distributionKey(lobbyTypeIdIndex.owner());
            if(!masterDataStore().load(gameCluster)) throw new RuntimeException("no game cluster config data");
        }
        OnLobby _onLobby = new OnLobbyTrack(lb.descriptor().typeId(),lb.descriptor().deployCode(),lb.descriptor().resetEnabled(),false,lobbyTypeIdIndex.owner(),(String) gameCluster.property(GameCluster.OWNER));
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
            GameServiceProvider gameServiceProvider = new GameServiceProvider(gameCluster);
            gameCluster.dataStore(masterDataStore());
            this.deployServiceProvider(gameServiceProvider);
            gameServiceProvider.start();
        }catch (Exception ex){
            throw new RuntimeException("failed to start game service provider->"+gameCluster.property(GameCluster.NAME));
        }
    }
    public synchronized void setGameClusterOnLobby(GameCluster gameCluster,Configurable.Listener listener){
 	    String publishingId = (String) gameCluster.property(GameCluster.PUBLISHING_ID);
 	    List<LobbyDescriptor> bList = masterDataStore().list(new LobbyQuery(publishingId));//this.queryFromIntegrationNode(memberId,PortableRegistry.OID,new LobbyQuery(publishingId),new String[]{publishingId},false);
        List<LobbyConfiguration> configurations = new ArrayList<>();
        bList.forEach((lb)->configurations.add(new LobbyConfiguration(lb)));
        Collections.sort(configurations,new LobbyComparator());
        configurations.forEach((c)->_setOnLobby(c,listener));
        IndexSet indexSet = new IndexSet();
        indexSet.distributionKey(this.bucketId());
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
        lc.applications = masterDataStore().list(new ApplicationQuery(d.distributionKey()));//this.queryFromIntegrationNode(memberId,PortableRegistry.OID,new ApplicationQuery(d.distributionKey()),new String[]{d.distributionKey()},false);
        lc.views = masterDataStore().list(new OnViewQuery(d.distributionKey()));//this.queryFromIntegrationNode(memberId,PortableRegistry.OID,new OnViewQuery(d.distributionKey()),new String[]{d.distributionKey()},false);
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
        lc.applications = this.masterDataStore().list(new ApplicationQuery(lobbyDescriptor.distributionKey()));//this.queryFromIntegrationNode(memberId,PortableRegistry.OID,new ApplicationQuery(lobbyDescriptor.distributionKey()),new String[]{lobbyDescriptor.distributionKey()},false);
        //lc.views = this.queryFromDataMaster(PortableRegistry.OID,new OnViewQuery(lobbyDescriptor.distributionKey()),new String[]{lobbyDescriptor.distributionKey()});
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
        List<LobbyDescriptor> bList = masterDataStore().list(new LobbyQuery(publishingId));//this.queryFromIntegrationNode(memberId,PortableRegistry.OID,new LobbyQuery(publishingId),new String[]{publishingId},false);
        bList.forEach((d)->{
            this.setLobby(d);//
            LobbyConfiguration lc = new LobbyConfiguration();
            lc.descriptor = d;
            lc.applications = masterDataStore().list(new ApplicationQuery(d.distributionKey()));//this.queryFromIntegrationNode(memberId,PortableRegistry.OID,new ApplicationQuery(d.distributionKey()),new String[]{d.distributionKey()},false);
            try{
                OnLobby ob = this.configure(lc);
                listener.onUpdated(ob);
            }catch (Exception ex){ex.printStackTrace();}
        });
        IndexSet indexSet = new IndexSet();
        indexSet.distributionKey(this.bucketId());
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
    public DataStore dataStore(String name){
        return this.deploymentDataStoreProvider.create(name);
    }
    public DataStore dataStore(String name,int partition){
        return this.deploymentDataStoreProvider.create(name,partition);
    }
    public int partitionNumber(){
 	    return this.platformRoutingNumber;
    }

    public String clusterNameSuffix(){
         return this.clusterNameSuffix;
    }

    public String servicePushAddress(){
         return servicePushAddress;
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
        return (AccessIndexService)this.serviceProviders.get(AccessIndexService.NAME);
    }
    @Override
    public DeploymentServiceProvider deploymentServiceProvider(){
 	    return this.deploymentServiceProvider;
    }

    public DataStore masterDataStore(){
        return this.deploymentDataStoreProvider.create(DeploymentServiceProvider.DEPLOY_DATA_STORE,this.partitionNumber());
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
    public void _setup(){
        this.serviceProviders.forEach((k,v)->{ //synchronize data and setup
            v.setup(this);
            v.waitForData();//block for global data sync
        });
    }
    public void _registerNode() throws Exception{
 	    this.accessIndexService().disable();
 	    _access_index_syc_finished.await();
 	    for(int i=0;i<accessIndexRoutingNumber;i++){
 	        CountDownLatch countDownLatch = new CountDownLatch(1);
 	        String _pk = "p"+i;
 	        _syncLatch.put(_pk,countDownLatch);
 	        if(this.accessIndexService().syncStart(i,_pk)==-1){
 	            countDownLatch.countDown();
            }
 	        countDownLatch.await();
 	        _syncLatch.remove(_pk);
        }
 	    log.warn("Access index data sync has finished");
        CountDownLatch _tarantula_sync = new CountDownLatch(1);
        _syncLatch.put("t100",_tarantula_sync);
        this.integrationCluster.recoverService().syncStart(DeploymentServiceProvider.DEPLOY_DATA_STORE,"t100");
        _tarantula_sync.await();
        _syncLatch.remove("t100");


        for(String s : this.integrationCluster.recoverService().listModules()){
            log.warn("Loading module files from master node ["+s+"]");
            byte[] ret = this.integrationCluster.recoverService().loadModuleJarFile(s);
            if(ret.length>0){
                _writeContent(s,ret);
            }
        }
        this.accessIndexService().enable();
        AccessIndex bid = this.accessIndexService().setIfAbsent(node.bucketName,0);
        node.bucketId = bid.distributionKey();
        AccessIndex nid = this.accessIndexService().setIfAbsent(node.nodeName,0);
        node.nodeId = nid.distributionKey();
        this.performanceMetrics = new PerformanceMetrics();
        this.performanceMetrics.setup(this);
        this.deploymentDataStoreProvider.registerMetricsListener(this.performanceMetrics);
        this.integrationCluster.registerMetricsListener(this.performanceMetrics);
        log.info("Bucket->"+dataBucketGroup+" is registered on ["+node.bucketId+"]");
        log.info("Node->"+dataBucketNode+" is registered on ["+node.nodeId+"]");
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
    public TarantulaLogger logger(Class target){
        return JDKLogger.getLogger(target);
    }
    public TarantulaLogger logger(String target){
        return JDKLogger.getLogger(target);
    }
    public String bucket(){
 	    return this.dataBucketGroup;
    }
    public String bucketId(){
 	    return node.bucketId;
    }
    public String nodeId(){
        return node.nodeId;
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
            this.deploymentDataStoreProvider.atMidnight();
            availableApplicationManagers.forEach((k,v)->{
                v.atMidnight();
            });
            endpointService.atMidnight();
            performanceMetrics.atMidnight();
 	    }catch (Exception ex){
 	        ex.printStackTrace();
 	    }
        this.schedule(new MidnightCheck(this));
    }
    public AuthVendorRegistry authVendor(String name){
 	    if(name.equals(OnAccess.GOOGLE)) return loadGoogleCredentials();

 	    if(name.equals(OnAccess.FACEBOOK)) return loadFacebookCredentials();

        if(name.equals(OnAccess.APPLE_STORE)) return loadAppleStoreCredentials();

 	    if(name.equals(OnAccess.STRIPE)) return loadStripeCredentials();

        if(name.equals(OnAccess.MOCK_STORE)) return loadMockStoreCredentials();

        if(name.equals(OnAccess.GAME_CENTER)) return new GameCenterAuthProvider();

        if(name.equals(OnAccess.GOOGLE_STORE)) return loadGoogleStoreCredentials();

        if(name.equals(OnAccess.AMAZON)) return loadAmazonAwsCredentials();

        return null;

    }
    private AuthVendorRegistry loadFacebookCredentials(){
        try{
            String config = this.authContext+"-facebook-auth.json";
            File f = new File("/etc/tarantula/"+config);
            InputStream in = f.exists()?new FileInputStream(f):Thread.currentThread().getContextClassLoader().getResourceAsStream(config);
            byte[] data = new byte[in.available()];
            in.read(data);
            in.close();
            GsonBuilder gb = new GsonBuilder();
            gb.registerTypeAdapter(AuthVendorRegistry.class,new FacebookAuthCredentialsDeserializer());
            return gb.create().fromJson(new String(data),AuthVendorRegistry.class);
        }catch (Exception ex){
            return null;
        }
    }
    private AuthVendorRegistry loadGoogleCredentials(){
 	    try{
            String config = this.authContext+"-google-auth.json";
            File f = new File("/etc/tarantula/"+config);
            InputStream in = f.exists()?new FileInputStream(f):Thread.currentThread().getContextClassLoader().getResourceAsStream(config);
            byte[] data = new byte[in.available()];
            in.read(data);
            in.close();
            GsonBuilder gb = new GsonBuilder();
            gb.registerTypeAdapter(AuthVendorRegistry.class,new GoogleAuthCredentialsDeserializer());
            return gb.create().fromJson(new String(data),AuthVendorRegistry.class);
 	    }catch (Exception ex){
 	        return null;
        }
    }
    private AuthVendorRegistry loadGoogleStoreCredentials(){
        try{
            String config = this.authContext+"-google-store.json";
            File f = new File("/etc/tarantula/"+config);
            InputStream in = f.exists()?new FileInputStream(f):Thread.currentThread().getContextClassLoader().getResourceAsStream(config);
            byte[] data = new byte[in.available()];
            in.read(data);
            in.close();
            GsonBuilder gb = new GsonBuilder();
            gb.registerTypeAdapter(AuthVendorRegistry.class,new GoogleStoreCredentialsDeserializer());
            return gb.create().fromJson(new String(data),AuthVendorRegistry.class);
        }catch (Exception ex){
            return null;
        }
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

    private AuthVendorRegistry loadAppleStoreCredentials(){
        try{
            String config = this.authContext+"-apple-iap-credentials.json";
            File f = new File("/etc/tarantula/"+config);
            InputStream in = f.exists()?new FileInputStream(f):Thread.currentThread().getContextClassLoader().getResourceAsStream(config);
            byte[] data = new byte[in.available()];
            in.read(data);
            in.close();
            GsonBuilder gb = new GsonBuilder();
            gb.registerTypeAdapter(AuthVendorRegistry.class,new AppleStoreCredentialsDeserializer());
            return gb.create().fromJson(new String(data),AuthVendorRegistry.class);
        }catch (Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    private AuthVendorRegistry loadAmazonAwsCredentials(){
        try{
            String config = this.authContext+"-amazon-iam-credentials.json";
            File f = new File("/etc/tarantula/"+config);
            InputStream in = f.exists()?new FileInputStream(f):Thread.currentThread().getContextClassLoader().getResourceAsStream(config);
            byte[] data = new byte[in.available()];
            in.read(data);
            in.close();
            GsonBuilder gb = new GsonBuilder();
            gb.registerTypeAdapter(AuthVendorRegistry.class,new AmazonAuthCredentialsDeserializer());
            return gb.create().fromJson(new String(data),AuthVendorRegistry.class);
        }catch (Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    private AuthVendorRegistry loadMockStoreCredentials(){
        try{
            String config = this.authContext+"-mock-store-credentials.json";
            File f = new File("/etc/tarantula/"+config);
            InputStream in = f.exists()?new FileInputStream(f):Thread.currentThread().getContextClassLoader().getResourceAsStream(config);
            byte[] data = new byte[in.available()];
            in.read(data);
            in.close();
            GsonBuilder gb = new GsonBuilder();
            gb.registerTypeAdapter(AuthVendorRegistry.class,new MockStoreCredentialsDeserializer());
            return gb.create().fromJson(new String(data),AuthVendorRegistry.class);
        }catch (Exception ex){
            ex.printStackTrace();
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
 	    URL url = Thread.currentThread().getContextClassLoader().getResource("deploy");
 	    File f = new File(url.getFile());
        ArrayList<Descriptor> alist = new ArrayList<>();
 	    f.list((m,n)->{
 	        if(n.endsWith(".json")){
 	            Descriptor app = JsonServiceParser.descriptor(n);
 	            if(!app.disabled()) alist.add(JsonServiceParser.descriptor(n));
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
                    File f = new File(this.deployDir+"/conf/"+gameCluster.property(GameCluster.NAME)+"/"+config);
                    ConfigurableTemplate itemSet = new ConfigurableTemplate();
                    JsonArray items = new JsonArray();
                    for(String fn : f.list()){
                        FileInputStream fin = new FileInputStream(f.getAbsoluteFile()+"/"+fn);
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

    public String deployDirectory(){
         return deployDir;
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

    public Metrics metrics(String name){
         return performanceMetrics;
    }
}
