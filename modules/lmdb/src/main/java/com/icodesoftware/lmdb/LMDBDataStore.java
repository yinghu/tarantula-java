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

    //NOTES : key+value < 2040 bytes ( 511 bytes for key ; value <= 1521 bytes (2040 - 511 - 8)

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
        if(t.onEdge() && t.label()!=null) lmdbDataStoreProvider.createEdgeDB(scope,name+"_"+t.label());
        BufferCache bufferCache = lmdbDataStoreProvider.fromCache();
        Recoverable.DataBuffer key = bufferCache.key;
        lmdbDataStoreProvider.assign(key);
        key.flip();
        if(!t.readKey(key)) return false;
        Txn<ByteBuffer> txn = env.txnWrite(); //can read also
        Recoverable.DataBuffer value = bufferCache.value;
        value.writeHeader(new LocalHeader(true,Long.MIN_VALUE,t.getFactoryId(),t.getClassId()));
        t.write(value);
        try{
            if(!dbi.put(txn,key.rewind(),value.flip())) return false;
            if(t.onEdge()) onEdge(t.ownerKey(),t.label(),t.key(),txn);
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
        BufferCache cache = lmdbDataStoreProvider.fromCache();
        Recoverable.DataBuffer key = cache.key;
        if(!t.writeKey(key)) return false;
        Txn<ByteBuffer> txn = env.txnWrite(); //can read also
        try{
            if (dbi.get(txn, key.flip()) == null) return false;
            BufferProxy proxy = new BufferProxy(txn.val());
            Recoverable.DataHeader header = proxy.readHeader();
            if(header.revision()!=t.revision()) return false;
            Recoverable.DataBuffer update = cache.value;
            header.update(header.local(),1);
            update.writeHeader(header);
            t.write(update);
            if(!dbi.put(txn,key.rewind(),update.flip())) return false;
            txn.commit();
            t.revision(header.revision());
            key.rewind();
            update.rewind();
            lmdbDataStoreProvider.onDistributing(metadata,key,update);
            return true;
        }finally {
            txn.close();
        }
    }

    @Override
    public <T extends Recoverable> boolean createIfAbsent(T t, boolean loading) {
        if(t.onEdge() && t.label()!=null) lmdbDataStoreProvider.createEdgeDB(scope,name+"_"+t.label());
        BufferCache cache = lmdbDataStoreProvider.fromCache();
        Recoverable.DataBuffer key = cache.key;
        if(!t.writeKey(key)) throw new IllegalArgumentException("Key must be assigned first");
        Txn<ByteBuffer> txn = env.txnWrite(); //can be reading also
        try{
            if (dbi.get(txn, key.flip()) != null) {
                if (!loading) return false;
                BufferProxy proxy = new BufferProxy(txn.val());
                Recoverable.DataHeader h = proxy.readHeader();
                t.read(proxy);
                t.revision(h.revision());
                return false;
            }
            Recoverable.DataBuffer value = cache.value;
            value.writeHeader(new LocalHeader(true,Long.MIN_VALUE,t.getFactoryId(),t.getClassId()));
            t.write(value);
            if (!dbi.put(txn, key.rewind(),value.flip())) throw new RuntimeException("lmdb failure to insert key/value");
            if(t.onEdge()) onEdge(t.ownerKey(),t.label(),t.key(),txn);
            txn.commit();
            t.revision(Long.MIN_VALUE);
            key.rewind();
            value.rewind();
            //lmdbDataStoreProvider.onDistributing(metadata,key,value);
            return true;
        }
        finally {
            txn.close();//rollback if exception
        }
    }

    @Override
    public <T extends Recoverable> boolean load(T t) {
        BufferCache cache = lmdbDataStoreProvider.fromCache();
        Recoverable.DataBuffer key = cache.key;
        if(!t.writeKey(key)) return false;
        Txn<ByteBuffer> txn = env.txnRead(); //read only
        try{
            if (dbi.get(txn, key.flip()) == null) return false;
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
        Txn<ByteBuffer> txn = env.txnWrite();
        try{
            if(!onEdge(t.ownerKey(),label,t.key(),txn)) return false;
            txn.commit();
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
        Txn<ByteBuffer> txn = env.txnWrite();
        if(!offEdge(t,label,edge,txn)) return false;
        txn.commit();
        return true;
    }

    public boolean load(Recoverable.Key key, BufferStream buffer) {
        BufferCache cache = lmdbDataStoreProvider.fromCache();
        Recoverable.DataBuffer akey = cache.key;
        if(!key.write(akey)) return false;
        Txn<ByteBuffer> txn = env.txnRead(); //read only
        try{
            if (dbi.get(txn, akey.flip()) == null) return false;
            BufferProxy data = new BufferProxy(txn.val());
            return buffer.on(new BufferProxy(txn.key()),data.readHeader(),data);
        }finally {
            txn.close();
        }
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
        Dbi<ByteBuffer> edgeDbi = lmdbDataStoreProvider.createEdgeDB(scope,name+"_"+query.label());
        Txn<ByteBuffer> txn = env.txnRead();
        ByteBuffer akey = key.flip();
        CursorIterable<ByteBuffer> cursor = edgeDbi.iterate(txn, KeyRange.closed(akey,akey));
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
                    t.label(query.label());

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
    public boolean set(BufferStream bufferStream) {
        BufferCache cache = lmdbDataStoreProvider.fromCache();
        Recoverable.DataBuffer key = cache.key;
        Recoverable.DataBuffer value = cache.value;
        if(!bufferStream.on(key, null,value)) return false;
        return set(key.flip(),value.flip());
    }
    public boolean set(byte[] key,byte[] value){
        return false;
    }
    @Override
    public byte[] get(byte[] key) {
        return new byte[0];
    }

    @Override
    public void unset(byte[] key) {

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
    public void list(BufferStream binary) {
        final Txn<ByteBuffer> txn = env.txnRead();
        final Cursor<ByteBuffer> cursor = dbi.openCursor(txn);
        while (cursor.next()){
            BufferProxy dataBuffer = new BufferProxy(cursor.val());
            if(!binary.on(new BufferProxy(cursor.key()),dataBuffer.readHeader(),dataBuffer)) break;
        }
        cursor.close();
        txn.close();
    }

    private <T extends Recoverable> boolean onEdge(Recoverable.Key ownerKey, String label, Recoverable.Key edgeKey, Txn<ByteBuffer> txn){
        if(ownerKey ==null || label==null) return false;
        BufferCache cache = lmdbDataStoreProvider.fromCache();
        Recoverable.DataBuffer key = cache.key;
        Recoverable.DataBuffer value = cache.value;
        if(!ownerKey.write(key)) return false;
        if(!edgeKey.write(value)) return false;
        Dbi<ByteBuffer> edgeDbi = lmdbDataStoreProvider.createEdgeDB(scope,name+"_"+label);
        if(!edgeDbi.put(txn,key.flip(),value.flip(), PutFlags.MDB_NODUPDATA)) return false;
        key.rewind();
        value.rewind();
        key.close();
        value.close();
        System.out.println("On edie->"+ownerKey.asString()+">>"+edgeKey.asString());
        return true;
    }
    private <T extends Recoverable> boolean offEdge(Recoverable.Key t,String label,Recoverable.Key edge,Txn<ByteBuffer> txn){
        if(t==null || edge==null || label ==null) return false;
        BufferCache cache = lmdbDataStoreProvider.fromCache();
        Recoverable.DataBuffer key = cache.key;
        Recoverable.DataBuffer value = cache.value;
        if(!t.write(key)) return false;
        if(!edge.write(value)) return false;
        Dbi<ByteBuffer> edgeDbi = lmdbDataStoreProvider.createEdgeDB(scope,name+"_"+label);
        if(!edgeDbi.delete(txn,key.flip(),value.flip())) return false;
        key.rewind();
        //edge.rewind();
        return true;
    }
    private <T extends Recoverable> boolean offEdge(Recoverable.Key t,String label,Txn<ByteBuffer> txn){
        if(t==null || label ==null) return false;
        BufferCache cache = lmdbDataStoreProvider.fromCache();
        Recoverable.DataBuffer key = cache.key;
        if(!t.write(key)) return false;
        Dbi<ByteBuffer> edgeDbi = lmdbDataStoreProvider.createEdgeDB(scope,name+"_"+label);
        if(!edgeDbi.delete(txn,key.flip())) return false;
        key.rewind();
        key.close();
        cache.value.close();
        return true;
    }
}
