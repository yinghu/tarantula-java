package com.icodesoftware.lmdb;

import com.icodesoftware.*;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.DataStoreSummary;
import com.icodesoftware.service.Metadata;
import org.lmdbjava.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class CachedLMDBDataStore implements DataStore,DataStore.Backup ,Closable {

    private TarantulaLogger logger = JDKLogger.getLogger(CachedLMDBDataStore.class);


    public final Env<ByteBuffer> env;
    private final Dbi<ByteBuffer> dbi;
    private final LocalEdgeDataStore idx;

    private final String name;

    private final Metadata metadata;

    private int scope;

    private final ConcurrentHashMap<String,String> edgeIndex;
    private final static String IDX_EDGE = "I_D_X";
    private final static ByteBuffer IDX_KEY = ByteBuffer.allocateDirect(8).putLong(1L).flip();

    //NOTES : key+value < 2032 bytes ( 511 bytes for key ; value <= 1521 bytes (2032 - 511 - 8)

    private final LMDBDataStoreProvider lmdbDataStoreProvider;

    public CachedLMDBDataStore(int scope, String name, Dbi<ByteBuffer> dbi, Env<ByteBuffer> env, LMDBDataStoreProvider lmdbDataStoreProvider){
        this.edgeIndex = new ConcurrentHashMap<>();
        this.metadata = new LocalMetadata(scope,name);
        this.scope = scope;
        this.name = name;
        this.dbi = dbi;
        this.env = env;
        this.lmdbDataStoreProvider = lmdbDataStoreProvider;
        idx = this.lmdbDataStoreProvider.createEdgeDB(scope,name,IDX_EDGE);
        IDX_KEY.getLong();
        loadEdges(null);
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
        edgeIndex.forEach((k,v)->elist.add(k));
        return elist;
    }

    @Override
    public void view(DataStoreSummary dataStoreSummary){
        Txn<ByteBuffer> txn = env.txnRead();
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
        //create edge db before txn creation
        if(t.onEdge() && t.label()!=null) lmdbDataStoreProvider.createEdgeDB(scope,name,t.label());
        Recoverable.DataBufferPair cache = lmdbDataStoreProvider.dataBufferPair();
        Recoverable.DataBuffer key = cache.key();
        Recoverable.DataBuffer value = cache.value();
        lmdbDataStoreProvider.assign(key);
        key.flip();
        if(!t.readKey(key)) return false;
        value.writeHeader(new LocalHeader(true,Long.MIN_VALUE,t.getFactoryId(),t.getClassId()));
        if(!t.write(value)) return false;
        final Txn<ByteBuffer> txn = env.txnWrite(); //can read also
        try {
            if (!dbi.put(txn, key.rewind(), value.flip())) throw new RuntimeException("lmdb failure to insert key/value");
            if (t.onEdge()) onEdge(t.ownerKey(), t.label(), t.key(), txn);
            key.rewind();
            value.rewind();
            t.revision(Long.MIN_VALUE);
            lmdbDataStoreProvider.onUpdating(metadata,key,value,txn.getId());
            lmdbDataStoreProvider.onCommit(metadata.scope(),txn.getId());
            txn.commit();
            return true;
        }catch(Exception ex){
            txn.abort();
            lmdbDataStoreProvider.onAbort(metadata.scope(),txn.getId());
            logger.error("Error on create",ex);
            return false;
        }
        finally {
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
        final Txn<ByteBuffer> txn = env.txnWrite(); //can read also
        boolean updated = false;
        try{
            if (dbi.get(txn, key.flip()) != null){
                Recoverable.DataBuffer proxy = BufferProxy.buffer(txn.val());
                Recoverable.DataHeader header = proxy.readHeader();
                if(header.revision() == t.revision()){
                    Recoverable.DataBuffer update = cache.value();
                    header.update(header.local(),1);
                    update.writeHeader(header);
                    t.write(update);
                    if(!dbi.put(txn,key.rewind(),update.flip()))  throw new RuntimeException("lmdb failure to insert key/value");
                    txn.commit();
                    t.revision(header.revision());
                    key.rewind();
                    update.rewind();
                    lmdbDataStoreProvider.onUpdating(metadata,key,update,txn.getId());
                    lmdbDataStoreProvider.onCommit(metadata.scope(),txn.getId());
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
        final Txn<ByteBuffer> xtxn = env.txnWrite();
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
            key.rewind();
            value.rewind();
            lmdbDataStoreProvider.onUpdating(metadata,key,value,xtxn.getId());
            lmdbDataStoreProvider.onCommit(metadata.scope(),txn.getId());
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
            lmdbDataStoreProvider.onUpdating(metadata,key,value,txn.getId());
            lmdbDataStoreProvider.onCommit(metadata.scope(),txn.getId());
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
        final Txn<ByteBuffer> txn = env.txnWrite(); //read only
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
        lmdbDataStoreProvider.createEdgeDB(scope,name,label);
        Txn<ByteBuffer> txn = env.txnWrite();
        try{
            if(!onEdge(t.ownerKey(),label,t.key(),txn)) return false;
            txn.commit();
            lmdbDataStoreProvider.onCommit(metadata.scope(),txn.getId());
            return true;
        }finally {
            txn.close();
        }
    }


    public  boolean deleteEdge(Recoverable.Key t,String label){
        final Txn<ByteBuffer> txn = env.txnWrite();
        try{
            if(!offEdge(t,label,txn)) return false;
            txn.commit();
            return true;
        }finally {
            txn.close();
        }
    }
    public boolean deleteEdge(Recoverable.Key t,Recoverable.Key edge,String label){
        final Txn<ByteBuffer> txn = env.txnWrite();
        try{
            if(!offEdge(t,label,edge,txn)) return false;
            txn.commit();
            return true;
        }finally {
            txn.close();
        }
    }
    public <T extends Recoverable> boolean delete(T t){
        Recoverable.DataBufferPair cache = lmdbDataStoreProvider.dataBufferPair();
        Recoverable.DataBuffer key = cache.key();
        if(!t.writeKey(key)){
            cache.reset();
            return false;
        }
        key.flip();
        if(!lmdbDataStoreProvider.onDeleting(metadata,key, cache.value(),0)){
            cache.reset();
            return false;
        }
        final Txn<ByteBuffer> txn = env.txnWrite();
        try{
            if(!dbi.delete(txn, key.rewind())) return false;
            removeEdges(txn,key.rewind());
            txn.commit();
            key.rewind();
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
            LocalEdgeDataStore localEdgeDataStore = lmdbDataStoreProvider.createEdgeDB(scope,name,query.label());
            if(list(key.flip(),localEdgeDataStore,query,stream)) return;
            if(lmdbDataStoreProvider.onRecovering(localEdgeDataStore.metadata,key,(e,v)->{
                ByteBuffer ek1 = e.flip();
                ByteBuffer ev1 = v.flip();
                set(ek1,ev1);
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
        LocalEdgeDataStore localEdgeDataStore = lmdbDataStoreProvider.createEdgeDB(metadata.scope(),name,label);
        Recoverable.DataBufferPair cache = lmdbDataStoreProvider.dataBufferPair();
        if(!key.write(cache.key())) return;
        ByteBuffer akey = cache.key().flip();
        final Txn<ByteBuffer> txn = env.txnRead();
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
        LocalEdgeDataStore localEdgeDataStore = lmdbDataStoreProvider.createEdgeDB(metadata.scope(),name,label);
        Recoverable.DataBufferPair cache = lmdbDataStoreProvider.dataBufferPair();
        if(!key.write(cache.key())) return;
        ByteBuffer akey = cache.key().flip();
        final Txn<ByteBuffer> txn = env.txnRead();
        CursorIterable<ByteBuffer> cursor = localEdgeDataStore.dbi.iterate(txn, KeyRange.closed(akey,akey));
        try{
            for(Iterator<CursorIterable.KeyVal<ByteBuffer>> it = cursor.iterator();it.hasNext();){
                CursorIterable.KeyVal<ByteBuffer> kv = it.next();
                if(dbi.get(txn,kv.val())==null){
                   continue;
                }
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
        final Txn<ByteBuffer> txn = env.txnRead();
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
        LocalEdgeDataStore localEdgeDataStore = lmdbDataStoreProvider.createEdgeDB(scope,name,label);
        final Txn<ByteBuffer> txn = env.txnWrite();
        try{
            if(!bufferStream.on(cache.key(),cache.value())) return false;
            if(!localEdgeDataStore.dbi.put(txn,cache.key().flip(),cache.value().flip())) return false;
            idx.dbi.put(txn,cache.value().rewind(),cache.key().rewind(),PutFlags.MDB_NODUPDATA);
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
        LocalEdgeDataStore localEdgeDataStore = lmdbDataStoreProvider.createEdgeDB(scope,name,label);
        final Txn<ByteBuffer> txn = env.txnWrite();
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
        final Txn<ByteBuffer> txn = env.txnWrite();
        try{
            Recoverable.DataBuffer key = cache.key();
            Recoverable.DataBuffer value = cache.value();
            if(!bufferStream.on(key,value)) return false;
            if(!dbi.delete(txn,key.flip())) return false;
            removeEdges(txn,key.rewind());
            txn.commit();
            return true;
        }finally {
            txn.close();
            cache.reset();
        }
    }
    //help methods

    private void loadEdges(Txn<ByteBuffer> ptxn){
        final Txn<ByteBuffer> txn = ptxn==null?env.txnRead():env.txn(ptxn);
        try {
            IDX_KEY.rewind();
            CursorIterable<ByteBuffer> cursor = idx.dbi.iterate(txn, KeyRange.closed(IDX_KEY,IDX_KEY));
            for (Iterator<CursorIterable.KeyVal<ByteBuffer>> it = cursor.iterator(); it.hasNext(); ) {
                CursorIterable.KeyVal<ByteBuffer> kv = it.next();
                String edge = new String(BufferProxy.buffer(kv.val()).array());
                edgeIndex.put(edge,edge);
            }
            cursor.close();
        }finally {
            txn.close();
        }
    }
    private void setEdge(Txn<ByteBuffer> txn,String edge){
        if(edgeIndex.putIfAbsent(edge,edge)==null){
            byte[] bytes = edge.getBytes();
            ByteBuffer lbl = ByteBuffer.allocateDirect(bytes.length).put(bytes).flip();
            if(!idx.dbi.put(txn,IDX_KEY.rewind(),lbl, PutFlags.MDB_NODUPDATA)) throw new RuntimeException("lmdb failure to insert key/value");
        }
    }
    private void removeEdges(Txn<ByteBuffer> txn,ByteBuffer key){
        CursorIterable<ByteBuffer> cursor = idx.dbi.iterate(txn, KeyRange.closed(key,key));
        for (Iterator<CursorIterable.KeyVal<ByteBuffer>> it = cursor.iterator(); it.hasNext(); ) {
            CursorIterable.KeyVal<ByteBuffer> kv = it.next();
            it.remove();
            edgeIndex.forEach((k,v)->{
                LocalEdgeDataStore localEdgeDataStore = lmdbDataStoreProvider.createEdgeDB(scope,name,k);
                key.rewind();
                localEdgeDataStore.dbi.delete(txn,kv.val(),key);
                kv.val().rewind();
            });
        }
        cursor.close();
    }

    private <T extends Recoverable> boolean list(ByteBuffer key,LocalEdgeDataStore localEdgeDataStore,RecoverableFactory<T> query, Stream<T> stream){
        final Txn<ByteBuffer> txn = env.txnRead();
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
        final Txn<ByteBuffer> txn = env.txnWrite();
        try{
            if(!dbi.put(txn,key,value)) return false;
            txn.commit();
            return true;
        }finally {
            txn.close();
        }
    }

    private boolean onEdge(Recoverable.Key ownerKey, String label, Recoverable.Key edgeKey, Txn<ByteBuffer> txn){
        if(ownerKey ==null || label==null) return false;
        Recoverable.DataBufferPair cache = lmdbDataStoreProvider.dataBufferPair();
        Recoverable.DataBuffer key = cache.key();
        Recoverable.DataBuffer value = cache.value();
        try{
            if(!ownerKey.write(key)) return false;
            if(!edgeKey.write(value)) return false;
            setEdge(txn,label);
            LocalEdgeDataStore localEdgeDataStore = lmdbDataStoreProvider.createEdgeDB(scope,name,label);
            if(!localEdgeDataStore.dbi.put(txn,key.flip(),value.flip(), PutFlags.MDB_NODUPDATA)) return false;//no duplicate entry
            idx.dbi.put(txn,value.rewind(),key.rewind(),PutFlags.MDB_NODUPDATA);
            key.rewind();
            value.rewind();
            lmdbDataStoreProvider.onUpdating(localEdgeDataStore.metadata,key,value,txn.getId());
            return true;
        }finally {
            cache.reset();
        }
    }
    private boolean offEdge(Recoverable.Key t,String label,Recoverable.Key edge,Txn<ByteBuffer> txn){
        if(t==null || edge==null || label ==null) return false;
        Recoverable.DataBufferPair cache = lmdbDataStoreProvider.dataBufferPair();
        Recoverable.DataBuffer key = cache.key();
        Recoverable.DataBuffer value = cache.value();
        try{
            if(!t.write(key)) return false;
            if(!edge.write(value)) return false;
            LocalEdgeDataStore localEdgeDataStore = lmdbDataStoreProvider.createEdgeDB(scope,name,label);
            if(!localEdgeDataStore.dbi.delete(txn,key.flip(),value.flip())) return false;
            key.rewind();
            value.rewind();
            lmdbDataStoreProvider.onDeleting(localEdgeDataStore.metadata,key,value,txn.getId());
            return true;
        }finally {
            cache.reset();
        }
    }
    private  boolean offEdge(Recoverable.Key t,String label,Txn<ByteBuffer> txn){
        if(t==null || label ==null) return false;
        Recoverable.DataBufferPair cache = lmdbDataStoreProvider.dataBufferPair();
        Recoverable.DataBuffer key = cache.key();
        try{
            if(!t.write(key)) return false;
            LocalEdgeDataStore localEdgeDataStore = lmdbDataStoreProvider.createEdgeDB(scope,name,label);
            if(!localEdgeDataStore.dbi.delete(txn,key.flip())) return false;
            key.rewind();
            this.lmdbDataStoreProvider.onDeleting(localEdgeDataStore.metadata,key,null,txn.getId());
            return true;
        } finally {
            cache.reset();
        }
    }

    private boolean get(ByteBuffer key, BufferStream buffer) {
        final Txn<ByteBuffer> txn = env.txnRead(); //read only
        try{
            if (dbi.get(txn,key) == null) return false;
            Recoverable.DataBuffer data = BufferProxy.buffer(txn.val());
            return buffer.on(BufferProxy.buffer(key.rewind()),data);
        }finally {
            txn.close();
        }
    }
}
