package com.tarantula.platform.service.cluster;


import java.util.*;
import java.util.concurrent.*;

import com.hazelcast.config.Config;
import com.hazelcast.config.ListenerConfig;
import com.hazelcast.core.*;
import com.hazelcast.core.Message;
import com.tarantula.*;

import com.tarantula.EventListener;
import com.tarantula.logging.JDKLogger;
import com.tarantula.platform.*;
import com.tarantula.platform.bootstrap.TarantulaExecutorServiceFactory;
import com.tarantula.platform.service.Closable;
import com.tarantula.platform.service.ClusterProvider;
import com.tarantula.platform.service.DeployService;
import com.tarantula.platform.util.SystemUtil;



public class TarantulaCluster extends TarantulaApplicationHeader implements ClusterProvider,EventService,LifecycleListener{

    private static JDKLogger log = JDKLogger.getLogger(TarantulaCluster.class);
    private final Config config;
	private HazelcastInstance _hazel;
	private final TarantulaContext _tarantulaContext;

    private final ConcurrentHashMap<String,ITopic<Event>> topicList = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String,EventSubscriber> eventSubscribers = new ConcurrentHashMap<>();

    private final ConcurrentLinkedQueue<Event> replicationPendingQueue = new ConcurrentLinkedQueue();


    private ExecutorService replicationPool;
    private int workerSize =8;
    private final ArrayList<Closable> wlist = new ArrayList<>();

    private String bucket;
    private final String INDEX_MAP = "tarantula.recoverable.index.Key";
    private final String VALUE_MAP = "tarantula.recoverable.data.Value";

    //cluster cache
    private MultiMap<String, byte[]> mIndex;
    private Map<byte[],byte[]> vMap;
    private ConcurrentHashMap<Integer,RecoverableListener> rMap = new ConcurrentHashMap<>();
    private String memberId;
    private DeployService deployService;
    private ConcurrentHashMap<String,EventListener> eMap = new ConcurrentHashMap<>();
    public TarantulaCluster(final Config config,final String bucket,final TarantulaContext tarantulaContext){
		this.config  = config;
		this.bucket = bucket;
		this._tarantulaContext = tarantulaContext;
	}

    public AccessIndexService accessIndexService(){
        return (AccessIndexService) this._tarantulaContext.serviceProvider(AccessIndexService.NAME);
    }
    public DeployService deployService(){
        return this.deployService;
    }
    public EventService subscribe(String topic, EventListener callback){
        this.eventSubscribers.computeIfAbsent(topic,(t)->{
            EventSubscriber eventSubscriber = new EventSubscriber();
            eventSubscriber.callback = callback;
            eventSubscriber.topic = this.topicList.computeIfAbsent(topic,(String dest)-> this._hazel.getTopic(dest));
            eventSubscriber.topic.addMessageListener((Message<Event> m) -> this.onQueue(m.getMessageObject()));
            return eventSubscriber;
        });
        return this;
    }
    public void unsubscribe(String topic){
        EventSubscriber sub = eventSubscribers.remove(topic);
        if(sub!=null){
            ITopic<Event> top = topicList.remove(topic);
            top.removeMessageListener(sub.registrationKey);
        }
    }
    public void registerEventListener(String topic, EventListener callback){

    }
    public String subscription(){
        return this.memberId;
    }
    public String name(){
        return "TarantulaCluster";
    }
    public String bucket(){
        return this.bucket;
    }
    public int scope(){
        return Distributable.DATA_SCOPE;
    }
    public boolean onPartition(byte[] key){
        throw new UnsupportedOperationException("on partition not support on data cluster");
        //return this.cluster.getPartitionService().getPartition(key).getOwner().getUuid().equals(this.memberId);
    }
    public int size(){
        return this._hazel.getCluster().getMembers().size();
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
                if(t.binary()){
                    t.fromByteArray(v);
                }
                else{
                    t.fromMap(SystemUtil.toMap(v));
                }
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
            if(t.binary()){
                t.fromByteArray(v);
            }
            else{
                t.fromMap(SystemUtil.toMap(v));
            }
            return true;
        }
        else{
            return false;
        }
    }
    public RecoverableListener registerRecoverableListener(RecoverableListener recoverableListener){
        return rMap.computeIfAbsent(recoverableListener.registryId(),(rid)->recoverableListener);
    }
    public void unregisterRecoverableListener(int factoryId){
        this.rMap.remove(factoryId);
    }
	public void start() throws Exception {
        TarantulaExecutorServiceFactory.createExecutorService("data-"+this._tarantulaContext.dataReplicationThreadPoolSetting,(pool,poolSize,rh)->{
            this.replicationPool = pool;
            this.workerSize = poolSize;
        });
        for(int i=0;i<this.workerSize;i++){
            EventSubscriptionWorker ese = new EventSubscriptionWorker(this,eventSubscribers,replicationPendingQueue);
            wlist.add(ese);
            this.replicationPool.execute(ese);
        }
        //add platform portable provider from conf
        this._tarantulaContext.fMap.forEach((k,r)->{
            config.getSerializationConfig().addPortableFactory(r.registryId(),new DynamicPortableRegistry(r));
        });
        config.getListenerConfigs().add(new ListenerConfig(this));
        _hazel = Hazelcast.newHazelcastInstance(this.config);
        _tarantulaContext._tarantulaInstanceStarted.await();
        mIndex = this._hazel.getMultiMap(INDEX_MAP);
        vMap = this._hazel.getMap(VALUE_MAP);
        this.deployService = this._hazel.getDistributedObject(DeployService.NAME,DeployService.NAME);
        memberId = _hazel.getCluster().getLocalMember().getUuid();
        this.subscribe(memberId,this);
    }

	public void shutdown() throws Exception {
        try{
            replicationPool.shutdown();
            for(Closable e : wlist){
                e.close();
            }
        }catch (Exception ex){
            log.error("error on event shutdown",ex);
            this.replicationPool.shutdownNow();
        }
        if(this._hazel!=null){
            this._hazel.getLifecycleService().shutdown();
        }
	}


    public void publish(Event out) {
        ITopic<Event> _t = this.topicList.computeIfAbsent(out.destination(),(String dest)-> this._hazel.getTopic(dest));
        _t.publish(out);
    }

    public void retry(String retryKey) {

    }

    @Override
    public boolean onEvent(Event event){
         EventListener e = eMap.get(event.trackId());
         if(e!=null){
            if(e.onEvent(event)){
                eMap.remove(event.trackId());
            }
         }
         return false;
    }
    public String addEventListener(String registerId,EventListener e){
        if(registerId==null){
            String rid = UUID.randomUUID().toString();
            eMap.put(rid,e);
            return rid;
        }else {
            eMap.put(registerId,e);
            return registerId;
        }

    }
    public void removeEventListener(String registerId){
        eMap.remove(registerId);
    }
    public boolean onQueue(Event event) {
        this.replicationPendingQueue.offer(event);
        return true;
    }
    public RoutingKey routingKey(String magicKey,String tag){
        return null;
    }
    public RoutingKey routingKey(String magicKey,String tag,int routingNumber){
        return null;
    }
    public RoutingKey instanceRoutingKey(String applicationId,String instanceId){
        return null;
    }
    public int routingNumber(){
        return this._tarantulaContext.platformRoutingNumber;
    }

    @Override
    public void stateChanged(LifecycleEvent state) {
        LifecycleEvent.LifecycleState cs = state.getState();
        log.warn("Data cluster state changed->"+state.toString());
        switch(cs){
            case STARTED:
                this._tarantulaContext._tarantulaInstanceStarted.countDown();
                break;
            case MERGING:
                this._tarantulaContext.integrationCluster.onMerging();
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
