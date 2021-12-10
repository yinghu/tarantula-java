package com.tarantula.platform.service.cluster;

import com.hazelcast.config.Config;
import com.hazelcast.config.ListenerConfig;
import com.hazelcast.core.*;
import com.hazelcast.core.Message;
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
import com.tarantula.platform.util.SystemUtil;
import com.icodesoftware.util.JsonUtil;

import java.util.*;
import java.util.concurrent.*;

public class IntegrationCluster extends TarantulaApplicationHeader implements ClusterProvider,EventService,LifecycleListener {

    private static JDKLogger log = JDKLogger.getLogger(IntegrationCluster.class);
    private final Config config;
    private final String bucket;
    private final String INDEX_MAP = "integration.recoverable.index";
    private final String DATA_MAP = "integration.recoverable.data";

    private HazelcastInstance _cluster;

    private final ConcurrentHashMap<String,ITopic<Event>> topicList = new ConcurrentHashMap<>();

    private final ConcurrentLinkedQueue<Event> replicationQueue = new ConcurrentLinkedQueue();
    private final ConcurrentHashMap<String,EventSubscriber> eventSubscribers = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String,BucketReceiver> bMap = new ConcurrentHashMap<>();
    public PartitionState[] partitionStates;

    private ExecutorService inboundEventPool;
    private int workerSize = 8;
    private final TarantulaContext tarantulaContext;

    private MultiMap<String, byte[]> mIndex;
    private IMap<byte[],byte[]> vMap;

    private DeployService deployService;
    private RecoverService recoverService;
    private CountDownLatch _integrationInstanceStarted ;
    private MetricsListener metricsListener;

    private ConcurrentHashMap<String, ReloadListener> rMap = new ConcurrentHashMap<>();


    public IntegrationCluster(final Config config,final String bucket,final TarantulaContext tcx){
        this.config = config;
        this.bucket = bucket;
        this.tarantulaContext = tcx;
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
        config.getSerializationConfig().addPortableFactory(PortableEventRegistry.OID,new PortableEventRegistry());
        this.config.getListenerConfigs().add(new ListenerConfig(this));
        _cluster = Hazelcast.newHazelcastInstance(this.config);
        _integrationInstanceStarted.await();
        mIndex = this._cluster.getMultiMap(INDEX_MAP);
        vMap = this._cluster.getMap(DATA_MAP);
        AccessIndexService accessIndexService =_cluster.getDistributedObject(AccessIndexService.NAME,AccessIndexService.NAME);
        this.tarantulaContext.serviceProvider(accessIndexService);
        this.deployService = this._cluster.getDistributedObject(DeployService.NAME,DeployService.NAME);
        this.recoverService = this._cluster.getDistributedObject(RecoverService.NAME,RecoverService.NAME);
        new ServiceBootstrap(this.tarantulaContext._deployServiceStarted,this.tarantulaContext._storageStarted,new StorageServiceBootstrap(this.tarantulaContext),"data-store-starter",true).start();
        new ServiceBootstrap(this.tarantulaContext._storageStarted,this.tarantulaContext._systemServiceStarted,new SystemServiceBootstrap(this.tarantulaContext),"system-service-starter",true).start();
        this.metricsListener = (k,v)->{};
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
            metricsListener.onUpdated(Metrics.EVENT_OUT_COUNT,1);
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
                t.fromMap(JsonUtil.toMap(v));
                t.distributionKey(new String(k));
                if(!stream.on(t)){
                    break;
                }
            }
        }
    }
    public void set(Metadata metadata, byte[] key, byte[] value){
        this.vMap.put(key,value);
        if(metadata.index()!=null){
            mIndex.put(metadata.index(),key);
        }
    }
    public byte[] get(byte[] key){
        return this.vMap.get(key);
    }
    public <T extends Recoverable> boolean load(T t){
        byte[] k = t.key().asString().getBytes();
        byte[] v = get(k);
        if(v!=null){
            t.fromMap(JsonUtil.toMap(v));
            return true;
        }
        else{
            return false;
        }
    }
    public byte[] createIfAbsent(byte[] key,byte[] value){
        byte[] ret = vMap.putIfAbsent(key,value);
        return ret!=null?ret:value;
    }
    public void set(byte[] key,byte[] value){
        this.vMap.put(key,value);
    }
    public void index(String index,byte[] key){
        mIndex.lock(index);
        mIndex.put(index,key);
        mIndex.unlock(index);
    }
    public byte[] firstIndex(String index){
        byte[] ret = null;
        mIndex.lock(index);
        Iterator<byte[]> it = mIndex.get(index).iterator();
        if(it.hasNext()){
            ret = it.next();
            mIndex.remove(index,ret);
        }
        mIndex.unlock(index);
        return ret;
    }
    public void removeIndex(String index){
        mIndex.lock(index);
        mIndex.remove(index);
        mIndex.unlock(index);
    }
    public byte[] remove(byte[] key){
        return vMap.remove(key);
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
        metricsListener.onUpdated(Metrics.EVENT_IN_COUNT,1);
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
        this.metricsListener = metricsListener;
    }

    @Override
    public void stateChanged(LifecycleEvent state) {
     LifecycleEvent.LifecycleState cs = state.getState();
        log.warn("Integration state changed->"+state);
        switch(cs){
            case STARTED:
                _integrationInstanceStarted.countDown();
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

    public String registerReloadListener(ReloadListener listener){
        String regKey = UUID.randomUUID().toString();
        rMap.put(regKey,listener);
        return regKey;
    }
    public void unregisterReloadListener(String regKey){
        rMap.remove(regKey);
    }
    public void onReload(){
        rMap.forEach((k,v)->v.reload());
    }

}
