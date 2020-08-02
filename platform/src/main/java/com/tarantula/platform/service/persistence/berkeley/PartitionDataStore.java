package com.tarantula.platform.service.persistence.berkeley;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.OperationStatus;
import com.tarantula.*;
import com.tarantula.logging.JDKLogger;
import com.tarantula.platform.IndexSet;

import com.tarantula.platform.service.DataStoreProvider;
import com.tarantula.platform.service.persistence.DataStoreOnPartition;
import com.tarantula.platform.service.persistence.MapStoreListener;
import com.tarantula.platform.service.persistence.ReplicatedDataStore;
import com.tarantula.platform.util.SystemUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
//data scope data store
public class PartitionDataStore extends ReplicatedDataStore{
    //public static String VERSION_NAME = "_v_";
    private final DataStoreOnPartition[] partitions;
    private final int partition;
    private final String bucket;
    private final String node;
    private final String prefix;

    private final MapStoreListener mapStoreListener;
    private ConcurrentHashMap<Integer,Listener> rMap = new ConcurrentHashMap<>();

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
            byte[] key = okey.getBytes();
            DataStoreOnPartition dso = this.partitions[SystemUtil.partition(key,partition)];
            byte[] value = SystemUtil.toJson(t.toMap());
            boolean suc = _put(dso,key,value);
            if(suc){
                //do backup and replication
                if(t.backup()){//backing up onto database
                    this.mapStoreListener.onCreating(dso.metadata,okey,t);
                }
                if(t.distributable()){//distributing onto cluster
                    this.mapStoreListener.onDistributing(dso.metadata,key,value);
                }
                Listener listener = rMap.get(t.getFactoryId());
                if(listener!=null){
                    listener.onCreated(t,key,value);
                }
                if(t.onEdge()&&t.owner()!=null&&t.label()!=null){
                    suc = onEdge(t,okey);
                }
            }
            return suc;
        }catch (Exception ex){
            log.error("Error on create",ex);
            return false;
        }
        finally {
            pass.release();
        }
    }
    private <T extends Recoverable> boolean onEdge(T t,String okey){
        IndexSet indexSet = new IndexSet();
        indexSet.distributionKey(t.owner());
        indexSet.label(t.label());
        byte[] _kn = indexSet.key().asString().getBytes();
        DataStoreOnPartition dos = this.partitions[SystemUtil.partition(_kn,partition)];
        byte[] ix = _get(dos,_kn);
        if(ix!=null){
            indexSet.fromMap(SystemUtil.toMap(ix));
        }
        else{
            ix = mapStoreListener.onRecovering(dos.metadata,_kn);
            if(ix!=null){
                indexSet.fromMap(SystemUtil.toMap(ix));
            }
        }
        indexSet.keySet.add(okey);
        byte[] _vn = SystemUtil.toJson(indexSet.toMap());
        boolean suc = _put(dos,_kn,_vn);
        if(suc){
            //do backup and replication
            if(t.backup()){
                if(ix!=null){
                    this.mapStoreListener.onUpdating(dos.metadata,indexSet.key().asString(),indexSet);
                }
                else{
                    this.mapStoreListener.onCreating(dos.metadata,indexSet.key().asString(),indexSet);
                }
            }
            if(t.distributable()){
                this.mapStoreListener.onDistributing(dos.metadata,_kn,_vn);
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
            byte[] key = akey.getBytes();
            DataStoreOnPartition dso = partitions[SystemUtil.partition(key,partition)];
            byte[] value = SystemUtil.toJson(t.toMap());
            if(_put(dso,key,value)){
                if(t.backup()){
                    this.mapStoreListener.onUpdating(dso.metadata,akey,t);
                }
                if(t.distributable()){
                    this.mapStoreListener.onDistributing(dso.metadata,key,value);
                }
                Listener listener = rMap.get(t.getFactoryId());
                if(listener!=null){
                    listener.onUpdated(t,key,value);
                }
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
            byte[] key = akey.getBytes();
            DataStoreOnPartition dso = this.partitions[SystemUtil.partition(key,partition)];
            byte[] v = _get(dso,key);
            if(v==null&&t.distributable()){
                v = mapStoreListener.onRecovering(dso.metadata,key);
            }
            if(v==null){
                byte[] vx = SystemUtil.toJson(t.toMap());
                if(_put(dso,key,vx)) {
                    if(t.backup()){
                        this.mapStoreListener.onCreating(dso.metadata,akey,t);
                    }
                    if(t.distributable()){
                        this.mapStoreListener.onDistributing(dso.metadata, key,vx);
                    }
                    Listener listener = rMap.get(t.getFactoryId());
                    if(listener!=null){
                        listener.onCreated(t,key,vx);
                    }
                    if(t.onEdge()&&t.owner()!=null&&t.label()!=null){
                        onEdge(t, akey);
                    }
                    return true;
                }
                return false;
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
            byte[] key = akey.getBytes();
            byte[] value;
            DataStoreOnPartition dso = partitions[SystemUtil.partition(key,partition)];
            if((value=_get(dso,key))==null){
                if((value=mapStoreListener.onRecovering(dso.metadata,key))==null){
                    return false;
                }
                _put(dso,key,value);
            }
            Map<String,Object> _map = SystemUtil.toMap(value);
            t.fromMap(_map);
            return true;
        }catch (Exception ex){
            log.error("Error on load",ex);
            return false;
        }
    }
    /**
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
    }**/
    //public void put(byte[] key,byte[] value){
        //_put(this.partitions[SystemUtil.partition(key,partition)],key,value);
    //}
    @Override
    public void set(byte[] key, byte[] value) {
        try{
            pass.acquire();
            int pt = SystemUtil.partition(key,partition);
            if(_put(this.partitions[pt],key,value)){
                //this.mapStoreListener.onUpdated(new RecoverableMetadata(this.prefix,this.scope(),0,0,pt,true),key,value);
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
            byte[] owner = akey.getBytes();
            DataStoreOnPartition dso = partitions[SystemUtil.partition(owner,partition)];
            byte[] edgeList = _get(dso,owner);
            if(edgeList==null){
                return;
            }
            IndexSet indexSet = new IndexSet();
            indexSet.fromMap(SystemUtil.toMap(edgeList));
            for(String b: indexSet.keySet){
                T t = query.create();
                byte[] v;
                byte[] ka = b.getBytes();
                if((v=_get(partitions[SystemUtil.partition(ka,partition)],ka))!=null){
                    t.fromMap(SystemUtil.toMap(v));
                    t.distributionKey(b);
                    if(!stream.on(t)){
                        break;
                    }
                }
            }
        }catch (Exception ex){
            log.error("Error on list",ex);
        }
    }
    public Backup backup(){
        return this;
    }
    private boolean _put(DataStoreOnPartition dso,byte[] key,byte[] value){
        return dso.database.put(null,new DatabaseEntry(key),new DatabaseEntry(value))==OperationStatus.SUCCESS;
    }
    public void registerListener(int registerId,Listener listener){
        rMap.putIfAbsent(registerId,listener);
    }
    private byte[] _get(DataStoreOnPartition dso,byte[] key){
        DatabaseEntry ve = new DatabaseEntry();
        OperationStatus status = dso.database.get(null,new DatabaseEntry(key),ve,null);
        return status==OperationStatus.SUCCESS?ve.getData():null;
    }
}
