package com.icodesoftware.lmdb;

import com.icodesoftware.*;
import com.icodesoftware.service.Metadata;
import org.lmdbjava.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class LMDBDataStore implements DataStore,DataStore.Backup ,Closable {

    private final Env<ByteBuffer> env;
    private final Dbi<ByteBuffer> dbi;
    private final String name;

    private final String bucket ="DBS";

    private Metadata metadata;

    private int scope;

    //NOTES : key+value < 2040 bytes ( 511 bytes for key ; value <= 1521 bytes (2040 - 511 - 8)

    private final LMDBDataStoreProvider lmdbDataStoreProvider;
    public LMDBDataStore(int scope,String name, Dbi<ByteBuffer> dbi,Env<ByteBuffer> env,LMDBDataStoreProvider lmdbDataStoreProvider){
        this.scope = scope;
        this.name = name;
        this.dbi = dbi;
        this.env = env;
        this.lmdbDataStoreProvider = lmdbDataStoreProvider;
    }

    @Override
    public String bucket() {
        return bucket;
    }

    @Override
    public String node() {
        return null;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public int partitionNumber() {
        return 0;
    }

    @Override
    public long count(int partition) {
        return 0;
    }

    @Override
    public <T extends Recoverable> boolean create(T t) {
        //create edge db before txn creation
        if(t.onEdge() && t.label()!=null) lmdbDataStoreProvider.createEdgeDB(scope,name+"_"+t.label());
        ByteBuffer key = ByteBuffer.allocateDirect(env.getMaxKeySize());
        Recoverable.DataBuffer keyBuffer = new BufferProxy(key);
        lmdbDataStoreProvider.assignKey(keyBuffer);
        key.flip();
        if(!t.readKey(keyBuffer)) return false;
        key.rewind();
        Txn<ByteBuffer> txn = env.txnWrite(); //can read also
        ByteBuffer value = ByteBuffer.allocateDirect(2700);
        BufferProxy proxy = new BufferProxy(value);
        proxy.writeHeader(new LocalHeader(true,Long.MIN_VALUE,t.getFactoryId(),t.getClassId()));
        t.write(proxy);
        value.flip();
        try{
            if(!dbi.put(txn,key,value)) return false;
            key.rewind();
            onEdge(t,t.label(),key,txn);
            txn.commit();
            key.rewind();
            value.rewind();
            t.revision(Long.MIN_VALUE);
            lmdbDataStoreProvider.onDistributing(metadata,key,value);
            return true;
        }finally {
            txn.close();
        }
    }

    @Override
    public <T extends Recoverable> boolean update(T t) {
        ByteBuffer key = ByteBuffer.allocateDirect(env.getMaxKeySize());
        if(!t.writeKey(new BufferProxy(key))) return false;
        key.flip();
        ByteBuffer value = ByteBuffer.allocateDirect(2700);
        Txn<ByteBuffer> txn = env.txnWrite(); //can read also
        try{
            if (dbi.get(txn, key) == null) return false;
            BufferProxy proxy = new BufferProxy(txn.val());
            Recoverable.DataHeader header = proxy.readHeader();
            if(header.revision()!=t.revision()) return false;
            BufferProxy update = new BufferProxy(value);
            header.update(header.local(),1);
            update.writeHeader(header);
            t.write(update);
            value.flip();
            key.rewind();
            if(!dbi.put(txn,key,value)) return false;
            txn.commit();
            t.revision(header.revision());
            return true;
        }finally {
            txn.close();
        }
    }

    @Override
    public <T extends Recoverable> boolean createIfAbsent(T t, boolean loading) {
        if(t.onEdge() && t.label()!=null) lmdbDataStoreProvider.createEdgeDB(scope,name+"_"+t.label());
        ByteBuffer key = ByteBuffer.allocateDirect(env.getMaxKeySize());
        if(!t.writeKey(new BufferProxy(key))) throw new IllegalArgumentException("Key must be assigned first");
        key.flip();
        Txn<ByteBuffer> txn = env.txnWrite(); //can be reading also
        try{
            if (dbi.get(txn, key) != null) {
                if (!loading) return false;
                BufferProxy proxy = new BufferProxy(txn.val());
                Recoverable.DataHeader h = proxy.readHeader();
                t.read(proxy);
                t.revision(h.revision());
                return false;
            }
            ByteBuffer value = ByteBuffer.allocateDirect(2700);
            BufferProxy proxy = new BufferProxy(value);
            proxy.writeHeader(new LocalHeader(true,Long.MIN_VALUE,t.getFactoryId(),t.getClassId()));
            t.write(proxy);
            value.flip();
            key.rewind();
            if (!dbi.put(txn, key, value)) throw new RuntimeException("lmdb failure to insert key/value");
            key.rewind();
            onEdge(t,t.label(),key,txn);
            txn.commit();
            t.revision(Long.MIN_VALUE);
            return true;
        }
        finally {
            txn.close();//rollback if exception
        }
    }

    @Override
    public <T extends Recoverable> boolean load(T t) {
        ByteBuffer key = ByteBuffer.allocateDirect(env.getMaxKeySize());
        if(!t.writeKey(new BufferProxy(key))) return false;
        key.flip();
        Txn<ByteBuffer> txn = env.txnRead(); //read only
        try{
            if (dbi.get(txn, key) == null) return false;
            BufferProxy proxy = new BufferProxy(txn.val());
            Recoverable.DataHeader header = proxy.readHeader();
            t.read(proxy);
            t.revision(header.revision());
            return true;
        }finally {
            txn.close();
        }
    }

    public <T extends Recoverable> boolean createEdge(T t,String label){
        lmdbDataStoreProvider.createEdgeDB(scope,name+"_"+label);
        ByteBuffer key = ByteBuffer.allocateDirect(env.getMaxKeySize());
        if(!t.writeKey(new BufferProxy(key))) return false;
        key.flip();
        Txn<ByteBuffer> txn = env.txnWrite();
        try{

            ByteBuffer okey = onEdge(t,label,key,txn);
            if(okey==null) return false;
            txn.commit();
            //lmdbDataStoreProvider.onDistributing(metadata,okey,key);
            return true;
        }finally {
            txn.close();
        }
    }

    public  <T extends Recoverable> boolean deleteEdge(Recoverable.Key t,String label){
        Txn<ByteBuffer> txn = env.txnWrite();
        if(!offEdge(t,label,txn)) return false;
        txn.commit();
        return true;
    }
    public <T extends Recoverable> boolean deleteEdge(Recoverable.Key t,Recoverable.Key edge,String label){
        ByteBuffer key = ByteBuffer.allocateDirect(env.getMaxKeySize());
        if(!edge.write(new BufferProxy(key))) return false;
        key.flip();
        Txn<ByteBuffer> txn = env.txnWrite();
        if(!offEdge(t,label,key,txn)) return false;
        txn.commit();
        return true;
    }

    public boolean load(Recoverable.Key key, Buffer buffer) {
        ByteBuffer akey = ByteBuffer.allocateDirect(env.getMaxKeySize());
        key.write(new BufferProxy(akey));
        akey.flip();
        Txn<ByteBuffer> txn = env.txnRead(); //read only
        try{
            if (dbi.get(txn, akey) == null) return false;
            return buffer.on(new BufferProxy(txn.val()));
        }finally {
            txn.close();
        }
    }

    public <T extends Recoverable> boolean delete(T t){
        ByteBuffer key = ByteBuffer.allocateDirect(env.getMaxKeySize());
        t.writeKey(new BufferProxy(key));
        key.flip();
        Txn<ByteBuffer> txn = env.txnWrite();
        try{
            if(!dbi.delete(txn, key)) return false;
            key.rewind();
            offEdge(t.ownerKey(),t.label(),key,txn);
            txn.commit();
            return true;
        }finally {
            txn.close();
        }
    }

    @Override
    public <T extends Recoverable> List<T> list(RecoverableFactory<T> query) {
        ArrayList<T> list = new ArrayList<>();
        list(query,(t)-> list.add(t));
        return list;
    }

    @Override
    public <T extends Recoverable> void list(RecoverableFactory<T> query, Stream<T> stream){
        ByteBuffer key = ByteBuffer.allocateDirect(env.getMaxKeySize());
        if(!query.key().write(new BufferProxy(key))) return;
        key.flip();
        Dbi<ByteBuffer> edgeDbi = lmdbDataStoreProvider.createEdgeDB(scope,name+"_"+query.label());
        Txn<ByteBuffer> txn = env.txnRead();
        CursorIterable<ByteBuffer> cursor = edgeDbi.iterate(txn, KeyRange.closed(key, key));
        try{
            for(Iterator<CursorIterable.KeyVal<ByteBuffer>> it = cursor.iterator();it.hasNext();){
                CursorIterable.KeyVal<ByteBuffer> kv = it.next();
                T t = query.create();
                if(dbi.get(txn,kv.val())!=null){
                    BufferProxy proxy = new BufferProxy(txn.val());
                    Recoverable.DataHeader local = proxy.readHeader();
                    t.read(proxy);
                    t.revision(local.revision());
                    t.readKey(new BufferProxy(kv.val()));
                    if(!stream.on(t)) break;
                }
            }
        }finally {
            cursor.close();
            txn.close();
        }
    }
    @Override
    public Backup backup() {
        return this;
    }

    @Override
    public void close() {
        dbi.close();
    }

    @Override
    public boolean set(byte[] key, byte[] value) {
        return false;
    }

    @Override
    public byte[] get(byte[] key) {
        return new byte[0];
    }

    @Override
    public void unset(byte[] key) {

    }
    public boolean set(ByteBuffer key, ByteBuffer value){
        Txn<ByteBuffer> txn = env.txnWrite();
        try{
            if(!dbi.put(txn,key,value)) return false;
            txn.commit();
            return true;
        }finally {
            txn.close();
        }
    }
    @Override
    public void list(Buffer binary) {
        final Txn<ByteBuffer> txn = env.txnRead();
        final Cursor<ByteBuffer> cursor = dbi.openCursor(txn);
        while (cursor.next()){
            if(!binary.on(new BufferProxy(cursor.val()))) break;
        }
        cursor.close();
        txn.close();
    }

    private <T extends Recoverable> ByteBuffer onEdge(T t,String label,ByteBuffer edge,Txn<ByteBuffer> txn){
        if(!t.onEdge() || t.ownerKey()==null || label==null) return null;
        ByteBuffer key = ByteBuffer.allocateDirect(env.getMaxKeySize());
        if(!t.ownerKey().write(new BufferProxy(key))) return null;
        key.flip();
        Dbi<ByteBuffer> edgeDbi = lmdbDataStoreProvider.createEdgeDB(scope,name+"_"+label);
        if(!edgeDbi.put(txn,key,edge, PutFlags.MDB_NODUPDATA)) return null;
        key.rewind();
        edge.rewind();
        return key;
    }
    private <T extends Recoverable> boolean offEdge(Recoverable.Key t,String label,ByteBuffer edge,Txn<ByteBuffer> txn){
        if(t==null || edge==null || label ==null) return false;
        ByteBuffer key = ByteBuffer.allocateDirect(env.getMaxKeySize());
        if(!t.write(new BufferProxy(key))) return false;
        key.flip();
        Dbi<ByteBuffer> edgeDbi = lmdbDataStoreProvider.createEdgeDB(scope,name+"_"+label);
        if(!edgeDbi.delete(txn,key,edge)) return false;
        key.rewind();
        edge.rewind();
        return true;
    }
    private <T extends Recoverable> boolean offEdge(Recoverable.Key t,String label,Txn<ByteBuffer> txn){
        if(t==null || label ==null) return false;
        ByteBuffer key = ByteBuffer.allocateDirect(env.getMaxKeySize());
        if(!t.write(new BufferProxy(key))) return false;
        key.flip();
        Dbi<ByteBuffer> edgeDbi = lmdbDataStoreProvider.createEdgeDB(scope,name+"_"+label);
        if(!edgeDbi.delete(txn,key)) return false;
        key.rewind();
        return true;
    }
}
