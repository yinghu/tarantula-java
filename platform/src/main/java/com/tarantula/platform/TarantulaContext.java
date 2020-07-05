package com.tarantula.platform;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.gson.GsonBuilder;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.ClasspathXmlConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.security.UsernamePasswordCredentials;
import com.tarantula.*;
import com.tarantula.logging.JDKLogger;
import com.tarantula.platform.event.PortableEventRegistry;
import com.tarantula.platform.presence.GameCluster;
import com.tarantula.platform.service.*;
import com.tarantula.platform.bootstrap.TarantulaExecutorServiceFactory;
import com.tarantula.platform.bootstrap.ServiceBootstrap;
import com.tarantula.platform.service.cluster.*;
import com.tarantula.platform.service.deployment.*;
import com.tarantula.platform.service.persistence.DataStoreConfigurationXMLParser;
import com.tarantula.platform.util.GoogleAuthCredentialsDeserializer;
import com.tarantula.platform.util.StripePaymentCredentialsDeserializer;
import com.tarantula.platform.util.SystemUtil;

public class TarantulaContext implements Serviceable,ServiceContext{


    private static TarantulaLogger log = JDKLogger.getLogger(TarantulaContext.class);
	
	private static final TarantulaContext BC = new TarantulaContext();

    public static  CountDownLatch _systemStorageInstanceStarted ;

	public static  CountDownLatch _storageInstanceStarted ;
 	
 	public static  CountDownLatch _tarantulaClusterStarted ;
    public static  CountDownLatch _integrationClusterStarted ;
 	
 	public static  CountDownLatch _tarantulaApplicationStarted ;

    public static  CountDownLatch _integrationInstanceStarted ;
    public static  CountDownLatch _tarantulaInstanceStarted;

    public static  CountDownLatch _accessIndexServiceStarted ;

    public static  CountDownLatch _storageStarted;

    public static  CountDownLatch _deployServiceStarted;

    public static CountDownLatch _systemServiceStarted;

    public AtomicBoolean node_started;


	private static final String CONFIG_DATA = "hazelcast-bucket.xml";
    private static final String CONFIG_INTEGRATION = "hazelcast-integration.xml";
	private TarantulaCluster tarantulaCluster;
    private IntegrationCluster integrationCluster;
	
	private final EndpointService endpointService;

    private final ConcurrentHashMap<String,DefaultLobby> _lobbyMapping = new ConcurrentHashMap<>();


    private final List<DefaultLobby> mlobbyList = new LinkedList();

    private final ConcurrentHashMap<String,Application> availableApplicationManagers = new ConcurrentHashMap<>();

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

    public  String eventThreadPoolSetting;

    public int retries; //event retries
    public long retryInterval; //event retry interval time

    public int operationRetries;
    public long operationRejectInterval;


    private final ConcurrentHashMap<String,ServiceProvider> serviceProviders = new ConcurrentHashMap();
    private final ConcurrentHashMap<String,ServiceProvider> dataStoreProviders = new ConcurrentHashMap();
    private final ConcurrentHashMap<String,List<Configuration>> configurations = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer,RecoverableListener> fMap = new ConcurrentHashMap<>();


    public String dataBucketGroup;
    public String dataBucketNode;

    public String dataReplicationThreadPoolSetting;

    public String dataStoreDir;
    public String dataStoreRecoveryDir;

    public int bootstrapRetries = 1 ;
    public String dataStoreMaster;

    public boolean dataStoreDailyBackup;

    public int maxIdlesOnInstance;
    public long timeoutOnInstance;
    public int metricsUpdateIntervalMinutes=1;
    public String clusterNamePrefix;

    public String platformVersion;
    public int platformRoutingNumber;
    public int integrationShardingNumber;
    public int dataShardingNumber;

    public String endpointIp ="localhost";
    public int endpointPort = 6393;
    public static ScopedMemberDiscovery memberDiscovery;

    public String authContext = "localhost";

 	private TarantulaContext(){
 	    this.endpointService = new EndpointService(this);
    }

	public static TarantulaContext getInstance(){
 	    return BC;
	}

	public void start() throws Exception {
        this.scheduledExecutorService = TarantulaExecutorServiceFactory.createScheduledExecutorService(this.applicationSchedulingPoolSetting);

 	    _systemStorageInstanceStarted = new CountDownLatch(1);
         _storageInstanceStarted = new CountDownLatch(1);
        _tarantulaClusterStarted = new CountDownLatch(1);
         _integrationClusterStarted = new CountDownLatch(1);
         _tarantulaApplicationStarted = new CountDownLatch(1);
        _integrationInstanceStarted = new CountDownLatch(1);
         _tarantulaInstanceStarted = new CountDownLatch(1);
        _accessIndexServiceStarted = new CountDownLatch(1);
        _storageStarted = new CountDownLatch(1);
        _deployServiceStarted = new CountDownLatch(2);
        _systemServiceStarted = new CountDownLatch(1);
        node_started = new AtomicBoolean(false);

        ServiceProviderConfigurationParser spc = new ServiceProviderConfigurationParser("tarantula-platform-service-provider-config.xml",serviceProviders);
        PortableProviderConfigurationParser pcs = new PortableProviderConfigurationParser("tarantula-platform-portable-provider.xml");
        pcs.parse().forEach((r)->{
            fMap.put(r.registryId(),r);
        });
        new ServiceBootstrap(new CountDownLatch(0),null,spc,"service-provider",true).start();
        DataStoreConfigurationXMLParser sparser = new DataStoreConfigurationXMLParser("tarantula-platform-data-store-config.xml",this,this.dataStoreProviders);
        new ServiceBootstrap(new CountDownLatch(0),_storageInstanceStarted,sparser,"system-data-store-parser",true).start();
        Config cfg = new ClasspathXmlConfig(Thread.currentThread().getContextClassLoader(),CONFIG_DATA);
        cfg.getGroupConfig().setName(this.clusterNamePrefix+"-"+this.dataBucketGroup);
        this.tarantulaCluster= new TarantulaCluster(cfg,this.dataBucketGroup,this);
        Config gcfg = new ClasspathXmlConfig(Thread.currentThread().getContextClassLoader(),CONFIG_INTEGRATION);
        gcfg.getGroupConfig().setName(this.clusterNamePrefix+"-integration");
        this.integrationCluster = new IntegrationCluster(gcfg,this.dataBucketGroup,this);
        new ServiceBootstrap(_storageInstanceStarted,_tarantulaClusterStarted,this.tarantulaCluster,"data-cluster",true).start();//data cluster start
        new ServiceBootstrap(_tarantulaClusterStarted,_integrationClusterStarted,this.integrationCluster,"integration-cluster",true).start(); //integration cluster start
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
	    this.scheduledExecutorService.shutdown();
        this.endpointService.shutdown();
        this.integrationCluster.shutdown();
        this.tarantulaCluster.shutdown();
        for(ServiceProvider ds : serviceProviders.values()){
            ds.shutdown();
        }
        for(ServiceProvider ds : dataStoreProviders.values()){
            ds.shutdown();
        }
    }

	public ScheduledFuture<?> schedule(SchedulingTask task){
        if(task.oneTime()){
            return this.scheduledExecutorService.schedule(task,task.initialDelay()+task.delay(),TimeUnit.MILLISECONDS);
        }else{
            return this.scheduledExecutorService.scheduleAtFixedRate(task,task.initialDelay(),task.delay(),TimeUnit.MILLISECONDS);
        }
    }
    public Application applicationManager(String applicationId){
       return this.availableApplicationManagers.get(applicationId);
    }

    private void setApplicationManager(DeploymentDescriptor c,Lobby lb) throws Exception{
        if(c.singleton()){
            c.instanceId(UUID.randomUUID().toString());
            c.accessMode(lb.descriptor().accessMode());
            SingletonApplicationManager singletonApplicationManager = new SingletonApplicationManager(this,c);//pass the class loader
            this.availableApplicationManagers.put(c.distributionKey(),singletonApplicationManager);
            singletonApplicationManager.start();
        }
        else{
            if(c.type().equals("application")){
                c.tag(lb.descriptor().tag());//inject lobby tag
                ApplicationManager am = new ApplicationManager(this,c);//pass the class loader
                this.availableApplicationManagers.put(c.distributionKey(),am);//will use subtypeId as the app register key
                am.start(); //recover on application manager start
            }
            else{//add future application type here
                log.info("deployment type ["+c.type()+"] not supported");
            }
        }
        lb.addEntry(c);
    }
    public void configure(ServiceConfiguration conf) throws Exception{
        List<ApplicationConfiguration> alist = this.query(new String[]{conf.distributionKey(),conf.tag},new ApplicationConfigurationQuery(conf.distributionKey(),conf.tag));
        for(ApplicationConfiguration afg : alist){
            conf.configurationMappings.put(new CompositeKey(conf.tag,afg.type),afg);
        }
        if(conf.serviceProviderName!=null){ //application service provider
            ServiceProvider serviceProvider = (ServiceProvider) Class.forName(conf.serviceProviderName).getConstructor().newInstance();
            serviceProvider.start();
            serviceProvider.setup(new ServiceContextProxy(this));//inject the proxy to service provider instead of the singleton
            serviceProvider.waitForData();
            this.serviceProviders.put(serviceProvider.name(),serviceProvider);
        }
        List<Configuration> rlist = new ArrayList<>();

        for(Configuration c : conf.configurationMappings.values()){
            rlist.add(c);
            this.deploymentServiceProvider.deploy(c);
        }
        configurations.put(conf.tag,rlist);
 	}
    public void configureViews(LobbyConfiguration conf){
 	    conf.views.forEach((v)->{
            this.deploymentServiceProvider.deploy(v);
        });
    }
	public OnLobby configure(LobbyConfiguration conf) throws Exception{
		DefaultLobby lb = this.setLobby(conf.descriptor);
        LobbyTypeIdIndex lobbyTypeIdIndex = new LobbyTypeIdIndex(lb.descriptor().bucket(),lb.descriptor().typeId());
        masterDataStore().load(lobbyTypeIdIndex);
        GameCluster gameCluster = new GameCluster();
        gameCluster.distributionKey(lobbyTypeIdIndex.owner);
        masterDataStore().load(gameCluster);
		OnLobby _onLobby = new OnLobbyTrack(lb.descriptor().typeId(),lb.descriptor().resetEnabled(),false,lobbyTypeIdIndex.owner(),(String) gameCluster.property(GameCluster.OWNER));
		Collections.sort(conf.applications, new DeploymentDescriptorComparator());//deploy by priority
        for (DeploymentDescriptor c : conf.applications) {
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
    public synchronized void setOnLobby(String typeId,OnLobby.Listener listener){
        if(this._lobbyMapping.containsKey(typeId)){
            return;
        }
        List<LobbyDescriptor> bList = this.query(new String[]{this.masterDataStore().bucket(),typeId},new LobbyQuery(this.masterDataStore().bucket()));
        bList.forEach((d)->{
            if(d.typeId().equals(typeId)){
                this.setLobby(d);//
                LobbyConfiguration lc = new LobbyConfiguration();
                lc.descriptor = d;
                lc.applications = this.query(new String[]{d.distributionKey()},new ApplicationQuery(d.distributionKey()));
                lc.views = this.query(new String[]{d.distributionKey()},new OnViewQuery(d.distributionKey()));
                this.configureViews(lc);
                try{
                    OnLobby ob = this.configure(lc);
                    if(lc.descriptor.deployCode>0){
                        listener.onLobby(ob);
                    }
                }catch (Exception ex){ex.printStackTrace();}
            }
        });
    }
    public synchronized void unsetLobby(String typeId,Lobby.Listener listener){
        try{
            Lobby lb = this._lobbyMapping.remove(typeId);
            if(lb==null){
                return;
            }
            HashMap<String,Descriptor> _codeBase = new HashMap<>();
            Descriptor lab = null;
            for(Descriptor d : lb.entryList()){
                Application ap = this.availableApplicationManagers.get(d.distributionKey());
                if(d.codebase()!=null&&d.moduleName()!=null){
                    _codeBase.putIfAbsent(d.codebase(),d);
                }
                if(!d.category().equals("lobby")){ //shut down app
                    ap.shutdown();
                    this.availableApplicationManagers.remove(d.distributionKey());
                }
                else{
                    lab = d;
                }
            }
            if(lab!=null){
                Application lbb = this.availableApplicationManagers.remove(lab.distributionKey());
                lbb.shutdown();
                listener.on(lab);
            }
            _codeBase.forEach((k,v)-> listener.on(v)); //clean module class loader
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
            List<DeploymentDescriptor> apps = this.query(new String[]{lb.descriptor().distributionKey()},new ApplicationQuery(lb.descriptor().distributionKey()));
            apps.forEach((a)->{
                if(a.distributionKey().equals(applicationId)){
                    try{setApplicationManager(a,lb);}catch (Exception exx){
                        throw new RuntimeException(exx);
                    }
                }
            });
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
                if(!d.category().equals("lobby")&&d.distributionKey().equals(applicationId)){
                    lb.removeEntry(applicationId);
                    Application app = this.availableApplicationManagers.remove(applicationId);
                    app.shutdown();
                }
                if(d.codebase()!=null&&d.moduleName()!=null){
                    _codeBase.putIfAbsent(d.codebase(),d);
                }
                if(d.category().equals("lobby")){
                    lab = d;
                }
            }
            if(lb.entryList().size()==1&&(lab!=null)){//clean lobby and clean module class loaders
                this._lobbyMapping.remove(typeId);
                Application lbb = this.availableApplicationManagers.remove(lab.distributionKey());
                lbb.shutdown();
                listener.on(lab);
                _codeBase.forEach((k,v)-> listener.on(v));
            }

        }catch (Exception ex){
 	        ex.printStackTrace();
        }
    }
    @Override
    public DataStore dataStore(String name){
        DataStoreProvider dataStoreProvider = (DataStoreProvider)dataStoreProviders.get(name);
        if(dataStoreProvider==null){
            dataStoreProvider = (DataStoreProvider)this.dataStoreProviders.get(this.dataStoreMaster);
        }
        return dataStoreProvider.create(name);
    }
    public DataStore dataStore(String name,int partition){
        DataStoreProvider dataStoreProvider = (DataStoreProvider)dataStoreProviders.get(name);
        if(dataStoreProvider==null){
            dataStoreProvider = (DataStoreProvider)this.dataStoreProviders.get(this.dataStoreMaster);
        }
        return dataStoreProvider.create(name,partition);
    }
    public int partitionNumber(){
 	    return this.platformRoutingNumber;
    }

    public Connection endpoint(){
 	    return new PushEndpoint(endpointIp,endpointPort);
    }
    //list the database list on deploy service
    public DataStoreProvider dataStoreProvider(){
 	    return (DataStoreProvider) this.dataStoreProviders.get(this.dataStoreMaster);
    }
    @Override
    public ClusterProvider clusterProvider(int scope){
        return scope==Distributable.INTEGRATION_SCOPE?integrationCluster:tarantulaCluster;
    }

    public EventService eventService(int scope){
 	    return scope==Distributable.DATA_SCOPE?tarantulaCluster.publisher():integrationCluster.publisher();
    }
    @Override
    public AccessIndexService accessIndexService(){
        return (AccessIndexService)this.serviceProviders.get(AccessIndexService.NAME);
    }
    @Override
    public DeploymentServiceProvider deploymentServiceProvider(){
 	    return this.deploymentServiceProvider;
    }
    public ConcurrentHashMap<String,ServiceProvider> _dataStoreProviderMap(){
 	    return this.dataStoreProviders;
    }
    public DataStore masterDataStore(){
        return ((DataStoreProvider)this.dataStoreProviders.get(this.dataStoreMaster)).create(this.dataStoreMaster,this.partitionNumber());
    }
    public RecoverableRegistry recoverableRegistry(int registryId){
 	    return fMap.get(registryId);
    }
    public <T extends Recoverable> T query(String key,T t){
        Batch batch = this.tarantulaCluster.deployService().query(t.getClassId(),new String[]{key});
        if(batch.size>0){
            t.fromMap(SystemUtil.toMap(batch.payload));
            t.distributionKey(batch.key);
        }
        return t;
    }
    public <T extends Recoverable> List<T> query(String[] params,RecoverableFactory<T> factory){
        List<T> _slist = new ArrayList<>();
        Batch batch = this.tarantulaCluster.deployService().query(factory.registryId(),params);
        if(batch.size>0){
            T sc = factory.create();
            sc.fromMap(SystemUtil.toMap(batch.payload));
            sc.distributionKey(batch.key);
            _slist.add(sc);
            sc.owner(factory.distributionKey());
            for(int i=batch.count+1;i<batch.size;i++){
                Batch bx = this.tarantulaCluster.deployService().query(batch.batchId,i);
                T scx = factory.create();
                scx.fromMap(SystemUtil.toMap(bx.payload));
                scx.distributionKey(bx.key);
                _slist.add(scx);
                scx.owner(factory.distributionKey());
            }
        }
        return _slist;
    }
    public EndpointService endpointService(){
 	    return this.endpointService;
    }
    public TarantulaCluster tarantulaCluster(){
 	    return tarantulaCluster;
    }
    public IntegrationCluster integrationCluster(){
        return integrationCluster;
    }
    public List<Configuration> configurations(String name){
 	    return this.configurations.get(name);
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
    public boolean deployServiceProvider(ServiceProvider serviceProvider){
        try{
            this.serviceProviders.computeIfAbsent(serviceProvider.name(),(sn)->{
                serviceProvider.setup(new ServiceContextProxy(this));
                serviceProvider.waitForData();
                return serviceProvider;
            });
            return true;
        }catch (Exception ex){
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
    public String bucket(){
 	    return this.dataBucketGroup;
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
            dataStoreProviders.forEach((k,v)->{
                v.atMidnight();
            });
            availableApplicationManagers.forEach((k,v)->{
                v.atMidnight();
            });
            endpointService.atMidnight();
 	    }catch (Exception ex){
 	        ex.printStackTrace();
 	    }
        this.schedule(new MidnightCheck(this));
    }
    public TokenValidatorProvider.AuthVendor authVendor(String name){
 	    if(name.equals(OnAccess.GOOGLE)){
 	        return loadGoogleCredentials();
 	    }
 	    else if(name.equals(OnAccess.STRIPE)){
 	        return loadStripeCredentials();
        }
        else{
            return null;
        }
    }
    private AuthObject loadGoogleCredentials(){
 	    try{
            InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(this.authContext+"-google-auth.json");
            byte[] data = new byte[in.available()];
            in.read(data);
            in.close();
            GsonBuilder gb = new GsonBuilder();
            gb.registerTypeAdapter(AuthObject.class,new GoogleAuthCredentialsDeserializer());
            return gb.create().fromJson(new String(data),AuthObject.class);
 	    }catch (Exception ex){
 	        return null;
        }
    }
    private AuthObject loadStripeCredentials(){
        try{
            InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("stripe-credentials.json");
            byte[] data = new byte[in.available()];
            in.read(data);
            in.close();
            GsonBuilder gb = new GsonBuilder();
            gb.registerTypeAdapter(AuthObject.class,new StripePaymentCredentialsDeserializer());
            return gb.create().fromJson(new String(data),AuthObject.class);
        }catch (Exception ex){
            return null;
        }
    }
}
