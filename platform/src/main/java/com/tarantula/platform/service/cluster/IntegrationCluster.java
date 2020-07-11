package com.tarantula.platform.service.cluster;

import com.hazelcast.config.Config;
import com.hazelcast.config.ListenerConfig;
import com.hazelcast.core.*;
import com.hazelcast.core.Message;
import com.tarantula.*;

import com.tarantula.EventListener;
import com.tarantula.logging.JDKLogger;
import com.tarantula.platform.*;
import com.tarantula.platform.bootstrap.ServiceBootstrap;
import com.tarantula.platform.bootstrap.TarantulaExecutorServiceFactory;
import com.tarantula.platform.bootstrap.TarantulaMain;
import com.tarantula.platform.event.PortableEventRegistry;
import com.tarantula.platform.service.*;
import com.tarantula.platform.util.SystemUtil;

import java.util.*;
import java.util.concurrent.*;

/**
 * updated by yinghu on 4/10/2019
 */
public class IntegrationCluster extends TarantulaApplicationHeader implements ClusterProvider,EventService,LifecycleListener{

    private static JDKLogger log = JDKLogger.getLogger(IntegrationCluster.class);
    private final Config config;
    private final String bucket;
    private final String INDEX_MAP = "integration.recoverable.index.Key";
    private final String VALUE_MAP = "integration.recoverable.data.Value";
    private HazelcastInstance _cluster;

    private final ConcurrentHashMap<String,ITopic<Event>> topicList = new ConcurrentHashMap<>();

    private final ConcurrentLinkedQueue<Event> replicationQueue = new ConcurrentLinkedQueue();
    private final ConcurrentHashMap<String,EventSubscriber> eventSubscribers = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String,BucketReceiver> bMap = new ConcurrentHashMap<>();
    public PartitionState[] partitionStates;

    private final ArrayList<Closable> wlist = new ArrayList<>();

    private ExecutorService inboundEventPool;
    private int workerSize = 8;
    private int partitionCount;
    private final TarantulaContext tarantulaContext;

    private MultiMap<String, byte[]> mIndex;
    private Map<byte[],byte[]> vMap;
    private ConcurrentHashMap<Integer,RecoverableListener> rMap = new ConcurrentHashMap<>();
    private String memberId;
    private DeployService deployService;
    private ConcurrentHashMap<String,EventListener> eMap = new ConcurrentHashMap<>();

    private MetricsListener metricsListener;
    private CopyOnWriteArrayList<BucketListener> bList = new CopyOnWriteArrayList<>();
    public IntegrationCluster(final Config config,final String bucket,final TarantulaContext tcx){
        this.config = config;
        this.bucket = bucket;
        this.tarantulaContext = tcx;
        this.partitionStates = new PartitionState[this.tarantulaContext.platformRoutingNumber];
        for(int i=0;i<tarantulaContext.platformRoutingNumber;i++){
            this.partitionStates[i]=new PartitionState(i,false);
        }
    }
    public String name(){
        return "IntegrationCluster";
    }
    public String subscription(){
        return this.memberId;
    }
    public String bucket(){
        return this.bucket;
    }
    public int partitionCount(){
        return partitionCount;
    }
    public void waitForData(){}
    public int scope(){
        return Distributable.INTEGRATION_SCOPE;
    }
    public boolean onPartition(byte[] key){
        return this._cluster.getPartitionService().getPartition(key).getOwner().getUuid().equals(this.memberId);
    }
    public int size(){
        return this._cluster.getCluster().getMembers().size();
    }
    public void start() throws Exception {
        TarantulaExecutorServiceFactory.createExecutorService("integration-"+this.tarantulaContext.eventThreadPoolSetting,(pool,poolSize,rh)->{
            this.inboundEventPool = pool;
            this.workerSize = poolSize;
        });
        for(int i=0;i<this.workerSize;i++){
            EventSubscriptionWorker ese = new EventSubscriptionWorker(this,eventSubscribers,replicationQueue);
            wlist.add(ese);
            this.inboundEventPool.execute(ese);
        }
        //this.tarantulaContext.serverTopic = this.bucket;
        partitionCount = Integer.parseInt(config.getProperty("hazelcast.partition.count"));
        config.getSerializationConfig().addPortableFactory(PortableEventRegistry.OID,new PortableEventRegistry());
        this.config.getListenerConfigs().add(new ListenerConfig(this));
        _cluster = Hazelcast.newHazelcastInstance(this.config);
        this.tarantulaContext._integrationInstanceStarted.await();
        mIndex = this._cluster.getMultiMap(INDEX_MAP);
        vMap = this._cluster.getMap(VALUE_MAP);
        AccessIndexService accessIndexService =_cluster.getDistributedObject(AccessIndexService.NAME,AccessIndexService.NAME);
        this.tarantulaContext.serviceProvider(accessIndexService);
        this.memberId = this._cluster.getCluster().getLocalMember().getUuid();
        this.subscribe(this.memberId,this);
        this.deployService = this._cluster.getDistributedObject(DeployService.NAME,DeployService.NAME);this.deployService = this._cluster.getDistributedObject(DeployService.NAME,DeployService.NAME);
        this.tarantulaContext.tarantulaCluster().subscribe(this.memberId,this);
        new ServiceBootstrap(this.tarantulaContext._deployServiceStarted,this.tarantulaContext._storageStarted,new StorageServiceBootstrap(this.tarantulaContext),"data-store-starter",true).start();
        new ServiceBootstrap(this.tarantulaContext._storageStarted,this.tarantulaContext._systemServiceStarted,new SystemServiceBootstrap(this.tarantulaContext),"system-service-starter",true).start();
        this.metricsListener = (k,v)->{};
    }
    public void shutdown() throws Exception {
        try{
            this.inboundEventPool.shutdown();
            bMap.forEach((k,b)->{
                b.shutdown();
            });
            for(Closable e : wlist){
                e.close();
            }
        }catch (Exception ex){
            log.error("error on event shutdown",ex);
            this.inboundEventPool.shutdownNow();
        }
        if(_cluster!=null){
            _cluster.getLifecycleService().shutdown();
        }
    }
    public void publish(Event message){
        if(message.destination()!=null){
            ITopic<Event> _t = this.topicList.computeIfAbsent(message.destination(),(String d)-> this._cluster.getTopic(d));
            _t.publish(message);
            metricsListener.onUpdated(Metrics.EVENT_OUT_COUNT,1);
        }else{
            log.warn("No destination message ->"+message.toString());
        }
    }

    public String addEventListener(String registerId,EventListener e){
        if(registerId==null){
            String rid = UUID.randomUUID().toString();
            eMap.put(rid,e);
            return rid;
        }
        else{
            eMap.put(registerId,e);
            return registerId;
        }
    }
    public void removeEventListener(String registerId){
        eMap.remove(registerId);
    }
    @Override
    public boolean onEvent(Event event) {
        EventListener e = eMap.get(event.trackId());
        if(e!=null){
            if(e.onEvent(event)){
                eMap.remove(event.trackId());
            }
            metricsListener.onUpdated(Metrics.EVENT_IN_COUNT,1);
        }
        return false;
    }
    public void retry(String retryKey) {

    }

    public EventService publisher(){
        return this;
    }

    public <T extends Recoverable> List<T> list(RecoverableFactory<T> query){
        ArrayList<T> alist = new ArrayList<>();
        list(query,(t)->{
            alist.add(t);
            return true;
        });
        return alist;
    }
    public  <T extends Recoverable> void list(RecoverableFactory<T> query, DataStore.Stream<T> stream){
        Collection<byte[]> c = this.mIndex.get(query.distributionKey()+Recoverable.PATH_SEPARATOR+query.label());
        for(byte[] k : c){
            T t = query.create();
            byte[] v = vMap.get(k);
            if(v!=null){
                t.fromMap(SystemUtil.toMap(v));
                t.distributionKey(new String(k));
                if(!stream.on(t)){
                    break;
                }
            }
        }
    }
    public void set(Metadata metadata,byte[] key,byte[] value){
        this.vMap.put(key,value);
        if(metadata.index()!=null){
            mIndex.put(metadata.index(),key);
        }
        RecoverableListener r = rMap.get(metadata.factoryId());
        if(r!=null){
            r.onUpdated(metadata,key,value);
        }
    }
    public byte[] get(byte[] key){
        return this.vMap.get(key);
    }
    public <T extends Recoverable> boolean load(T t){
        byte[] k = t.key().asString().getBytes();
        byte[] v = get(k);
        if(v!=null){
            t.fromMap(SystemUtil.toMap(v));
            return true;
        }
        else{
            return false;
        }
    }
    public void set(byte[] key,byte[] value){
        this.vMap.put(key,value);
    }
    public void index(String index,byte[] key){
        mIndex.lock(index);
        mIndex.put(index,key);
        mIndex.unlock(index);
        //log.warn("Index->1"+index+"/"+mIndex.get(index).isEmpty());
    }
    public byte[] firstIndex(String index){
        byte[] ret = null;
        mIndex.lock(index);
        //log.warn("Index->2"+index+"/"+mIndex.get(index).isEmpty());
        Iterator<byte[]> it = mIndex.get(index).iterator();
        if(it.hasNext()){
            ret = it.next();
            mIndex.remove(index,ret);
        }
        //log.warn("Index->3"+index+"/"+mIndex.get(index).isEmpty());
        mIndex.unlock(index);
        return ret;
    }
    public byte[] remove(byte[] key){
        return vMap.remove(key);
    }
    public RecoverableListener registerRecoverableListener(RecoverableListener recoverableListener){
        return rMap.computeIfAbsent(recoverableListener.registryId(),(rid)->recoverableListener);
    }
    public void unregisterRecoverableListener(int factoryId){
        this.rMap.remove(factoryId);
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
        return this.tarantulaContext.tarantulaCluster().recoverService();
    }
    private void onDispatch(Event event){
        //dispatch event to registered callback
        this.replicationQueue.offer(event);
        metricsListener.onUpdated(Metrics.EVENT_IN_COUNT,1);
    }
    public void registerBucketReceiver(BucketReceiver bucketReceiver){
        //log.warn("["+bucketReceiver.bucket()+"] registered on cluster");
        BucketReceiver br = bMap.computeIfAbsent(bucketReceiver.bucket(),(b)->bucketReceiver);
        PartitionState ps = this.partitionStates[br.partition()];
        if(ps.opening){
            br.open();
            this.subscribe(br.bucket(),br);
        }
    }
    public void unregisterBucketReceiver(String bucket){
        //log.warn("Bucket Receiver ["+bucket+"] unregistered from integration cluster");
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
        bList.forEach((b)->{
            b.onBucket(pt,opening?BucketReceiver.OPEN:BucketReceiver.CLOSE);
        });
    }

    public RoutingKey routingKey(String magicKey,String tag){
        return this.routingKey(magicKey,tag,routingNumber(magicKey));
    }
    public RoutingKey routingKey(String magicKey,String tag,int routingNumber){
        int _ix = magicKey.indexOf(Recoverable.PATH_SEPARATOR);
        return new ServiceRoutingKey(_ix!=-1?magicKey.substring(0,magicKey.indexOf("/")):magicKey,tag,routingNumber);
    }
    public RoutingKey instanceRoutingKey(String applicationId,String instanceId){
        return new InstanceRoutingKey(applicationId,instanceId,Integer.parseInt(instanceId.substring(instanceId.lastIndexOf(Recoverable.PATH_SEPARATOR)+1)));
    }
    private int routingNumber(String magicKey){
        return SystemUtil.partition(magicKey,this.tarantulaContext.platformRoutingNumber);
    }
    public void registerMetricsListener(MetricsListener metricsListener){
        this.metricsListener = metricsListener;
    }
    public void addBucketListener(BucketListener bucketListener){
        this.bList.add(bucketListener);
    }
    @Override
    public void stateChanged(LifecycleEvent state) {
     LifecycleEvent.LifecycleState cs = state.getState();
        log.warn("Integration state changed->"+state.toString());
        switch(cs){
            case STARTED:
                TarantulaContext._integrationInstanceStarted.countDown();
                break;
            case MERGING:
                //TarantulaContext.getInstance().integrationCluster.onMerging();
                break;
            case MERGED:
                //TarantulaContext.getInstance().integrationCluster.onMerged();
                break;
            case MERGE_FAILED:
                //TarantulaContext.getInstance().integrationCluster.onMergeFailed();
                break;
            default:
        }
    }
}
