package com.tarantula.platform.service.persistence.berkeley;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DiskOrderedCursor;
import com.sleepycat.je.OperationStatus;
import com.tarantula.*;
import com.tarantula.logging.JDKLogger;
import com.tarantula.platform.event.MapStoreSyncEvent;
import com.tarantula.platform.service.DataStoreProvider;
import com.tarantula.platform.service.persistence.DataStoreOnPartition;
import com.tarantula.platform.service.persistence.MapStoreListener;
import com.tarantula.platform.service.persistence.RecoverableMetadata;
import com.tarantula.platform.service.persistence.ReplicatedDataStore;
import com.tarantula.platform.util.SystemUtil;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

public class PartitionDataStore extends ReplicatedDataStore{
    private static String ENCODING = "UTF-8";
    private final DataStoreOnPartition[] partitions;
    private final int partition;
    private final String bucket;
    private final String node;
    private final String prefix;

    private final MapStoreListener mapStoreListener;
    private ConcurrentHashMap<Integer,RecoverableListener> rMap = new ConcurrentHashMap<>();

    private static TarantulaLogger log = JDKLogger.getLogger(PartitionDataStore.class);

    private final Semaphore pass = new Semaphore(DataStoreProvider.CONCURRENCY_ACCESS_LIMIT);

    public PartitionDataStore(int partition, String bucket, String node, String prefix, Database[] shards,MapStoreListener mapStoreListener){
        this.partition = partition;
        this.bucket = bucket;
        this.node = node;
        this.prefix = prefix;

        this.mapStoreListener = mapStoreListener;
        this.partitions = new DataStoreOnPartition[this.partition];
        for(int i=0;i<this.partition;i++){
            this.partitions[i]=new DataStoreOnPartition(i,shards[i]);
        }
    }
    @Override
    public String bucket() {
        return this.bucket;
    }

    @Override
    public String node() {
        return node;
    }
    @Override
    public String name(){
        return prefix;
    }
    @Override
    public long count() {
        long ct = 0;
        for(DataStoreOnPartition dso: partitions){
            ct +=dso.database.count();
        }
        return ct;
    }

    public void close(){
        for(DataStoreOnPartition dso : partitions){
            dso.database.close();
        }
    }
    public int scope(){
        return Distributable.DATA_SCOPE;
    }
    @Override
    public <T extends Recoverable> boolean create(T t) {
        try {
            pass.acquire();
            String okey = t.key().asString();
            if (okey == null) {
                //use bucket/oid as the key
                t.bucket(this.bucket);
                t.oid(SystemUtil.oid());
                okey = t.key().asString();
            }
            byte[] key = okey.getBytes(ENCODING);
            byte[] value = SystemUtil.toJson(t.toMap());
            return _set(t,key,value);
        }catch (Exception ex){
            log.error("Error on create",ex);
            return false;
        }
        finally {
            pass.release();
        }
    }

    @Override
    public <T extends Recoverable> int create(T[] tb) {
        int suc =0;
        for(T t : tb){
            if(this.create(t)){
                suc++;
            }
        }
        return suc;
    }

    @Override
    public <T extends Recoverable> boolean update(T t) {
        try{
            pass.acquire();
            String akey = t.key().asString();
            if(akey==null){
                return false;
            }
            byte[] key = akey.getBytes(ENCODING);
            DataStoreOnPartition dso = partitions[SystemUtil.partition(key,partition)];
            Map<String,Object> _map = t.toMap();
            byte[] value = SystemUtil.toJson(_map);
            if(_put(dso,key,value)){
                this.mapStoreListener.onUpdated(new RecoverableMetadata(this.prefix,t.scope(),t.getFactoryId(),t.getClassId(),dso.partition,false),key,value);
                return true;
            }
            else{
                return false;
            }
        }catch (Exception ex){
            log.error("Error on update",ex);
            return false;
        }
        finally {
            pass.release();
        }
    }

    @Override
    public <T extends Recoverable> boolean createIfAbsent(T t, boolean loading) {
        try{
            pass.acquire();
            String akey = t.key().asString();
            if(akey==null){
                t.bucket(this.bucket);
                t.oid(SystemUtil.oid());
                akey = t.key().asString();
            }
            byte[] key = akey.getBytes(ENCODING);
            byte[] v;
            if((v=_get(partitions[SystemUtil.partition(key,partition)],key))==null){
                return _set(t,key,SystemUtil.toJson(t.toMap()));
            }
            else{
                if(loading){
                    t.fromMap(SystemUtil.toMap(v));
                }
                return false;
            }
        }catch (Exception ex){
            log.error("Error on createIfAbsent",ex);
            return false;
        }
        finally {
            pass.release();
        }
    }

    @Override
    public <T extends Recoverable> boolean load(T t) {
        try{
            String akey = t.key().asString();
            if(akey==null){
                return false;
            }
            byte[] key = akey.getBytes(ENCODING);
            byte[] value;
            if((value=_get(partitions[SystemUtil.partition(key,partition)],key))==null){
                return false;
            }
            Map<String,Object> _map = SystemUtil.toMap(value);
            t.fromMap(_map);
            return true;
        }catch (Exception ex){
            log.error("Error on load",ex);
            return false;
        }
    }

    @Override
    public void traverse(Overflow overflow) {
        for(DataStoreOnPartition dso: partitions){
            _traverse(dso,overflow);
        }
    }
    private void _traverse(DataStoreOnPartition dso,Overflow overflow) {
        DiskOrderedCursor cursor = dso.database.openCursor(null);
        try{
            DatabaseEntry pk = new DatabaseEntry();
            DatabaseEntry pv = new DatabaseEntry();
            do{
                if(cursor.getNext(pk,pv,null)==OperationStatus.SUCCESS){
                    if(!overflow.on(this.prefix,dso.partition,pk.getData(),pv.getData())){
                        break;
                    }
                }
                else{
                    break;
                }
            }while (true);
        }finally {
            cursor.close();
        }
    }


    public void batch(byte[] key, Overflow overflow) {

    }
    public void put(byte[] key,byte[] value){
        _put(this.partitions[SystemUtil.partition(key,partition)],key,value);
    }
    @Override
    public void set(byte[] key, byte[] value) {
        try{
            pass.acquire();
            int pt = SystemUtil.partition(key,partition);
            if(_put(this.partitions[pt],key,value)){
                this.mapStoreListener.onUpdated(new RecoverableMetadata(this.prefix,this.scope(),0,0,pt,true),key,value);
            }
        }
        catch (Exception ex){
            log.error("Error on set",ex);
        }
        finally {
            pass.release();
        }
    }
    public byte[] get(byte[] key){
        int pt = SystemUtil.partition(key,partition);
        return _get(this.partitions[pt],key);
    }
    @Override
    public <T extends Recoverable> List<T> list(RecoverableFactory<T> query) {
        List<T> alist = new ArrayList<>();
        this.list(query,(t)->{
            alist.add(t);
            return true;
        });
        return alist;
    }

    @Override
    public <T extends Recoverable> void list(RecoverableFactory<T> query, Stream<T> stream) {
        try {
            String akey = (query.distributionKey() + Recoverable.PATH_SEPARATOR + query.label());
            byte[] owner = akey.getBytes(ENCODING);
            DataStoreOnPartition dso = partitions[SystemUtil.partition(owner,partition)];
            byte[] edgeList = _get(dso,owner);
            if(edgeList==null){
                return;
            }
            ByteBuffer key = ByteBuffer.allocate(100);
            boolean continuing = true;
            for(byte b: edgeList){
                if(b!=','){
                    key.put(b);
                }
                else{
                    key.flip();
                    byte[] ka = new byte[key.limit()];
                    key.get(ka);
                    T t = query.create();
                    byte[] v;
                    if((v=_get(partitions[SystemUtil.partition(ka,partition)],ka))!=null){
                        t.fromMap(SystemUtil.toMap(v));
                        t.distributionKey(new String(ka,ENCODING));
                        if(!stream.on(t)){
                            continuing = false;
                            break;
                        }
                    }
                    key.clear();
                }
            }
            if(continuing){
                key.flip();
                if(key.limit()>0){
                    byte[] ka = new byte[key.limit()];
                    key.get(ka);
                    T t = query.create();
                    byte[] v;
                    if((v=_get(partitions[SystemUtil.partition(ka,partition)],ka))!=null){
                        t.fromMap(SystemUtil.toMap(v));
                        t.distributionKey(new String(ka,ENCODING));
                        stream.on(t);
                    }
                }
            }

        }catch (Exception ex){
            log.error("Error on list",ex);
        }
    }


    @Override
    public RecoverableListener registerRecoverableListener(RecoverableListener recoverableListener) {
        return rMap.computeIfAbsent(recoverableListener.registryId(),(rid)->recoverableListener);
    }

    @Override
    public void unregisterRecoverableListener(int factoryId) {
        rMap.remove(factoryId);
    }

    @Override
    public void onReplication(MapStoreSyncEvent msd) {
        Metadata mt = msd.metadata;
        if(!msd.source().equals(this.node)){
            _put(partitions[mt.partition()],msd.key,msd.payload());
        }
        if(!mt.onEdge()){
            //callback on local application register
            RecoverableListener rl = rMap.get(mt.factoryId());
            if(rl!=null){
                rl.onUpdated(mt,msd.key,msd.payload());
            }
        }

    }
    private <T extends Recoverable> boolean _set(T t,byte[] key,byte[] value) throws Exception{
        DataStoreOnPartition dso = partitions[SystemUtil.partition(key,partition)];
        if(_put(dso,key,value)){
            this.mapStoreListener.onUpdated(new RecoverableMetadata(this.prefix,t.scope(),t.getFactoryId(),t.getClassId(),dso.partition,false),key,value);
            if(t.onEdge()){
                byte[] owner = (t.owner()+Recoverable.PATH_SEPARATOR+t.label()).getBytes(ENCODING);
                byte[] v;
                DataStoreOnPartition edo = partitions[SystemUtil.partition(owner,partition)];
                if((v= _get(edo,owner))!=null){
                    //append
                    ByteBuffer buffer = ByteBuffer.allocate(v.length+key.length+1);
                    buffer.put(v).put((byte)',').put(key);
                    v = buffer.array();
                }
                else{
                    //new edge
                    v = key;
                }
                //ignore if insert failed
                if(_put(edo,owner,v)){
                    //send replication
                    this.mapStoreListener.onUpdated(new RecoverableMetadata(this.prefix,t.scope(),t.getFactoryId(),t.getClassId(),edo.partition,true),owner,v);
                }
            }
            return true;
        }else{
            return true;
        }
    }
    private boolean _put(DataStoreOnPartition dso,byte[] key,byte[] value){
        return dso.database.put(null,new DatabaseEntry(key),new DatabaseEntry(value))==OperationStatus.SUCCESS;
    }
    private byte[] _get(DataStoreOnPartition dso,byte[] key){
        DatabaseEntry ve = new DatabaseEntry();
        OperationStatus status = dso.database.get(null,new DatabaseEntry(key),ve,null);
        if(status==OperationStatus.SUCCESS){
            return ve.getData();
        }
        else{
            return null;
        }
    }
}
