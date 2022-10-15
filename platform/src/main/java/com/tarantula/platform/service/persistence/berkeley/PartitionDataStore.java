package com.tarantula.platform.service.persistence.berkeley;

import com.icodesoftware.Distributable;
import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.TarantulaLogger;
import com.sleepycat.je.*;
import com.icodesoftware.logging.JDKLogger;
import com.tarantula.platform.IndexSet;

import com.tarantula.platform.service.persistence.*;
import com.tarantula.platform.util.SystemUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class PartitionDataStore extends ReplicatedDataStore{

    private final DataBaseOnPartition[] partitions;
    private final int partition;
    private final String bucket;
    private final String node;
    private final String prefix;

    private final MapStoreListener mapStoreListener;
    private ConcurrentHashMap<Integer,Listener> rMap = new ConcurrentHashMap<>();

    private static TarantulaLogger log = JDKLogger.getLogger(PartitionDataStore.class);

    public PartitionDataStore(int partition, String bucket, String node, String prefix, Database[] shards,MapStoreListener mapStoreListener){
        this.partition = partition;
        this.bucket = bucket;
        this.node = node;
        this.prefix = prefix;
        this.mapStoreListener = mapStoreListener;
        this.partitions = new DataBaseOnPartition[this.partition];
        for(int i=0;i<this.partition;i++){
            this.partitions[i]=new DataBaseOnPartition(i,shards[i]);
            this.partitions[i].metadata = new RecoverableMetadata(this.prefix,i,Distributable.DATA_SCOPE);
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
        for(DataBaseOnPartition dso: partitions){
            ct +=dso.database.count();
        }
        return ct;
    }
    public int partitionNumber(){
        return this.partition;
    }

    public long count(int partition){
        if(partition<0||partition>=this.partition) return 0;
        return partitions[partition].database.count();
    }
    public void close(){
        for(DataBaseOnPartition dso : partitions){
            dso.database.close();
        }
    }

    @Override
    public <T extends Recoverable> boolean create(T t) {
        try {
            String akey = t.key().asString();
            if (akey == null) {
                //use bucket/oid as the key
                t.bucket(this.bucket);
                t.oid(SystemUtil.oid());
                akey = t.key().asString();
            }
            final String okey = akey;
            byte[] key = okey.getBytes();
            DataBaseOnPartition dso = this.partitions[SystemUtil.partition(key,partition)];
            boolean suc = dso.lock(key,()->{
                if(_get(dso,key) != null) return false;
                byte[] value = RevisionObject.toBinary(Long.MIN_VALUE,t.toBinary(),true);
                if(!_put(dso,key,value)) return false;
                //do backup and replication
                t.revision(Long.MIN_VALUE);
                if(t.backup()) this.mapStoreListener.onCreating(dso.metadata,okey,t);
                if(t.distributable()) this.mapStoreListener.onDistributing(dso.metadata,key,value);
                Listener listener = rMap.get(t.getFactoryId());
                if(listener!=null) listener.onCreated(t,okey,key,value);
                return true;
            });
            if(suc && t.onEdge() && t.owner() != null && t.label() !=null){
                onEdge(t,okey);
            }
            return suc;
        }catch (Exception ex){
            log.error("Error on create",ex);
            return false;
        }
    }
    private <T extends Recoverable> boolean onEdge(T t,String okey){
        IndexSet indexSet = new IndexSet();
        indexSet.distributionKey(t.owner());
        indexSet.label(t.label());
        indexSet.revision(Long.MIN_VALUE);
        byte[] _kn = indexSet.key().asString().getBytes();
        DataBaseOnPartition dso = this.partitions[SystemUtil.partition(_kn,partition)];
        return dso.lock(_kn,()->{
            RevisionObject ro = _getRevisionObject(dso,_kn);//from local
            if(ro != null && ro.local){
                indexSet.fromBinary(ro.data);
                indexSet.revision(ro.revision);
            }
            else{
                byte[] data = mapStoreListener.onRecovering(dso.metadata,_kn);//from cluster
                if( data != null) {
                    RevisionObject rd = RevisionObject.fromBinary(data);
                    indexSet.fromBinary(rd.data);
                    indexSet.revision(rd.revision);
                }
            }
            indexSet.addKey(okey);
            indexSet.revision(indexSet.revision()+1);
            byte[] _vn = RevisionObject.toBinary(indexSet.revision(),indexSet.toBinary(),true);
            if(!_put(dso,_kn,_vn)) return false;
            //do backup and replication
            if(t.backup()){
                if(indexSet.revision() > Long.MIN_VALUE){
                    this.mapStoreListener.onUpdating(dso.metadata,indexSet.key().asString(),indexSet);
                }
                else{
                    this.mapStoreListener.onCreating(dso.metadata,indexSet.key().asString(),indexSet);
                }
            }
            if(t.distributable()) this.mapStoreListener.onDistributing(dso.metadata,_kn,_vn);
            return true;
        });
    }

    @Override
    public <T extends Recoverable> boolean update(T t) {
        try{
            String akey = t.key().asString();
            if(akey==null) return false;
            byte[] key = akey.getBytes();
            DataBaseOnPartition dso = partitions[SystemUtil.partition(key,partition)];
            return dso.lock(key,()->{
                RevisionObject ro = _getRevisionObject(dso,key);
                boolean creating = ro==null;
                if(creating){
                    t.revision(Long.MIN_VALUE);
                }
                else{
                    if(ro.local && ro.revision != t.revision()) return false;
                    if(!ro.local){
                        byte[] data = this.mapStoreListener.onRecovering(dso.metadata,key);
                        if(data != null){
                            RevisionObject rd = RevisionObject.fromBinary(data);
                            _put(dso,key,RevisionObject.toBinary(rd.revision,rd.data,true));
                            if(rd.revision != t.revision()) return false;
                        }
                    }
                }
                t.revision(t.revision()+1);
                byte[] value = RevisionObject.toBinary(t.revision(),t.toBinary(),true);
                if(!_put(dso,key,value)) return false;

                if(t.backup()) {
                    if(creating){
                        this.mapStoreListener.onCreating(dso.metadata,akey,t);
                    }else{
                        this.mapStoreListener.onUpdating(dso.metadata, akey, t);
                    }
                }
                if(t.distributable()) this.mapStoreListener.onDistributing(dso.metadata,key,value);

                Listener listener = rMap.get(t.getFactoryId());

                if(listener!=null) listener.onUpdated(t,akey,key,value);

                return true;
            });

        }catch (Exception ex){
            log.error("Error on update",ex);
            return false;
        }
    }

    @Override
    public <T extends Recoverable> boolean createIfAbsent(T t, boolean loading) {
        try{
            String akey = t.key().asString();
            if(akey==null){
                t.bucket(this.bucket);
                t.oid(SystemUtil.oid());
                akey = t.key().asString();
            }
            byte[] key = akey.getBytes();
            final String okey = akey;
            DataBaseOnPartition dso = this.partitions[SystemUtil.partition(key,partition)];
            boolean suc = dso.lock(key,()->{
                RevisionObject ro = _getRevisionObject(dso,key);//from local
                if(ro==null){
                    byte[] data = mapStoreListener.onRecovering(dso.metadata,key);//from cluster
                    if(data != null) {
                        ro = RevisionObject.fromBinary(data);
                        _put(dso,key,RevisionObject.toBinary(ro.revision,ro.data,true));//set local data from remote
                    }
                }
                if(ro != null){//existed no creation
                    if(loading) {
                        t.fromBinary(ro.data);
                        t.revision(ro.revision);
                    }
                    return false;
                }
                byte[] vx = RevisionObject.toBinary(Long.MIN_VALUE,t.toBinary(),true);
                if(!_put(dso,key,vx)) return false;
                t.revision(Long.MIN_VALUE);
                if(t.backup()) this.mapStoreListener.onCreating(dso.metadata,okey,t);

                if(t.distributable()) this.mapStoreListener.onDistributing(dso.metadata, key,vx);

                Listener listener = rMap.get(t.getFactoryId());
                if(listener!=null) listener.onCreated(t,okey,key,vx);
                //if(t.onEdge()&&t.owner()!=null&&t.label()!=null) onEdge(t,okey);
                return true;
            });
            if(suc && t.onEdge() && t.owner() != null && t.label() !=null){
                onEdge(t,okey);
            }
            return suc;

        }catch (Exception ex){
            log.error("Error on createIfAbsent",ex);
            return false;
        }
    }

    @Override
    public <T extends Recoverable> boolean load(T t) {
        try{
            String akey = t.key().asString();
            if(akey==null) return false;
            byte[] key = akey.getBytes();
            DataBaseOnPartition dso = partitions[SystemUtil.partition(key,partition)];
            return dso.lock(key,()->{
                RevisionObject ro = _getRevisionObject(dso,key);
                if(ro == null || !ro.local){//get from cluster
                    byte[] value = mapStoreListener.onRecovering(dso.metadata,key);
                    if(value==null && ro==null) return false;
                    ro = RevisionObject.fromBinary(value);
                    _put(dso,key,RevisionObject.toBinary(ro.revision,ro.data,true));
                }
                t.fromBinary(ro.data);
                t.revision(ro.revision);
                return true;
            });
        }catch (Exception ex){
            log.error("Error on load",ex);
            return false;
        }
    }

    public byte[] load(byte[] key){
        DataBaseOnPartition dso = partitions[SystemUtil.partition(key,partition)];
        boolean loaded = dso.lock(key,()->{
            RevisionObject ro = _getRevisionObject(dso,key);
            if(ro == null || !ro.local){//get from cluster
                byte[] value = mapStoreListener.onRecovering(dso.metadata,key);
                if(value==null && ro==null) return false;
                ro = RevisionObject.fromBinary(value);
                _put(dso,key,RevisionObject.toBinary(ro.revision,ro.data,true));
            }
            return true;
        });
        return loaded?_get(dso,key):null;
    }

    @Override
    public void set(byte[] key, byte[] value) {
        try{
            DataBaseOnPartition dso = partitions[SystemUtil.partition(key,partition)];
            dso.lock(key,()->{
                RevisionObject ro = _getRevisionObject(dso,key);
                RevisionObject rd = RevisionObject.fromBinary(value);
                if(ro == null){
                    _put(dso,key,RevisionObject.toBinary(rd.revision,rd.data,false));
                   return true;
                }
                if(rd.revision <= ro.revision) return false;
                _put(dso,key,RevisionObject.toBinary(ro.revision,ro.data,false));
                return true;
            });
        }
        catch (Exception ex){
            log.error("Error on set",ex);
        }
    }
    public byte[] get(byte[] key){
        try{
            DataBaseOnPartition dso = partitions[SystemUtil.partition(key,partition)];
            RevisionObject[] ros = new RevisionObject[1];
            boolean suc = dso.lock(key,()->{
                RevisionObject ro = _getRevisionObject(dso,key);
                if(ro == null || !ro.local) return false;
                ros[0] = ro;
                return true;
            });
            if(!suc) return null;
            return RevisionObject.toBinary(ros[0].revision,ros[0].data,ros[0].local);
        }catch (Exception ex){
            log.error("error on get",ex);
            return null;
        }
    }
    public void list(Binary binary){
        for(DataBaseOnPartition dso : partitions){
            Cursor cursor = dso.database.openCursor(null,null);
            DatabaseEntry _key = new DatabaseEntry();
            DatabaseEntry _value = new DatabaseEntry();
            try{
                while (cursor.getNext(_key, _value, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
                    RevisionObject ro = RevisionObject.fromBinary(_value.getData());
                    if(ro.local){
                        if(!binary.on(_key.getData(),RevisionObject.toBinary(ro.revision,ro.data,true))) break;
                    }
                }
            } catch (Exception ex) {
                log.error("",ex);
            } finally {
                cursor.close();
            }
        }
    }
    public  <T extends Recoverable> void list(RecoverableFactory<T> query,Stream<T> binary){
        try {
            String akey = (query.distributionKey() + Recoverable.PATH_SEPARATOR + query.label());
            byte[] owner = akey.getBytes();
            DataBaseOnPartition dso = partitions[SystemUtil.partition(owner,partition)];
            RevisionObject ro = _getRevisionObject(dso,owner);
            if(ro==null|| !ro.local){
                //recovering from remote
                byte[] data = mapStoreListener.onRecovering(dso.metadata,owner);
                if(data!=null){
                    ro = RevisionObject.fromBinary(data);
                    _put(dso,owner,RevisionObject.toBinary(ro.revision,ro.data,true));
                }
            }
            if(ro==null) return;
            IndexSet indexSet = new IndexSet();
            indexSet.fromBinary(ro.data);
            for(String b: indexSet.keySet()){
                RevisionObject v;
                byte[] ka = b.getBytes();
                DataBaseOnPartition dwso = partitions[SystemUtil.partition(ka,partition)];
                if((v =_getRevisionObject(dwso,ka)) == null){//from local
                    byte[] data = mapStoreListener.onRecovering(dwso.metadata,ka);
                    if(data!=null){
                        v = RevisionObject.fromBinary(data);
                        _put(dwso,ka,RevisionObject.toBinary(v.revision,v.data,false));
                    }
                }
                if(v!=null){
                    T t = query.create();
                    t.fromBinary(v.data);
                    t.revision(v.revision);
                    t.distributionKey(b);
                    if(!binary.on(t)) break;
                }
            }
        }catch (Exception ex){
            log.error("Error on list",ex);
        }
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

    public Backup backup(){
        return this;
    }

    private boolean _put(DataBaseOnPartition dso,byte[] key,byte[] value){
        return dso.database.put(null, new DatabaseEntry(key), new DatabaseEntry(value)) == OperationStatus.SUCCESS;
    }

    public void registerListener(int registerId,Listener listener){
        rMap.putIfAbsent(registerId,listener);
    }

    private byte[] _get(DataBaseOnPartition dso,byte[] key){
        DatabaseEntry ve = new DatabaseEntry();
        OperationStatus status = dso.database.get(null, new DatabaseEntry(key), ve, null);
        return status==OperationStatus.SUCCESS?ve.getData():null;
    }

    private RevisionObject _getRevisionObject(DataBaseOnPartition dso,byte[] key){
        DatabaseEntry ve = new DatabaseEntry();
        OperationStatus status = dso.database.get(null, new DatabaseEntry(key), ve, null);
        return status==OperationStatus.SUCCESS?RevisionObject.fromBinary(ve.getData()):null;
    }
}
