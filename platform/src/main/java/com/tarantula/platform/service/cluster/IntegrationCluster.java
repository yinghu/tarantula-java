package com.tarantula.platform.service.cluster;

import com.hazelcast.config.Config;
import com.hazelcast.config.ListenerConfig;
import com.hazelcast.core.*;
import com.hazelcast.core.Message;
import com.hazelcast.spi.MemberAttributeServiceEvent;
import com.hazelcast.spi.MembershipServiceEvent;
import com.icodesoftware.*;
import com.icodesoftware.EventListener;
import com.icodesoftware.service.*;

import com.icodesoftware.util.TarantulaExecutorServiceFactory;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.*;
import com.tarantula.platform.bootstrap.ServiceBootstrap;
import com.tarantula.platform.bootstrap.TarantulaMain;
import com.tarantula.platform.event.PortableEventRegistry;
import com.tarantula.platform.service.*;
import com.tarantula.platform.service.metrics.PerformanceMetrics;
import com.tarantula.platform.service.persistence.ClusterNode;
import com.tarantula.platform.util.SystemUtil;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

public class IntegrationCluster extends TarantulaApplicationHeader implements ClusterProvider,EventService,LifecycleListener {

    private static JDKLogger log = JDKLogger.getLogger(IntegrationCluster.class);
    private static String PENDING_EVENT_NUMBER = "pendingEventNumber";
    private final Config config;
    private final String bucket;
    private final String INDEX_MAP_PREFIX = "tarantula.index.";
    private final String DATA_MAP_PREFIX = "tarantula.map.";
    private final String DATA_QUEUE_PREFIX = "tarantula.queue.";


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
    //private IQueue<byte[]> vQueue;

    private AccessIndexService accessIndexService;
    private DeployService deployService;
    private RecoverService recoverService;
    private CountDownLatch _integrationInstanceStarted ;
    private MetricsListener metricsListener =(n,d)->{};

    private ClusterSummary summary;


    private ConcurrentHashMap<String, ReloadListener> rMap = new ConcurrentHashMap<>();

    private LinkedList<Node> roundRobinQueue = new LinkedList<>();


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
        int partitionCount = Integer.parseInt(config.getProperties().getProperty("hazelcast.partition.count"));
        this.summary = new ClusterSummary(config.getGroupConfig().getName(),partitionCount);
        config.getSerializationConfig().addPortableFactory(PortableEventRegistry.OID,new PortableEventRegistry());
        this.config.getListenerConfigs().add(new ListenerConfig(this));
        _cluster = Hazelcast.newHazelcastInstance(this.config);
        _integrationInstanceStarted.await();
        //mIndex = this._cluster.getMultiMap(INDEX_MAP_PREFIX+"Master");
        vMap = this._cluster.getMap(DATA_MAP_PREFIX+"Master");
        //vQueue = this._cluster.getQueue(DATA_QUEUE_PREFIX+"Master");
        this.accessIndexService =_cluster.getDistributedObject(AccessIndexService.NAME,AccessIndexService.NAME);
        this.accessIndexService.setup(this.tarantulaContext);
        this.deployService = this._cluster.getDistributedObject(DeployService.NAME,DeployService.NAME);
        this.deployService.setup(this.tarantulaContext);
        this.recoverService = this._cluster.getDistributedObject(RecoverService.NAME,RecoverService.NAME);
        this.recoverService.setup(this.tarantulaContext);
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
        return this.accessIndexService;
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
        this.recoverService.registerMetricsListener(this.metricsListener);
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

    public ClusterStore clusterStore(String size,String name,boolean map,boolean index,boolean queue){
        if(name.equals("Master")) throw new RuntimeException("Master is reserved for system level cluster store");
        if( !map && !index && !queue) throw new RuntimeException("Empty Store is not supported");
        return cMap.computeIfAbsent(name,k->{
            IMap<byte[],byte[]> vMap = null;
            MultiMap<String, byte[]> mIndex = null;
            IQueue<byte[]> vQueue = null;
            if(map){
                //log.warn(DATA_MAP_PREFIX+size+name);
                vMap = _cluster.getMap(DATA_MAP_PREFIX+size+name);
            }
            if(index) {
                //log.warn(INDEX_MAP_PREFIX+name);
                mIndex = _cluster.getMultiMap(INDEX_MAP_PREFIX + name);
            }
            if(queue) {
                //log.warn(DATA_QUEUE_PREFIX+size+name);
                vQueue = _cluster.getQueue(DATA_QUEUE_PREFIX + size + name);
            }
            return  new IntegrationClusterStore(this,name,mIndex,vMap,vQueue,TarantulaContext.operationTimeout);
        });
    }
    public ClusterStore clusterStore(String size,String name){
        return clusterStore(ClusterStore.SMALL,name,true,true,true);
    }

    public void closeClusterStore(String name){
        cMap.remove(name);
    }
    public ClusterSummary summary(){
        return this.summary;
    }

    public void registerNode(Node node){
        ClusterNode cnode = (ClusterNode) node;
        cnode.startTime = TimeUtil.toUTCMilliseconds(LocalDateTime.now());
        cnode.memberId = _cluster.getCluster().getLocalMember().getUuid();
        cnode.address = _cluster.getCluster().getLocalMember().getAddress().getHost();
        byte[] ret = this.vMap.putIfAbsent(cnode.nodeId().getBytes(),cnode.toBinary());
        if(ret != null) throw new RuntimeException("Node ["+node.nodeName()+"] already has been registered");
        _cluster.getCluster().getLocalMember().setStringAttribute("node",node.nodeName()+"#"+node.nodeId());
    }
    public void onNodeRegistered(MemberAttributeServiceEvent mEvent){
        String[] node = mEvent.getValue().toString().split("#");
        String nodeName = node[0];
        String nodeId = node[1];
        String memberId = mEvent.getMember().getUuid();
        log.warn("Member ["+memberId+"] joined on node ["+nodeName+":"+nodeId+"]");
        this.vMap.putIfAbsent(memberId.getBytes(),nodeId.getBytes()); //memberId => nodeId index
        summary.register(fromCluster(nodeId));
        for(int i=0;i<10;i++){
            try{
                for(Member m : _cluster.getCluster().getMembers()){
                    if(!m.localMember()){
                        String[] pnode = m.getStringAttribute("node").split("#");
                        Node exstingNode = fromCluster(pnode[1]);
                        if(exstingNode != null) this.summary.register(fromCluster(pnode[1]));
                    }
                }
                break;
            }catch (Exception ex){
                if(i == 9) {
                    log.warn("Cluster going to shutdown due to member not ready after 10 retries");
                    _cluster.getCluster().shutdown();
                }else{
                    log.warn("Waiting pending registering nodes ...");
                    try { Thread.sleep(5000); }catch (Exception ignore){}
                }
            }
        }
    }

    public void onNodeRemoved(MembershipServiceEvent mEvent){
        String memberId = mEvent.getMember().getUuid();
        synchronized (roundRobinQueue){
            roundRobinQueue.remove(new ClusterNode(memberId));
        }
        String[] node = mEvent.getMember().getStringAttribute("node").split("#");
        log.warn("Member ["+memberId+"] left from node ["+node[0]+":"+node[1]+"]");
        this.summary.unregister(new ClusterNode("",node[0],tarantulaContext.platformRoutingNumber));
        this.vMap.remove(node[1].getBytes());//remove nodeId = > node
        this.vMap.remove(memberId.getBytes()); //remove member =>  nodeId
    }
    public void onNodeAdded(String memberId){
        synchronized (roundRobinQueue){
            roundRobinQueue.offer(new ClusterNode(memberId));
        }
    }
    public Node roundRobinMember(){
        synchronized (roundRobinQueue){
            Node node = roundRobinQueue.poll();
            if(node!=null) roundRobinQueue.offer(node);
            return node;
        }
    }
    private Node fromCluster(String nodeId){
        Node n = new ClusterNode();
        byte[] ret = this.vMap.get(nodeId.getBytes());
        if(ret==null) return null;
        n.fromBinary(ret);
        return n;
    }
    @Override
    public void registerSummary(ServiceProvider.Summary summary){
        summary.registerCategory(PENDING_EVENT_NUMBER);
    }
    @Override
    public void updateSummary(ServiceProvider.Summary summary){
        summary.update(PENDING_EVENT_NUMBER,replicationQueue.size());
    }

}
