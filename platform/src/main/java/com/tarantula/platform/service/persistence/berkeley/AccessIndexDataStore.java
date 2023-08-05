package com.tarantula.platform.service.persistence.berkeley;

import com.icodesoftware.Distributable;
import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.Metadata;
import com.sleepycat.je.*;
import com.tarantula.platform.service.persistence.*;

import java.util.List;

public class AccessIndexDataStore implements ReplicatedDataStore {

    private static TarantulaLogger log = JDKLogger.getLogger(AccessIndexDataStore.class);
    private final Database berkeleyStore;
    private final ClusterNode node;
    private final byte[] _node;
    private final MapStoreListener mapStoreListener;
    private final String dataStore;
    private final int partition;
    private final Metadata metadata1;


    public AccessIndexDataStore(ClusterNode node, Database database, MapStoreListener mapStoreListener){
        this.node = node;
        this._node = node.nodeName().getBytes();
        this.berkeleyStore = database;
        this.dataStore = this.berkeleyStore.getDatabaseName();
        int  index = this.dataStore.lastIndexOf("_");
        this.partition = Integer.parseInt(this.dataStore.substring(index+1));
        this.metadata1 = new RecoverableMetadata(dataStore,partition, Distributable.INTEGRATION_SCOPE);
        this.mapStoreListener = mapStoreListener;
    }

    @Override
    public String bucket() {
        return this.node.bucketName;
    }

    @Override
    public String node() {
        return node.nodeName;
    }

    @Override
    public String name(){
        return this.dataStore;
    }
    public long count(){
        return this.berkeleyStore.count();
    }
    public int partitionNumber(){
        return partition;
    }

    public long count(int partition){
        return this.berkeleyStore.count();
    }
    @Override
    public <T extends Recoverable> boolean create(T t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends Recoverable> boolean update(T t) {
        throw new UnsupportedOperationException();
    }


    @Override
    public <T extends Recoverable> boolean createIfAbsent(T t, boolean loading) {
        try{
            String akey = t.key().asString();
            if(akey==null) return false;
            byte[] k = akey.getBytes();
            byte[] v = _get(k);//get local
            if(v==null){
                v = mapStoreListener.onRecovering(metadata1,akey,k);//get cluster
                if(v!=null) _set(k,v);//local set
            }
            if(v!=null){
                RevisionObject ro = RevisionObject.fromBinary(v);
                if(loading) t.fromBinary(ro.data);
                return false;
            }
            v = RevisionObject.toBinary(0,t.toBinary(),true,_node);
            if(!_set(k,v)) return false;
            mapStoreListener.onDistributing(metadata1,akey,k,RevisionObject.fromRecoverable(t,_node));//set cluster
            if(t.backup()) mapStoreListener.onBackingUp(metadata1,akey,t);
            return true;
        }catch (Exception ex){
            log.error("error on createIfAbsent",ex);
            return false;
        }
    }

    @Override
    public <T extends Recoverable> boolean load(T t) {
        try{
            String akey = t.key().asString();
            if(akey==null) return false;
            byte[] key = akey.getBytes();
            byte[] value;
            if((value=_get(key))!=null){//from local
                RevisionObject ro = RevisionObject.fromBinary(value);
                t.fromBinary(ro.data);
                return true;
            }
            if((value=mapStoreListener.onRecovering(metadata1,akey,key))==null) return false;
            RevisionObject ro = RevisionObject.fromBinary(value);
            t.fromBinary(ro.data);
            _set(key,value);
            return true;
        }catch (Exception ex){
            log.error("error on load",ex);
            return false;
        }
    }

    public byte[] load(byte[] key){
        throw new UnsupportedOperationException();
    }

    public boolean delete(byte[] key){
        if(!_delete(key)) return false;
        mapStoreListener.onDeleting(metadata1,key);
        return true;
    }
    public boolean set(byte[] key,byte[] value){
        try{
            return _set(key,value);
        }
        catch (Exception ex){
            log.error("error on backup set",ex);
            return false;
        }
    }
    public byte[] get(byte[] key){
        try{
            return _get(key);
        }catch (Exception ex){
            log.error("error on backup get",ex);
            return null;
        }

    }
    public void list(Binary binary){
        Cursor cursor = berkeleyStore.openCursor(null,null);
        DatabaseEntry _key = new DatabaseEntry();
        DatabaseEntry _value = new DatabaseEntry();
        try{
            while (cursor.getNext(_key, _value, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
                if(!binary.on(_key.getData(),_value.getData())){
                    break;
                }
            }
        } catch (Exception ex) {
            log.error("error on backup list",ex);
        } finally {
            cursor.close();
        }
    }

    @Override
    public void unset(byte[] key) {
        _delete(key);
    }

    @Override
    public <T extends Recoverable> List<T> list(RecoverableFactory<T> query) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends Recoverable> void list(RecoverableFactory<T> query, Stream<T> stream) {
        throw new UnsupportedOperationException();
    }
    public Backup backup(){
        return this;
    }

    public void close(){
        this.berkeleyStore.close();
    }

    private boolean _set(byte[] key,byte[] value){
        return berkeleyStore.put(null,new DatabaseEntry(key),new DatabaseEntry(value))==OperationStatus.SUCCESS;
    }
    private byte[] _get(byte[] key){
        DatabaseEntry ve = new DatabaseEntry();
        OperationStatus status = berkeleyStore.get(null,new DatabaseEntry(key),ve,null);
        if(status==OperationStatus.SUCCESS){
            return ve.getData();
        }
        else{
            return null;
        }
    }
    private boolean _delete(byte[] key){
        return berkeleyStore.delete(null,new DatabaseEntry(key)) == OperationStatus.SUCCESS;
    }
}
