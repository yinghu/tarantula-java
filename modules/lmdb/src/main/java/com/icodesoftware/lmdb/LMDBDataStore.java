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

    private final Metadata metadata;

    private int scope;

    //NOTES : key+value < 2032 bytes ( 511 bytes for key ; value <= 1521 bytes (2032 - 511 - 8)

    private final LMDBDataStoreProvider lmdbDataStoreProvider;
    public LMDBDataStore(int scope,String name, Dbi<ByteBuffer> dbi,Env<ByteBuffer> env,LMDBDataStoreProvider lmdbDataStoreProvider){
        this.metadata = new LocalMetadata(scope,name);
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
        Txn<ByteBuffer> txn = env.txnRead();
        Stat st = dbi.stat(txn);
        long count = st.entries;
        txn.close();
        return count;
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
        if(t.onEdge() && t.label()!=null) lmdbDataStoreProvider.createEdgeDB(scope,name,t.label());
        BufferCache cache = lmdbDataStoreProvider.fromCache();
        Recoverable.DataBuffer key = cache.key;
        lmdbDataStoreProvider.assign(key);
        key.flip();
        if(!t.readKey(key)) return false;
        Recoverable.DataBuffer value = cache.value;
        value.writeHeader(new LocalHeader(true,Long.MIN_VALUE,t.getFactoryId(),t.getClassId()));
        if(!t.write(value)) return false;
        Txn<ByteBuffer> txn = env.txnWrite(); //can read also
        try{
            if(!dbi.put(txn,key.rewind(),value.flip())) throw new RuntimeException("lmdb failure to insert key/value");
            if(t.onEdge()) onEdge(t.ownerKey(),t.label(),t.key(),txn);
            txn.commit();
            key.rewind();
            value.rewind();
            t.revision(Long.MIN_VALUE);
            lmdbDataStoreProvider.onDistributing(metadata,key,value);
            return true;
        }finally {
            txn.close();
            cache.reset();
        }
    }

    @Override
    public <T extends Recoverable> boolean update(T t) {
        BufferCache cache = lmdbDataStoreProvider.fromCache();
        Recoverable.DataBuffer key = cache.key;
        if(!t.writeKey(key)) return false;
        Txn<ByteBuffer> txn = env.txnWrite(); //can read also
        boolean updated = false;
        try{
            if (dbi.get(txn, key.flip()) != null){
                Recoverable.DataBuffer proxy = BufferProxy.buffer(txn.val());
                Recoverable.DataHeader header = proxy.readHeader();
                if(header.revision() == t.revision()){
                    Recoverable.DataBuffer update = cache.value;
                    header.update(header.local(),1);
                    update.writeHeader(header);
                    t.write(update);
                    if(!dbi.put(txn,key.rewind(),update.flip()))  throw new RuntimeException("lmdb failure to insert key/value");
                    txn.commit();
                    t.revision(header.revision());
                    key.rewind();
                    update.rewind();
                    lmdbDataStoreProvider.onDistributing(metadata,key,update);
                    updated = true;
                }
            }
        }finally {
            txn.close();
        }
        if(updated) return true;
        key.rewind();
        Recoverable.DataBuffer value = cache.value;
        if(!lmdbDataStoreProvider.onRecovering(metadata,key,value)) return false;
        Txn<ByteBuffer> xtxn = env.txnWrite();
        try{
            value.flip();
            Recoverable.DataHeader header = value.readHeader();
            if(header.revision() != t.revision()) return false;
            value.clear();
            header.update(true,1);
            value.writeHeader(header);
            t.revision(header.revision());
            if(!t.write(value)) return false;
            if(!dbi.put(xtxn,key.rewind(),value.flip())) throw new RuntimeException("lmdb failure to insert key/value");
            xtxn.commit();
        }
        finally {
            xtxn.close();
            cache.reset();
        }
        return true;
    }

    @Override
    public <T extends Recoverable> boolean createIfAbsent(T t, boolean loading) {
        if(t.onEdge() && t.label()!=null) lmdbDataStoreProvider.createEdgeDB(scope,name,t.label());
        BufferCache cache = lmdbDataStoreProvider.fromCache();
        Recoverable.DataBuffer key = cache.key;
        Recoverable.DataBuffer value = cache.value;
        if(!t.writeKey(key)) throw new IllegalArgumentException("Key must be assigned first");
        boolean existed = get(key.flip(),(k,v)->{
            if(loading){
                Recoverable.DataHeader h = v.readHeader();
                t.read(v);
                t.revision(h.revision());
            }
            return true;
        });
        if(existed) return false;
        existed = lmdbDataStoreProvider.onRecovering(metadata,key,value);
        Txn<ByteBuffer> txn = env.txnWrite(); //can be reading also
        try{
            if(existed){
              if(!dbi.put(txn,key.rewind(),value.flip())) throw new RuntimeException("lmdb failure to insert key/value");
              txn.commit();
              if(!loading) return false;
              value.rewind();
              Recoverable.DataHeader h = value.readHeader();
              t.read(value);
              t.revision(h.revision());
              return false;
            }
            value.writeHeader(new LocalHeader(true,Long.MIN_VALUE,t.getFactoryId(),t.getClassId()));
            t.write(value);
            if (!dbi.put(txn, key.rewind(),value.flip())) throw new RuntimeException("lmdb failure to insert key/value");
            if(t.onEdge()) onEdge(t.ownerKey(),t.label(),t.key(),txn);
            txn.commit();
            t.revision(Long.MIN_VALUE);
            key.rewind();
            value.rewind();
            lmdbDataStoreProvider.onDistributing(metadata,key,value);
            return true;
        }
        finally {
            txn.close();//rollback if exception
            cache.reset();
        }

    }

    @Override
    public <T extends Recoverable> boolean load(T t) {
        boolean loaded = get(t.key(),(k,v)->{
            Recoverable.DataHeader h = v.readHeader();
            t.read(v);
            t.revision(h.revision());
            return true;
        });
        if(loaded) return true;
        BufferCache cache = lmdbDataStoreProvider.fromCache();
        Recoverable.DataBuffer key = cache.key;
        if(!t.writeKey(key)) return false;
        Recoverable.DataBuffer value = cache.value;
        key.flip();
        if(!lmdbDataStoreProvider.onRecovering(metadata,key,value)) return false;
        Txn<ByteBuffer> txn = env.txnWrite(); //read only
        try{
            if (!dbi.put(txn, key.rewind(),value.flip())) throw new RuntimeException("lmdb failure to insert key/value");
            txn.commit();
            value.rewind();
            Recoverable.DataHeader header = value.readHeader();
            t.read(value);
            t.revision(header.revision());
            return true;
        }finally {
            txn.close();
            cache.reset();
        }

    }

    public <T extends Recoverable> boolean createEdge(T t,String label){
        lmdbDataStoreProvider.createEdgeDB(scope,name,label);
        Txn<ByteBuffer> txn = env.txnWrite();
        try{
            if(!onEdge(t.ownerKey(),label,t.key(),txn)) return false;
            txn.commit();
            return true;
        }finally {
            txn.close();
        }
    }

    public  boolean deleteEdge(Recoverable.Key t,String label){
        Txn<ByteBuffer> txn = env.txnWrite();
        if(!offEdge(t,label,txn)) return false;
        txn.commit();
        return true;
    }
    public boolean deleteEdge(Recoverable.Key t,Recoverable.Key edge,String label){
        Txn<ByteBuffer> txn = env.txnWrite();
        if(!offEdge(t,label,edge,txn)) return false;
        txn.commit();
        return true;
    }
    public <T extends Recoverable> boolean delete(T t){
        BufferCache cache = lmdbDataStoreProvider.fromCache();
        Recoverable.DataBuffer key = cache.key;
        t.writeKey(key);
        Txn<ByteBuffer> txn = env.txnWrite();
        try{
            if(!dbi.delete(txn, key.flip())) return false;
            key.rewind();
            offEdge(t.ownerKey(),t.label(),t.key(),txn);
            txn.commit();
            return true;
        }finally {
            txn.close();
            cache.reset();
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
        BufferCache cache = lmdbDataStoreProvider.fromCache();
        Recoverable.DataBuffer key = cache.key;
        if(!query.key().write(key)) return;
        LocalEdgeDataStore localEdgeDataStore = lmdbDataStoreProvider.createEdgeDB(scope,name,query.label());
        Txn<ByteBuffer> txn = env.txnRead();
        ByteBuffer akey = key.flip();
        CursorIterable<ByteBuffer> cursor = localEdgeDataStore.dbi.iterate(txn, KeyRange.closed(akey,akey));
        try{
            for(Iterator<CursorIterable.KeyVal<ByteBuffer>> it = cursor.iterator();it.hasNext();){
                CursorIterable.KeyVal<ByteBuffer> kv = it.next();
                T t = query.create();
                if(dbi.get(txn,kv.val())!=null){
                    Recoverable.DataBuffer proxy = BufferProxy.buffer(txn.val());
                    Recoverable.DataHeader local = proxy.readHeader();
                    t.read(proxy);
                    t.revision(local.revision());
                    t.readKey(BufferProxy.buffer(kv.val()));
                    t.label(query.label());

                    if(!stream.on(t)) break;
                }
            }
        }finally {
            cursor.close();
            txn.close();
            cache.reset();
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

    //BACKUP METHODS
    public boolean get(Recoverable.Key key, BufferStream buffer) {
        BufferCache cache = lmdbDataStoreProvider.fromCache();
        Recoverable.DataBuffer akey = cache.key;
        if(!key.write(akey)) return false;
        try{
            return get(akey.flip(),buffer);
        }finally {
            cache.reset();
        }
    }

    @Override
    public boolean set(BufferStream bufferStream) {
        BufferCache cache = lmdbDataStoreProvider.fromCache();
        try{
            Recoverable.DataBuffer key = cache.key;
            Recoverable.DataBuffer value = cache.value;
            if(!bufferStream.on(key,value)) return false;
            return set(key.flip(),value.flip());
        }finally {
            cache.reset();
        }
    }

    public void forEachEdgeKey(Recoverable.Key key,String label,BufferStream bufferStream){
        LocalEdgeDataStore localEdgeDataStore = lmdbDataStoreProvider.createEdgeDB(metadata.scope(),name,label);
        BufferCache cache = lmdbDataStoreProvider.fromCache();
        if(!key.write(cache.key)) return;
        Txn<ByteBuffer> txn = env.txnRead();
        ByteBuffer akey = cache.key.flip();
        CursorIterable<ByteBuffer> cursor = localEdgeDataStore.dbi.iterate(txn, KeyRange.closed(akey,akey));
        for(Iterator<CursorIterable.KeyVal<ByteBuffer>> it = cursor.iterator();it.hasNext();){
            CursorIterable.KeyVal<ByteBuffer> kv = it.next();
            bufferStream.on(cache.key,BufferProxy.buffer(kv.val()));
        }
        txn.close();

    }

    private boolean set(ByteBuffer key, ByteBuffer value){
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
    public void forEach(BufferStream stream) {
        final Txn<ByteBuffer> txn = env.txnRead();
        final Cursor<ByteBuffer> cursor = dbi.openCursor(txn);
        while (cursor.next()){
            Recoverable.DataBuffer dataBuffer = BufferProxy.buffer(cursor.val());
            if(!stream.on(BufferProxy.buffer(cursor.key()),dataBuffer)) break;
        }
        cursor.close();
        txn.close();
    }

    private boolean onEdge(Recoverable.Key ownerKey, String label, Recoverable.Key edgeKey, Txn<ByteBuffer> txn){
        if(ownerKey ==null || label==null) return false;
        BufferCache cache = lmdbDataStoreProvider.fromCache();
        Recoverable.DataBuffer key = cache.key;
        Recoverable.DataBuffer value = cache.value;
        if(!ownerKey.write(key)) return false;
        if(!edgeKey.write(value)) return false;
        try{
            LocalEdgeDataStore localEdgeDataStore = lmdbDataStoreProvider.createEdgeDB(scope,name,label);
            if(!localEdgeDataStore.dbi.put(txn,key.flip(),value.flip(), PutFlags.MDB_NODUPDATA)) return false;
            key.rewind();
            value.rewind();
            lmdbDataStoreProvider.onDistributing(localEdgeDataStore.metadata,key,value);
            return true;
        }finally {
            cache.reset();
        }
    }
    private boolean offEdge(Recoverable.Key t,String label,Recoverable.Key edge,Txn<ByteBuffer> txn){
        if(t==null || edge==null || label ==null) return false;
        BufferCache cache = lmdbDataStoreProvider.fromCache();
        Recoverable.DataBuffer key = cache.key;
        Recoverable.DataBuffer value = cache.value;
        if(!t.write(key)) return false;
        if(!edge.write(value)) return false;
        try{
            LocalEdgeDataStore localEdgeDataStore = lmdbDataStoreProvider.createEdgeDB(scope,name,label);
            if(!localEdgeDataStore.dbi.delete(txn,key.flip(),value.flip())) return false;
            key.rewind();
            return true;
        }finally {
            cache.reset();
        }
    }
    private  boolean offEdge(Recoverable.Key t,String label,Txn<ByteBuffer> txn){
        if(t==null || label ==null) return false;
        BufferCache cache = lmdbDataStoreProvider.fromCache();
        Recoverable.DataBuffer key = cache.key;
        if(!t.write(key)) return false;
        try{
            LocalEdgeDataStore localEdgeDataStore = lmdbDataStoreProvider.createEdgeDB(scope,name,label);
            if(!localEdgeDataStore.dbi.delete(txn,key.flip())) return false;
            key.rewind();
            return true;
        } finally {
            cache.reset();
        }
    }

    private boolean get(ByteBuffer key, BufferStream buffer) {
        Txn<ByteBuffer> txn = env.txnRead(); //read only
        try{
            if (dbi.get(txn,key) == null) return false;
            Recoverable.DataBuffer data = BufferProxy.buffer(txn.val());
            return buffer.on(BufferProxy.buffer(txn.key()),data);
        }finally {
            txn.close();
        }
    }
}
