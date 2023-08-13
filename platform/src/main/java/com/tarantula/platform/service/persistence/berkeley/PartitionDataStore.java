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

/**
 * The key of the recoverable T is thread-safe. It is locked on concurrent map.
 * However, the recoverable T itself is not thread-safe. Callers need to take care of the concurrent access.
 * */

public class PartitionDataStore implements ReplicatedDataStore{

    private final DataBaseOnPartition[] partitions;
    private final int partition;
    private final String bucket;
    private final String node;

    //private final byte[] _node;
    private final String prefix;

    private final MapStoreListener mapStoreListener;

    private static TarantulaLogger log = JDKLogger.getLogger(PartitionDataStore.class);

    public PartitionDataStore(int partition, String bucket, String node, String prefix, Database[] shards,MapStoreListener mapStoreListener){
        this.partition = partition;
        this.bucket = bucket;
        this.node = node;
        //this._node = node.getBytes();
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
            if (akey != null) return false;
            t.bucket(this.bucket);
            t.oid(SystemUtil.oid());
            akey = t.key().asString();
            byte[] key = akey.getBytes();
            t.revision(Long.MIN_VALUE);
            byte[] value = RevisionObject.toBinary(t.revision(),t.toBinary(),true);
            DataBaseOnPartition dso = this.partitions[SystemUtil.partition(key,partition)];
            boolean suc = dso.lock(key,()-> _put(dso,key,value)? RevisionObject.TRUE : RevisionObject.FALSE).successful;
            if(!suc) return false;
            if(t.backup()) this.mapStoreListener.onBackingUp(dso.metadata.fromRevision(t.revision()),akey,t);
            if(t.distributable()) this.mapStoreListener.onDistributing(dso.metadata,akey,key,value);
            if(t.onEdge() && t.owner() != null && t.label() !=null){
                onEdge(t,akey);
            }
            return true;
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
        String akey = indexSet.key().asString();
        byte[] key = akey.getBytes();
        DataBaseOnPartition dso = this.partitions[SystemUtil.partition(key,partition)];
        RevisionObject pendingData = dso.lock(key,()->_getRevisionObject(dso,key));
        if(!pendingData.successful || !pendingData.local
        ){
            byte[] data = mapStoreListener.onRecovering(dso.metadata,akey,key);
            if(data != null){
                RevisionObject remoteData = RevisionObject.fromBinary(data);
                if(!pendingData.successful || remoteData.revision >= pendingData.revision){
                    pendingData = remoteData; //use remote data
                }
            }
        }
        if(pendingData.successful){
            indexSet.fromBinary(pendingData.data);
            indexSet.revision(pendingData.revision);
        }
        RevisionObject suc = dso.lock(key,()->{
            RevisionObject localData = _getRevisionObject(dso,key);//reload to check if there are newer updates existing
            if(localData.successful && localData.revision > indexSet.revision()){
                indexSet.fromBinary(localData.data);
                indexSet.revision(localData.revision);
            }
            indexSet.addKey(okey);
            indexSet.revision(indexSet.revision()+1);
            byte[] pendingUpdate = RevisionObject.toBinary(indexSet.revision(),indexSet.toBinary(),true);
            return (_put(dso,key,pendingUpdate)) ? RevisionObject.fromUpdate(indexSet.revision(),pendingUpdate) : RevisionObject.FALSE;
        });
        if(suc.successful){
            if(t.backup())this.mapStoreListener.onBackingUp(dso.metadata.fromRevision(indexSet.revision()),akey,indexSet);
            if(t.distributable()) this.mapStoreListener.onDistributing(dso.metadata,akey,key ,suc.data);
        }
        return suc.successful;
    }

    @Override
    public <T extends Recoverable> boolean update(T t) {
        try{
            String akey = t.key().asString();
            if(akey==null) return false;
            byte[] key = akey.getBytes();
            byte[] pendingUpdate = RevisionObject.toBinary(t.revision()+1,t.toBinary(),true);
            DataBaseOnPartition dso = partitions[SystemUtil.partition(key,partition)];
            RevisionObject suc = dso.lock(key,()->{//local update
                RevisionObject localData = _getRevisionObject(dso,key);
                if(localData.successful && localData.local && localData.revision == t.revision()){
                    if(_put(dso,key,pendingUpdate)){
                        t.revision(t.revision()+1);
                        return RevisionObject.fromUpdate(t.revision()+1,pendingUpdate);
                    }
                    return RevisionObject.FALSE;
                }
                return RevisionObject.FALSE;
            });
            if(!suc.successful){//remote recovery
                byte[] data = mapStoreListener.onRecovering(dso.metadata,akey,key);
                if(data != null){
                    RevisionObject remoteData = RevisionObject.fromBinary(data);
                    suc = dso.lock(key,()->{
                        RevisionObject localData = _getRevisionObject(dso,key);
                        if(!localData.successful || !localData.local || localData.revision <= remoteData.revision ){
                            byte[] remoteValue = RevisionObject.toBinary(remoteData.revision+1,remoteData.data,true);
                            if(_put(dso,key,remoteValue)){
                                return RevisionObject.fromUpdate(remoteData.revision+1,remoteValue);
                            }
                            return RevisionObject.FALSE;
                        }
                        return RevisionObject.FALSE;
                    });
                    if(suc.successful){
                        t.fromBinary(remoteData.data);
                        t.revision(remoteData.revision+1);
                    }
                }
            }
            if(suc.successful){
               if(t.backup()) this.mapStoreListener.onBackingUp(dso.metadata.fromRevision(t.revision()), akey, t);
               if(t.distributable()) this.mapStoreListener.onDistributing(dso.metadata,akey,key,suc.data);
            }
            return suc.successful;

        }catch (Exception ex){
            log.error("Error on update",ex);
            return false;
        }
    }

    @Override
    public <T extends Recoverable> boolean createIfAbsent(T t, boolean loading) {
        try{
            String akey = t.key().asString();
            if(akey==null) throw new IllegalArgumentException("Key must be assigned first");
            byte[] key = akey.getBytes();
            final String okey = akey;
            t.revision(Long.MIN_VALUE);
            byte[] pendingValue = RevisionObject.toBinary(t.revision(),t.toBinary(),true);
            boolean[] created = {false};
            DataBaseOnPartition dso = this.partitions[SystemUtil.partition(key,partition)];
            RevisionObject suc = dso.lock(key,()->{
                RevisionObject localData = _getRevisionObject(dso,key);
                if(localData.successful){
                    return localData;
                }
                return RevisionObject.FALSE;
            });
            if(!suc.successful){//remote recovery
               byte[] data = mapStoreListener.onRecovering(dso.metadata,akey,key);
               if(data != null){
                   RevisionObject remoteData = RevisionObject.fromBinary(data);
                   suc = dso.lock(key,()->{
                       RevisionObject localData = _getRevisionObject(dso,key);
                       if(!localData.successful || localData.revision <= remoteData.revision){
                           return _put(dso,key,data)? remoteData : localData;
                       }
                       return localData;
                   });
               }
               else{
                   suc = dso.lock(key,()->{
                       RevisionObject localData = _getRevisionObject(dso,key);
                       if(!localData.successful){
                           created[0] = _put(dso,key,pendingValue);
                           return RevisionObject.FALSE;
                       }
                       return localData;
                   });
               }

            }
            if(!created[0] && suc.successful && loading){
                t.fromBinary(suc.data);
                t.revision(suc.revision);
            }
            if(created[0]) {
                if (t.backup()) this.mapStoreListener.onBackingUp(dso.metadata.fromRevision(t.revision()), okey, t);

                if (t.distributable()) this.mapStoreListener.onDistributing(dso.metadata, okey, key,pendingValue);
            }
            if(created[0] && t.onEdge() && t.owner() != null && t.label() !=null){
                onEdge(t,okey);
            }
            return created[0];

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
            RevisionObject pendingData = dso.lock(key,()-> _getRevisionObject(dso,key));
            if(pendingData.successful && pendingData.local){
                t.fromBinary(pendingData.data);
                t.revision(pendingData.revision);
                return true;
            }
            byte[] data = mapStoreListener.onRecovering(dso.metadata,akey,key);
            if(data==null) return false;
            RevisionObject remoteData = RevisionObject.fromBinary(data);
            pendingData = dso.lock(key,()->{
                RevisionObject localData = _getRevisionObject(dso,key);
                if(!localData.successful || !localData.local || remoteData.revision >= localData.revision){
                    if(_put(dso,key,data)){
                        return remoteData;
                    }
                    return localData;
                }
                return localData;
            });
            if(!pendingData.successful) return false;
            t.fromBinary(pendingData.data);
            t.revision(pendingData.revision);
            return true;
        }catch (Exception ex){
            log.error("Error on load",ex);
            return false;
        }
    }

    public byte[] load(byte[] key){
        DataBaseOnPartition dso = partitions[SystemUtil.partition(key,partition)];
        RevisionObject pendingData = dso.lock(key,()->_getRevisionObject(dso,key));
        if(pendingData.successful && pendingData.local){
            return RevisionObject.toBinary(pendingData.revision,pendingData.data,pendingData.local);
        }
        byte[] data = mapStoreListener.onRecovering(dso.metadata,new String(key),key);
        if(data==null) return null;
        RevisionObject remoteData = RevisionObject.fromBinary(data);
        pendingData = dso.lock(key,()->{
            RevisionObject localData = _getRevisionObject(dso,key);
            if(!localData.successful || !localData.local || remoteData.revision >= localData.revision){
                return _put(dso,key,data)? remoteData : localData;
            }
            return localData;
        });
        if(!pendingData.successful) return null;
        return RevisionObject.toBinary(pendingData.revision,pendingData.data,true);
    }

    public  <T extends Recoverable> void list(RecoverableFactory<T> query,Stream<T> binary){
        try {
            String akey = (query.distributionKey() + Recoverable.PATH_SEPARATOR + query.label());
            byte[] owner = akey.getBytes();
            DataBaseOnPartition dso = partitions[SystemUtil.partition(owner,partition)];
            RevisionObject pendingData = _getRevisionObject(dso,owner);
            if(!pendingData.successful || !pendingData.local){
                //recovering from remote
                byte[] data = mapStoreListener.onRecovering(dso.metadata,akey,owner);
                if(data!=null){
                    RevisionObject remoteData = RevisionObject.fromBinary(data);
                    pendingData = dso.lock(owner,()->{
                        RevisionObject localData = _getRevisionObject(dso,owner);
                        if(!localData.successful || remoteData.revision >= localData.revision ){
                            return _put(dso,owner,RevisionObject.toBinary(remoteData.revision,remoteData.data,true))? remoteData: RevisionObject.FALSE;
                        }
                        return RevisionObject.FALSE;
                    });
                }
            }
            if(!pendingData.successful) return;
            IndexSet indexSet = new IndexSet();
            indexSet.fromBinary(pendingData.data);
            for(String k : indexSet.keySet()){
                byte[] key = k.getBytes();
                byte[] data = load(key);
                if(data != null){
                    RevisionObject revisionObject = RevisionObject.fromBinary(data);
                    T t = query.create();
                    t.fromBinary(revisionObject.data);
                    t.revision(revisionObject.revision);
                    t.distributionKey(k);
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

    public boolean delete(byte[] key){
        DataBaseOnPartition dso = partitions[SystemUtil.partition(key,partition)];
        if(!_delete(dso,key)) return false;
        mapStoreListener.onDeleting(dso.metadata,key);
        return true;
    }

    /**
     * Backup implementation
     * */
    public Backup backup(){
        return this;
    }

    @Override
    public boolean set(byte[] key, byte[] value) {
        try{
            DataBaseOnPartition dso = partitions[SystemUtil.partition(key,partition)];
            return dso.lock(key,()->{
                RevisionObject localData = _getRevisionObject(dso,key);
                RevisionObject remoteData = RevisionObject.fromBinary(value);
                if(!localData.successful || localData.revision < remoteData.revision){
                    _put(dso,key,RevisionObject.toBinary(remoteData.revision,remoteData.data,false));
                    return RevisionObject.TRUE;
                }
                return RevisionObject.FALSE;
            }).successful;
        }
        catch (Exception ex){
            log.error("Error on backup set",ex);
            return false;
        }
    }
    public byte[] get(byte[] key){
        try{
            DataBaseOnPartition dso = partitions[SystemUtil.partition(key,partition)];
            RevisionObject localData = dso.lock(key,()->_getRevisionObject(dso,key));
            if(!localData.successful) return null;// || !localData.local) return null;
            return RevisionObject.toBinary(localData.revision,localData.data,localData.local);
        }catch (Exception ex){
            log.error("error on backup get",ex);
            return null;
        }
    }
    public void list(Binary binary){
        for(DataBaseOnPartition dso : partitions){
            Cursor cursor = dso.database.openCursor(null,null);
            DatabaseEntry _key = new DatabaseEntry();
            DatabaseEntry _value = new DatabaseEntry();
            try{
                boolean stopped = false;
                while (cursor.getNext(_key, _value, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
                    RevisionObject localData = RevisionObject.fromBinary(_value.getData());
                    if(localData.successful){
                        if(!binary.on(_key.getData(),RevisionObject.toBinary(localData.revision,localData.data,localData.local))){
                            stopped = true;
                            break;
                        }
                    }
                }
                if(stopped) break;
            } catch (Exception ex) {
                log.error("error on backup list",ex);
            } finally {
                cursor.close();
            }
        }
    }

    public void unset(byte[] key){
        DataBaseOnPartition dso = partitions[SystemUtil.partition(key,partition)];
        _delete(dso,key);
    }
    // End of Backup implementation

    //Berkeley data base helper methods
    private boolean _put(DataBaseOnPartition dso,byte[] key,byte[] value){
        return dso.database.put(null, new DatabaseEntry(key), new DatabaseEntry(value)) == OperationStatus.SUCCESS;
    }

    private RevisionObject _getRevisionObject(DataBaseOnPartition dso,byte[] key){
        DatabaseEntry ve = new DatabaseEntry();
        OperationStatus status = dso.database.get(null, new DatabaseEntry(key), ve, null);
        return status==OperationStatus.SUCCESS?RevisionObject.fromBinary(ve.getData()):RevisionObject.FALSE;
    }

    private boolean _delete(DataBaseOnPartition dso,byte[] key){
        return dso.database.delete(null, new DatabaseEntry(key)) == OperationStatus.SUCCESS;
    }
}
