package com.tarantula.platform.service.cluster;

import com.hazelcast.config.Config;
import com.hazelcast.config.ListenerConfig;
import com.hazelcast.core.*;
import com.hazelcast.core.Message;
import com.hazelcast.spi.MemberAttributeServiceEvent;
import com.icodesoftware.*;
import com.icodesoftware.EventListener;
import com.icodesoftware.service.*;

import com.icodesoftware.util.TarantulaExecutorServiceFactory;
import com.icodesoftware.logging.JDKLogger;
import com.tarantula.platform.*;
import com.tarantula.platform.bootstrap.ServiceBootstrap;
import com.tarantula.platform.bootstrap.TarantulaMain;
import com.tarantula.platform.event.PortableEventRegistry;
import com.tarantula.platform.service.*;
import com.tarantula.platform.service.metrics.PerformanceMetrics;
import com.tarantula.platform.service.persistence.ClusterNode;
import com.tarantula.platform.util.SystemUtil;

import java.util.*;
import java.util.concurrent.*;

public class IntegrationCluster extends TarantulaApplicationHeader implements ClusterProvider,EventService,LifecycleListener {

    private static JDKLogger log = JDKLogger.getLogger(IntegrationCluster.class);
    private final Config config;
    private final String bucket;
    private final String INDEX_MAP_PREFIX = "integration.recoverable.index.";
    private final String DATA_MAP_PREFIX = "integration.recoverable.data.";

    private HazelcastInstance _cluster;

    private final ConcurrentHashMap<String,ITopic<Event>> topicList = new ConcurrentHashMap<>();

    private final ConcurrentLinkedQueue<Event> replicationQueue = new ConcurrentLinkedQueue();
    private final ConcurrentHashMap<String,EventSubscriber> eventSubscribers = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String,BucketReceiver> bMap = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String,IntegrationClusterStore> cMap = new ConcurrentHashMap<>();
    public PartitionState[] partitionStates;

    private ExecutorService inboundEventPool;
    private int workerSize = 8;
    private final TarantulaContext tarantulaContext;

    private MultiMap<String, byte[]> mIndex;
    private IMap<byte[],byte[]> vMap;

    private DeployService deployService;
    private RecoverService recoverService;
    private CountDownLatch _integrationInstanceStarted ;
    private MetricsListener metricsListener =(n,d)->{};

    private ClusterSummary summary;
    private ClusterNode node;

    private ConcurrentHashMap<String, ReloadListener> rMap = new ConcurrentHashMap<>();

    //private ConcurrentHashMap<String,CountDownLatch> pendingNode = new ConcurrentHashMap<>();
    private CountDownLatch pendingNode = new CountDownLatch(1);

    public IntegrationCluster(final Config config,final String bucket,final TarantulaContext tcx){
        this.tarantulaContext = tcx;
        this.config = config;
        this.bucket = bucket;
        this.partitionStates = new PartitionState[this.tarantulaContext.platformRoutingNumber];
        for(int i=0;i<tarantulaContext.platformRoutingNumber;i++){
            this.partitionStates[i]=new PartitionState(i,false);
        }
        _integrationInstanceStarted = new CountDownLatch(1);
    }
    public String name(){
        return "IntegrationCluster";
    }

    public String bucket(){
        return this.bucket;
    }

    public void waitForData(){}
    public int scope(){
        return Distributable.INTEGRATION_SCOPE;
    }

    public void start() throws Exception {
        TarantulaExecutorServiceFactory.createExecutorService("integration-"+this.tarantulaContext.eventThreadPoolSetting,(pool, poolSize, rh)->{
            this.inboundEventPool = pool;
            this.workerSize = poolSize;
        });
        for(int i=0;i<this.workerSize;i++){
            EventSubscriptionWorker ese = new EventSubscriptionWorker(this,eventSubscribers,replicationQueue);
            this.inboundEventPool.execute(ese);
        }
        this.node = this.tarantulaContext.deploymentDataStoreProvider.node();
        this.summary = new ClusterSummary(config.getGroupConfig().getName());
        this.summary.register(this.node);

        config.getSerializationConfig().addPortableFactory(PortableEventRegistry.OID,new PortableEventRegistry());
        this.config.getListenerConfigs().add(new ListenerConfig(this));
        _cluster = Hazelcast.newHazelcastInstance(this.config);
        _integrationInstanceStarted.await();
        mIndex = this._cluster.getMultiMap(INDEX_MAP_PREFIX+"Master");
        vMap = this._cluster.getMap(DATA_MAP_PREFIX+"Master");
        AccessIndexService accessIndexService =_cluster.getDistributedObject(AccessIndexService.NAME,AccessIndexService.NAME);
        this.tarantulaContext.serviceProvider(accessIndexService);
        this.deployService = this._cluster.getDistributedObject(DeployService.NAME,DeployService.NAME);
        this.recoverService = this._cluster.getDistributedObject(RecoverService.NAME,RecoverService.NAME);
        new ServiceBootstrap(this.tarantulaContext._deployServiceStarted,this.tarantulaContext._storageStarted,new StorageServiceBootstrap(this.tarantulaContext),"data-store-starter",true).start();
        new ServiceBootstrap(this.tarantulaContext._accessIndexServiceStarted,this.tarantulaContext._systemServiceStarted,new SystemServiceBootstrap(this.tarantulaContext),"system-service-starter",true).start();
    }
    public void shutdown() throws Exception {
        try{
            bMap.forEach((k,b)->{
                b.shutdown();
            });
        }catch (Exception ex){
            log.error("error on event shutdown",ex);
        }
        if(_cluster!=null){
            _cluster.getLifecycleService().shutdown();
        }
    }
    public void publish(Event message){
        if(message.destination()!=null){
            ITopic<Event> _t = this.topicList.computeIfAbsent(message.destination(),(String d)-> this._cluster.getTopic(d));
            _t.publish(message);
            metricsListener.onUpdated(PerformanceMetrics.PERFORMANCE_CLUSTER_OUTBOUND_MESSAGE_COUNT,1);
        }else{
            log.warn("No destination message ->"+message);
        }
    }
    public boolean onEvent(Event event) {
        onDispatch(event);
        return false;
    }
    public void retry(String retryKey) {

    }

    public EventService publisher(){
        return this;
    }

    //key value pair
    public byte[] get(byte[] key){
        return this.vMap.get(key);
    }

    public byte[] createIfAbsent(byte[] key,byte[] value){
        byte[] ret = vMap.putIfAbsent(key,value);
        return ret!=null?ret:value;
    }
    public void set(byte[] key,byte[] value){
        this.vMap.put(key,value);
    }
    public byte[] remove(byte[] key){
        return vMap.remove(key);
    }

    //key value index list pair
    public void index(String index,byte[] key){
        mIndex.put(index,key);
    }
    public void removeIndex(String index,byte[] key){
        mIndex.remove(index,key);
    }
    public Collection<byte[]> index(String index){
        return mIndex.get(index);
    }
    public void removeIndex(String index){
        mIndex.remove(index);
    }

    public EventService subscribe(String topic, EventListener callback){
        this.eventSubscribers.computeIfAbsent(topic,(t)->{
            EventSubscriber eventSubscriber = new EventSubscriber();
            eventSubscriber.callback = callback;
            eventSubscriber.topic = this._cluster.getTopic(topic);
            this.topicList.put(topic,eventSubscriber.topic);
            String sid = eventSubscriber.topic.addMessageListener((Message<Event> m) -> this.onDispatch(m.getMessageObject()));
            eventSubscriber.registrationKey = sid;
            return eventSubscriber;
        });
        return this;
    }
    public void unsubscribe(String topic){
        EventSubscriber sub = eventSubscribers.remove(topic);
        if(sub!=null){
            ITopic<Event> top = topicList.remove(topic);
            top.removeMessageListener(sub.registrationKey);
            //log.warn("Event subscription ["+topic+"] released from integration cluster");
        }
    }

    public AccessIndexService accessIndexService(){
        return (AccessIndexService) this.tarantulaContext.serviceProvider(AccessIndexService.NAME);
    }
    public DeployService deployService(){
        return this.deployService;
    }
    public RecoverService recoverService(){
        return this.recoverService;
    }
    public <T extends ServiceProvider> T serviceProvider(String name){
        return this._cluster.getDistributedObject(name,name);
    }
    private void onDispatch(Event event){
        //dispatch event to registered callback
        this.replicationQueue.offer(event);
        metricsListener.onUpdated(PerformanceMetrics.PERFORMANCE_CLUSTER_INBOUND_MESSAGE_COUNT,1);
    }
    public void registerBucketReceiver(BucketReceiver bucketReceiver){
        BucketReceiver br = bMap.computeIfAbsent(bucketReceiver.bucket(),(b)->bucketReceiver);
        PartitionState ps = this.partitionStates[br.partition()];
        if(ps.opening){
            br.open();
            this.subscribe(br.bucket(),br);
        }
    }
    public void unregisterBucketReceiver(String bucket){
        this.unsubscribe(bucket);
        bMap.remove(bucket);
    }
    public void registerEventListener(String topic, EventListener callback){
        this.subscribe(topic,callback);
    }

    public void onMerging(){
        log.warn("Node is rebooting due to cluster split");
        new Thread(()->{
            try{
                TarantulaMain.runtime.reboot();
            }catch (Exception ex){
                log.error("Filed to reboot",ex);
            }
        }).start();
    }
    public void onPartition(int pt,boolean opening){
        //log.warn("Partition ["+pt+"] with opening ["+opening+"]");
        this.partitionStates[pt].opening = opening;
        bMap.forEach((k,v)->{
            if(v.partition()==pt&&opening){//open if closed
                if(!v.opening()){
                    v.open();
                    this.subscribe(v.bucket(),v);
                }
            }
            else if(v.partition()==pt&&(!opening)){ //release if opened
                if(v.opening()){
                    v.close();
                    this.unsubscribe(v.bucket());
                }
            }
        });
    }

    public RoutingKey routingKey(String magicKey,String tag){
        return this.routingKey(magicKey,tag,routingNumber(magicKey));
    }
    public RoutingKey routingKey(String magicKey,String tag,int routingNumber){
        int _ix = magicKey.indexOf(Recoverable.PATH_SEPARATOR);
        return new ServiceRoutingKey(_ix!=-1?magicKey.substring(0,magicKey.indexOf("/")):magicKey,tag,routingNumber);
    }

    private int routingNumber(String magicKey){
        return SystemUtil.partition(magicKey,this.tarantulaContext.platformRoutingNumber);
    }
    public void registerMetricsListener(MetricsListener metricsListener){
        if(metricsListener == null) return;
        this.metricsListener = metricsListener;
    }

    @Override
    public void stateChanged(LifecycleEvent state) {
        LifecycleEvent.LifecycleState cs = state.getState();
        log.warn("Integration cluster state changed->"+state);
        switch(cs){
            case STARTED:
                _integrationInstanceStarted.countDown();
                break;
            case MERGING:
                log.warn("Integration cluster state merging->"+state);
                break;
            case MERGED:
                log.warn("Integration cluster state merged->"+state);
                break;
            case MERGE_FAILED:
                log.warn("Integration cluster state merged faied->"+state);
                break;
            default:
        }
    }

    public String registerReloadListener(ReloadListener listener){
        String regKey = UUID.randomUUID().toString();
        rMap.put(regKey,listener);
        return regKey;
    }
    public void unregisterReloadListener(String regKey){
        rMap.remove(regKey);
    }
    public void onReload(int partition,boolean localMember){
        rMap.forEach((k,v)->v.reload(partition,localMember));
    }

    public ClusterStore clusterStore(String name){
        if(name.equals("Master")) throw new RuntimeException("Master is reserved for system level cluster store");
        return cMap.computeIfAbsent(name,k->{
            MultiMap<String, byte[]> mIndex = _cluster.getMultiMap(INDEX_MAP_PREFIX+name);
            IMap<byte[],byte[]> vMap = _cluster.getMap(DATA_MAP_PREFIX+name);
            IntegrationClusterStore integrationClusterStore = new IntegrationClusterStore(mIndex,vMap);
            return integrationClusterStore;
        });
    }

    public ClusterSummary summary(){
        return this.summary;
    }

    public void registerNode(Node node) throws Exception{

        _cluster.getCluster().getLocalMember().setStringAttribute("nodeName",node.nodeName());
    }
    public void onNodeRegistered(MemberAttributeServiceEvent memberAttributeServiceEvent){
        log.warn("Member->"+memberAttributeServiceEvent.getMember().getUuid()+">>>"+memberAttributeServiceEvent.getValue());
    }

}
