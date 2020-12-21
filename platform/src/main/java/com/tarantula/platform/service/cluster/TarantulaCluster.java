package com.tarantula.platform.service.cluster;

import java.util.*;

import com.hazelcast.config.Config;
import com.hazelcast.config.ListenerConfig;
import com.hazelcast.core.*;
import com.icodesoftware.*;
import com.icodesoftware.EventListener;
import com.icodesoftware.service.*;
import com.icodesoftware.logging.JDKLogger;

import com.tarantula.platform.*;
import com.tarantula.platform.event.PortableEventRegistry;
import com.tarantula.platform.util.SystemUtil;

public class TarantulaCluster extends TarantulaApplicationHeader implements ClusterProvider, EventService,LifecycleListener{

    private static JDKLogger log = JDKLogger.getLogger(TarantulaCluster.class);
    private final Config config;
	private HazelcastInstance _hazel;
	private final TarantulaContext _tarantulaContext;

    private String bucket;
    private final String INDEX_MAP = "tarantula.recoverable.index.Key";
    private final String VALUE_MAP = "tarantula.recoverable.data.Value";

    //cluster cache
    private MultiMap<String, byte[]> mIndex;
    private Map<byte[],byte[]> vMap;
    private String memberId;
    private DeployService deployService;
    private RecoverService recoverService;
    //private ConcurrentHashMap<String,EventListener> eMap = new ConcurrentHashMap<>();

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
    public RecoverService recoverService(){
        return this.recoverService;
    }
    public EventService subscribe(String topic, EventListener callback){
        return null;
    }
    public void unsubscribe(String topic){
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

    public EventService publisher(){
        return this._tarantulaContext.integrationCluster().publisher();
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
            t.fromMap(SystemUtil.toMap(v));
            return true;
        }
        else{
            return false;
        }
    }
    public void set(byte[] key,byte[] value){

    }
    public void index(String index,byte[] key){

    }
    public byte[] firstIndex(String index){
        return null;
    }
    public void removeIndex(String index){
    }
    public byte[] remove(byte[] key){
        return null;
    }

	public void start() throws Exception {
        //add platform portable provider from conf
        //partitionCount = Integer.parseInt(config.getProperty("hazelcast.partition.count"));
        config.getSerializationConfig().addPortableFactory(PortableEventRegistry.OID,new PortableEventRegistry());
        config.getListenerConfigs().add(new ListenerConfig(this));
        _hazel = Hazelcast.newHazelcastInstance(this.config);
        _tarantulaContext._tarantulaInstanceStarted.await();
        mIndex = this._hazel.getMultiMap(INDEX_MAP);
        vMap = this._hazel.getMap(VALUE_MAP);
        this.deployService = this._hazel.getDistributedObject(DeployService.NAME,DeployService.NAME);
        this.recoverService = this._hazel.getDistributedObject(RecoverService.NAME,RecoverService.NAME);
        memberId = _hazel.getCluster().getLocalMember().getUuid();
    }

	public void shutdown() throws Exception {
        if(this._hazel!=null){
            this._hazel.getLifecycleService().shutdown();
        }
	}


    public void publish(Event out) {
        //ITopic<Event> _t = this.topicList.computeIfAbsent(out.destination(),(String dest)-> this._hazel.getTopic(dest));
        //_t.publish(out);
        //metricsListener.onUpdated(Metrics.EVENT_OUT_COUNT,1);
    }

    public void retry(String retryKey) {

    }

    public String addEventListener(String registerId,EventListener e){
        throw new UnsupportedOperationException();
    }
    public void removeEventListener(String registerId){
    }
    public RoutingKey routingKey(String magicKey, String tag){
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
    public void registerMetricsListener(MetricsListener metricsListener){
        //this.metricsListener = metricsListener;
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
                this._tarantulaContext.integrationCluster().onMerging();
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
