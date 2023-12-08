package com.icodesoftware.lmdb;

import com.icodesoftware.*;
import com.icodesoftware.service.DataStoreSummary;
import com.icodesoftware.service.Metadata;
import org.lmdbjava.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LMDBDataStore implements DataStore,DataStore.Backup ,Closable {

    public final Env<ByteBuffer> env;
    private final Dbi<ByteBuffer> dbi;

    private final String name;

    private final Metadata metadata;

    private int scope;

    //NOTES : key+value < 2032 bytes ( 511 bytes for key ; value <= 1521 bytes (2032 - 511 - 8)

    private final LMDBDataStoreProvider lmdbDataStoreProvider;
    private final Txn<ByteBuffer> ptxn;

    public LMDBDataStore(int scope,String name, Dbi<ByteBuffer> dbi,Env<ByteBuffer> env,LMDBDataStoreProvider lmdbDataStoreProvider,Txn<ByteBuffer> ptxn){
        this.metadata = new LocalMetadata(scope,name);
        this.scope = scope;
        this.name = name;
        this.dbi = dbi;
        this.env = env;
        this.lmdbDataStoreProvider = lmdbDataStoreProvider;
        this.ptxn = ptxn;
    }

    @Override
    public int scope() {
        return scope;
    }

    @Override
    public String name() {
        return name;
    }


    private List<String> edgeList(){
        ArrayList<String> elist = new ArrayList<>();
        return elist;
    }

    @Override
    public void view(DataStoreSummary dataStoreSummary){
        Txn<ByteBuffer> txn = env.txn(ptxn);
        try{
            Stat st = dbi.stat(txn);
            dataStoreSummary.count(st.entries);
            dataStoreSummary.depth(st.depth);
            dataStoreSummary.leafPages(st.leafPages);
            dataStoreSummary.overflowPages(st.overflowPages);
            dataStoreSummary.branchPages(st.branchPages);
            dataStoreSummary.pageSize(st.pageSize);
            dataStoreSummary.edgeList(edgeList());
        }finally {
            txn.close();
        }
    }

    @Override
    public <T extends Recoverable> boolean create(T t) {
        Recoverable.DataBufferPair cache = lmdbDataStoreProvider.dataBufferPair();
        Recoverable.DataBuffer key = cache.key();
        Recoverable.DataBuffer value = cache.value();
        final Txn<ByteBuffer> txn = env.txn(ptxn); //can read also
        try{
            lmdbDataStoreProvider.assign(key);
            key.flip();
            if(!t.readKey(key)) return false;
            value.writeHeader(new LocalHeader(Long.MIN_VALUE,t.getFactoryId(),t.getClassId()));
            if(!t.write(value)) return false;
            if(!dbi.put(txn,key.rewind(),value.flip())) throw new RuntimeException("lmdb failure to insert key/value");
            txn.commit();
            if(t.onEdge()) onEdge(t.ownerKey(),t.label(),t.key());
            key.rewind();
            value.rewind();
            t.revision(Long.MIN_VALUE);
            lmdbDataStoreProvider.onUpdating(metadata,key,value,ptxn.getId());
            return true;
        }finally {
            txn.close();
            cache.reset();
        }
    }

    @Override
    public <T extends Recoverable> boolean update(T t) {
        Recoverable.DataBufferPair cache = lmdbDataStoreProvider.dataBufferPair();
        Recoverable.DataBuffer key = cache.key();
        if(!t.writeKey(key)){
            cache.reset();
            return false;
        }
        final Txn<ByteBuffer> txn = env.txn(ptxn); //can read also
        boolean updated = false;
        try{
            if (dbi.get(txn, key.flip()) != null){
                Recoverable.DataBuffer proxy = BufferProxy.buffer(txn.val());
                Recoverable.DataHeader header = proxy.readHeader();
                if(header.revision() == t.revision()){
                    Recoverable.DataBuffer update = cache.value();
                    header.update(1);
                    update.writeHeader(header);
                    t.write(update);
                    if(!dbi.put(txn,key.rewind(),update.flip()))  throw new RuntimeException("lmdb failure to insert key/value");
                    txn.commit();
                    t.revision(header.revision());
                    key.rewind();
                    update.rewind();
                    lmdbDataStoreProvider.onUpdating(metadata,key,update,ptxn.getId());
                    updated = true;
                }
            }
        }finally {
            txn.close();
            if(updated) cache.reset();
        }
        if(updated) return true;
        key.rewind();
        Recoverable.DataBuffer value = cache.value();
        if(!lmdbDataStoreProvider.onRecovering(metadata,key,value)){
            cache.reset();
            return false;
        }
        final Txn<ByteBuffer> xtxn = env.txn(ptxn);
        try{
            value.flip();
            Recoverable.DataHeader header = value.readHeader();
            if(header.revision() != t.revision()) return false;
            value.clear();
            header.update(1);
            value.writeHeader(header);
            t.revision(header.revision());
            if(!t.write(value)) return false;
            if(!dbi.put(xtxn,key.rewind(),value.flip())) throw new RuntimeException("lmdb failure to insert key/value");
            xtxn.commit();
            key.rewind();
            value.rewind();
            lmdbDataStoreProvider.onUpdating(metadata,key,value,ptxn.getId());
        }
        finally {
            xtxn.close();
            cache.reset();
        }
        return true;
    }

    @Override
    public <T extends Recoverable> boolean createIfAbsent(T t, boolean loading) {
        //if(t.onEdge() && t.label()!=null) lmdbDataStoreProvider.createEdgeDB(scope,name,t.label());
        Recoverable.DataBufferPair cache = lmdbDataStoreProvider.dataBufferPair();
        Recoverable.DataBuffer key = cache.key();
        Recoverable.DataBuffer value = cache.value();
        if(!t.writeKey(key)) {
            cache.reset();
            throw new IllegalArgumentException("Key must be assigned first");
        }
        boolean existed = get(key.flip(),(k,v)->{
            if(loading){
                Recoverable.DataHeader h = v.readHeader();
                t.read(v);
                t.revision(h.revision());
            }
            cache.reset();
            return true;
        });
        if(existed) return false;
        existed = lmdbDataStoreProvider.onRecovering(metadata,key,value);
        Txn<ByteBuffer> txn = env.txn(ptxn); //can be reading also
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
            value.writeHeader(new LocalHeader(Long.MIN_VALUE,t.getFactoryId(),t.getClassId()));
            t.write(value);
            if (!dbi.put(txn, key.rewind(),value.flip())) throw new RuntimeException("lmdb failure to insert key/value");
            txn.commit();
            if(t.onEdge()) onEdge(t.ownerKey(),t.label(),t.key());
            t.revision(Long.MIN_VALUE);
            key.rewind();
            value.rewind();
            lmdbDataStoreProvider.onUpdating(metadata,key,value,ptxn.getId());
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
        Recoverable.DataBufferPair cache = lmdbDataStoreProvider.dataBufferPair();
        Recoverable.DataBuffer key = cache.key();
        if(!t.writeKey(key)) {
            cache.reset();
            return false;
        }
        Recoverable.DataBuffer value = cache.value();
        key.flip();
        if(!lmdbDataStoreProvider.onRecovering(metadata,key,value)) {
            cache.reset();
            return false;
        }
        final Txn<ByteBuffer> txn = env.txn(ptxn);
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
        if(label==null) return false;
        return onEdge(t.ownerKey(),label,t.key());
    }

    public  boolean deleteEdge(Recoverable.Key t,String label){
        return offEdge(t,label);
    }
    public boolean deleteEdge(Recoverable.Key t,Recoverable.Key edge,String label){
       return offEdge(t,label,edge);
    }
    public <T extends Recoverable> boolean delete(T t){
        Recoverable.DataBufferPair cache = lmdbDataStoreProvider.dataBufferPair();
        Recoverable.DataBuffer key = cache.key();
        if(!t.writeKey(key)){
            cache.reset();
            return false;
        }
        final Txn<ByteBuffer> txn = env.txn(ptxn);
        try{
            if(!dbi.delete(txn, key.flip())) return false;
            txn.commit();
            key.rewind();
            lmdbDataStoreProvider.onDeleting(metadata,key, cache.value(),txn.getId());
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
        Recoverable.DataBufferPair cache = lmdbDataStoreProvider.dataBufferPair();
        Recoverable.DataBuffer key = cache.key();
        try{
            if(!query.key().write(key)) return;
            LocalEdgeDataStore localEdgeDataStore = lmdbDataStoreProvider.localEdgeDataStore(scope,name,query.label(),ptxn);
            if(list(key.flip(),localEdgeDataStore,query,stream)) return;
            if(lmdbDataStoreProvider.onRecovering(localEdgeDataStore.metadata,key,(e,v)->{
                set(e.rewind(),v.rewind());
                e.rewind();
                key.rewind();
                setEdge(query.label(),(ek,ev)->{
                    for(byte b: key.array()){
                        ek.writeByte(b);
                    }
                    for(byte b: e.array()){
                        ev.writeByte(b);
                    }
                    return true;
                });
                return true;
            })){
                list(key.rewind(),localEdgeDataStore,query,stream);
            }
        }finally {
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
        Recoverable.DataBufferPair cache = lmdbDataStoreProvider.dataBufferPair();
        Recoverable.DataBuffer akey = cache.key();
        try{
            if(!key.write(akey)) return false;
            return get(akey.flip(),buffer);
        }finally {
            cache.reset();
        }
    }

    @Override
    public boolean set(BufferStream bufferStream) {
        Recoverable.DataBufferPair cache = lmdbDataStoreProvider.dataBufferPair();
        try{
            Recoverable.DataBuffer key = cache.key();
            Recoverable.DataBuffer value = cache.value();
            if(!bufferStream.on(key,value)) return false;
            return set(key.flip(),value.flip());
        }finally {
            cache.reset();
        }
    }

    public void forEachEdgeKey(Recoverable.Key key,String label,BufferStream bufferStream){
        LocalEdgeDataStore localEdgeDataStore = lmdbDataStoreProvider.localEdgeDataStore(metadata.scope(),name,label,ptxn);
        Recoverable.DataBufferPair cache = lmdbDataStoreProvider.dataBufferPair();
        if(!key.write(cache.key())) return;
        ByteBuffer akey = cache.key().flip();
        final Txn<ByteBuffer> txn = env.txn(ptxn);
        CursorIterable<ByteBuffer> cursor = localEdgeDataStore.dbi.iterate(txn, KeyRange.closed(akey,akey));
        try{
            for(Iterator<CursorIterable.KeyVal<ByteBuffer>> it = cursor.iterator();it.hasNext();){
                CursorIterable.KeyVal<ByteBuffer> kv = it.next();
                bufferStream.on(cache.key(),BufferProxy.buffer(kv.val()));
            }
        }finally {
            cursor.close();
            txn.close();
            cache.reset();
        }

    }

    public void forEachEdgeKeyValue(Recoverable.Key key,String label,BufferStream bufferStream){
        LocalEdgeDataStore localEdgeDataStore = lmdbDataStoreProvider.localEdgeDataStore(metadata.scope(),name,label,ptxn);
        Recoverable.DataBufferPair cache = lmdbDataStoreProvider.dataBufferPair();
        if(!key.write(cache.key())) return;
        ByteBuffer akey = cache.key().flip();
        final Txn<ByteBuffer> txn = env.txn(ptxn);
        CursorIterable<ByteBuffer> cursor = localEdgeDataStore.dbi.iterate(txn, KeyRange.closed(akey,akey));
        try{
            for(Iterator<CursorIterable.KeyVal<ByteBuffer>> it = cursor.iterator();it.hasNext();){
                CursorIterable.KeyVal<ByteBuffer> kv = it.next();
                if(dbi.get(txn,kv.val())==null) continue;
                bufferStream.on(BufferProxy.buffer(kv.val().rewind()),BufferProxy.buffer(txn.val()));
            }
        }finally {
            cursor.close();
            txn.close();
            cache.reset();
        }
    }

    @Override
    public void forEach(BufferStream stream) {
        final Txn<ByteBuffer> txn = env.txn(ptxn);
        final Cursor<ByteBuffer> cursor = dbi.openCursor(txn);
        try{
            while (cursor.next()){
                Recoverable.DataBuffer dataBuffer = BufferProxy.buffer(cursor.val());
                if(!stream.on(BufferProxy.buffer(cursor.key()),dataBuffer)) break;
            }
        }finally {
            cursor.close();
            txn.close();
        }
    }

    public boolean setEdge(String label,BufferStream bufferStream){
        if(label==null) return false;
        Recoverable.DataBufferPair cache = lmdbDataStoreProvider.dataBufferPair();
        LocalEdgeDataStore localEdgeDataStore = lmdbDataStoreProvider.localEdgeDataStore(scope,name,label,ptxn);
        final Txn<ByteBuffer> txn = env.txn(ptxn);
        try{
            if(!bufferStream.on(cache.key(),cache.value())) return false;
            if(!localEdgeDataStore.dbi.put(txn,cache.key().flip(),cache.value().flip())) return false;
            txn.commit();
            return true;
        }finally {
            txn.close();
            cache.reset();
        }
    }

    public boolean unsetEdge(String label,BufferStream bufferStream,boolean fromLabel){
        if(label==null) return false;
        Recoverable.DataBufferPair cache = lmdbDataStoreProvider.dataBufferPair();
        LocalEdgeDataStore localEdgeDataStore = lmdbDataStoreProvider.localEdgeDataStore(scope,name,label,ptxn);
        final Txn<ByteBuffer> txn = env.txn(ptxn);
        try{
            Recoverable.DataBuffer key = cache.key();
            Recoverable.DataBuffer value = cache.value();
            if(!bufferStream.on(key,value)) return false;
            if(fromLabel){
                if(!localEdgeDataStore.dbi.delete(txn,key.flip())) return false;
            }
            else {
                if (!localEdgeDataStore.dbi.delete(txn, key.flip(), value.flip())) return false;
            }
            txn.commit();
            return true;
        }finally {
            txn.close();
            cache.reset();
        }
    }
    public boolean unset(BufferStream bufferStream){
        Recoverable.DataBufferPair cache = lmdbDataStoreProvider.dataBufferPair();
        final Txn<ByteBuffer> txn = env.txn(ptxn);
        try{
            Recoverable.DataBuffer key = cache.key();
            Recoverable.DataBuffer value = cache.value();
            if(!bufferStream.on(key,value)) return false;
            if(!dbi.delete(txn,key.flip())) return false;
            txn.commit();
            return true;
        }finally {
            txn.close();
            cache.reset();
        }
    }
    //help methods

    private <T extends Recoverable> boolean list(ByteBuffer key,LocalEdgeDataStore localEdgeDataStore,RecoverableFactory<T> query, Stream<T> stream){
        final Txn<ByteBuffer> txn = env.txn(ptxn);
        CursorIterable<ByteBuffer> cursor = localEdgeDataStore.dbi.iterate(txn, KeyRange.closed(key,key));
        int[] matched ={0,0};
        try{
            for(Iterator<CursorIterable.KeyVal<ByteBuffer>> it = cursor.iterator();it.hasNext();){
                CursorIterable.KeyVal<ByteBuffer> kv = it.next();
                T t = query.create();
                matched[0]++;
                if(dbi.get(txn,kv.val())!=null){
                    matched[1]++;
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
        }
        int ifOdd = matched[0]+matched[1];
        return  (ifOdd >0 && ifOdd % 2 ==0);
    }
    private boolean set(ByteBuffer key, ByteBuffer value){
        final Txn<ByteBuffer> txn = env.txn(ptxn);
        try{
            if(!dbi.put(txn,key,value)) return false;
            txn.commit();
            return true;
        }finally {
            txn.close();
        }
    }

    private boolean onEdge(Recoverable.Key ownerKey, String label, Recoverable.Key edgeKey){
        if(ownerKey ==null || label==null) return false;
        Recoverable.DataBufferPair cache = lmdbDataStoreProvider.dataBufferPair();
        Recoverable.DataBuffer key = cache.key();
        Recoverable.DataBuffer value = cache.value();
        LocalEdgeDataStore localEdgeDataStore = lmdbDataStoreProvider.localEdgeDataStore(scope,name,label,ptxn);
        Txn<ByteBuffer> txn = env.txn(ptxn);
        try{
            if(!ownerKey.write(key)) return false;
            if(!edgeKey.write(value)) return false;

            if(!localEdgeDataStore.dbi.put(txn,key.flip(),value.flip(), PutFlags.MDB_NODUPDATA)) return false;//no duplicate entry

            txn.commit();
            key.rewind();
            value.rewind();
            lmdbDataStoreProvider.onUpdating(localEdgeDataStore.metadata,key,value,ptxn.getId());
            return true;
        }finally {
            txn.close();
            cache.reset();
        }
    }
    private boolean offEdge(Recoverable.Key t,String label,Recoverable.Key edge){
        if(t==null || edge==null || label ==null) return false;
        Recoverable.DataBufferPair cache = lmdbDataStoreProvider.dataBufferPair();
        Recoverable.DataBuffer key = cache.key();
        Recoverable.DataBuffer value = cache.value();
        LocalEdgeDataStore localEdgeDataStore = lmdbDataStoreProvider.localEdgeDataStore(scope,name,label,ptxn);
        Txn<ByteBuffer> txn = env.txn(ptxn);
        try{
            if(!t.write(key)) return false;
            if(!edge.write(value)) return false;
            if(!localEdgeDataStore.dbi.delete(txn,key.flip(),value.flip())) return false;
            txn.commit();
            key.rewind();
            value.rewind();
            lmdbDataStoreProvider.onDeleting(localEdgeDataStore.metadata,key,value,ptxn.getId());
            return true;
        }finally {
            txn.close();
            cache.reset();
        }
    }
    private  boolean offEdge(Recoverable.Key t,String label){
        if(t==null || label ==null) return false;
        Recoverable.DataBufferPair cache = lmdbDataStoreProvider.dataBufferPair();
        Recoverable.DataBuffer key = cache.key();
        try{
            if(!t.write(key)) return false;
            LocalEdgeDataStore localEdgeDataStore = lmdbDataStoreProvider.localEdgeDataStore(scope,name,label,ptxn);
            Txn<ByteBuffer> txn = env.txn(ptxn);
            if(!localEdgeDataStore.dbi.delete(txn,key.flip())) return false;
            txn.commit();
            key.rewind();
            this.lmdbDataStoreProvider.onDeleting(localEdgeDataStore.metadata,key,null,ptxn.getId());
            return true;
        } finally {
            cache.reset();
        }
    }

    private boolean get(ByteBuffer key, BufferStream buffer) {
        final Txn<ByteBuffer> txn = env.txn(ptxn); //read only
        try{
            if (dbi.get(txn,key) == null) return false;
            Recoverable.DataBuffer data = BufferProxy.buffer(txn.val());
            return buffer.on(BufferProxy.buffer(key.rewind()),data);
        }finally {
            txn.close();
        }
    }

}
