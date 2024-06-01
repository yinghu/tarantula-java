package com.icodesoftware.lmdb;

import com.icodesoftware.*;
import com.icodesoftware.service.DataStoreSummary;
import com.icodesoftware.service.Metadata;
import org.lmdbjava.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;


public class CachedLMDBDataStore implements DataStore,DataStore.Backup ,Closable {



    public final Env<ByteBuffer> env;
    private final Dbi<ByteBuffer> dbi;

    private final String name;

    private final Metadata metadata;

    private int scope;

    //NOTES : key+value < 2032 bytes ( 511 bytes for key ; value <= 1521 bytes (2032 - 511 - 8)

    private final LMDBDataStoreProvider lmdbDataStoreProvider;

    public CachedLMDBDataStore(int scope, String name, Dbi<ByteBuffer> dbi, Env<ByteBuffer> env, LMDBDataStoreProvider lmdbDataStoreProvider){
        this.metadata = new LocalMetadata(scope,name);
        this.scope = scope;
        this.name = name;
        this.dbi = dbi;
        this.env = env;
        this.lmdbDataStoreProvider = lmdbDataStoreProvider;
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
        //env.getDbiNames().forEach(n->System.out.println(new String(n)));
        ArrayList<String> elist = new ArrayList<>();
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
        if(!t.readKey(key)){
            cache.reset();
            return false;
        }
        value.writeHeader(new LocalHeader(Long.MIN_VALUE,t.getFactoryId(),t.getClassId()));
        if(!t.write(value)){
            cache.reset();
            return false;
        }
        final Txn<ByteBuffer> txn = env.txnWrite(); //can read also
        final long transactionId = txn.getId();
        try {
            if (!dbi.put(txn, key.rewind(), value.flip())) throw new RuntimeException("lmdb failure to insert key/value");
            if (t.onEdge()) onEdge(t.ownerKey(), t.label(), t.key(), txn);
            key.rewind();
            value.rewind();
            t.revision(Long.MIN_VALUE);
            lmdbDataStoreProvider.onUpdating(metadata,key,value,transactionId);
            lmdbDataStoreProvider.onCommit(metadata.scope(),transactionId);
            txn.commit();
            return true;
        }catch(Exception ex){
            txn.abort();
            lmdbDataStoreProvider.onAbort(metadata.scope(),transactionId);
            return false;
        }
        finally {
            txn.close();
            cache.reset();
            lmdbDataStoreProvider.metricsListener.onUpdated(METRICS_CREATE,1);
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
        final long transactionId = txn.getId();
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
                    lmdbDataStoreProvider.onUpdating(metadata,key,update,transactionId);
                    lmdbDataStoreProvider.onCommit(metadata.scope(),transactionId);
                    updated = true;
                }
            }
        }catch (Exception ex){
            txn.abort();
            lmdbDataStoreProvider.onAbort(metadata.scope(),transactionId);
            return false;
        }
        finally {
            txn.close();
            if(updated) cache.reset();
        }
        if(updated){
            lmdbDataStoreProvider.metricsListener.onUpdated(METRICS_UPDATE,1);
            return true;
        }
        key.rewind();
        Recoverable.DataBuffer value = cache.value();
        if(!lmdbDataStoreProvider.onRecovering(metadata,key,value)){
            cache.reset();
            return false;
        }
        value.flip();
        Recoverable.DataHeader header = value.readHeader();
        if(header.revision() != t.revision()){
            cache.reset();
            return false;
        }
        value.clear();
        header.update(1);
        value.writeHeader(header);
        if(!t.write(value)){
            cache.reset();
            return false;
        }
        t.revision(header.revision());
        final Txn<ByteBuffer> xtxn = env.txnWrite();
        final long xtransctionId = xtxn.getId();
        try{
            if(!dbi.put(xtxn,key.rewind(),value.flip())) throw new RuntimeException("lmdb failure to insert key/value");
            xtxn.commit();
            key.rewind();
            value.rewind();
            lmdbDataStoreProvider.onUpdating(metadata,key,value,xtransctionId);
            lmdbDataStoreProvider.onCommit(metadata.scope(),xtransctionId);
            return true;
        }
        catch (Exception ex){
            xtxn.abort();
            lmdbDataStoreProvider.onAbort(metadata.scope(),xtransctionId);
            return false;
        }
        finally {
            xtxn.close();
            cache.reset();
            lmdbDataStoreProvider.metricsListener.onUpdated(METRICS_UPDATE,1);
        }
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
            return true;
        });
        key.rewind();
        boolean recovered = lmdbDataStoreProvider.onRecovering(metadata,key,value);
        if(!existed && !recovered){
            value.writeHeader(new LocalHeader(Long.MIN_VALUE,t.getFactoryId(),t.getClassId()));
            if(!t.write(value)){
                cache.reset();
                throw new RuntimeException("Error on write value");
            }
            Txn<ByteBuffer> txn = env.txnWrite();
            final long transactionId = txn.getId();
            try{
                if (!dbi.put(txn, key.rewind(),value.flip())) throw new RuntimeException("lmdb failure to insert key/value");
                if(t.onEdge()) onEdge(t.ownerKey(),t.label(),t.key(),txn);
                txn.commit();
                t.revision(Long.MIN_VALUE);
                key.rewind();
                value.rewind();
                lmdbDataStoreProvider.onUpdating(metadata,key,value,transactionId);
                lmdbDataStoreProvider.onCommit(metadata.scope(),transactionId);
                return true;
            }
            catch (Exception exception){
                throw new RuntimeException("Error on create",exception);
            }
            finally {
                txn.close();
                cache.reset();
            }
        }
        if(recovered){
            if(!set(key.rewind(),value.flip())){
                cache.reset();
                throw new RuntimeException("Error on set recovered data");
            }
            if(loading){
                value.rewind();
                Recoverable.DataHeader h = value.readHeader();
                t.read(value);
                t.revision(h.revision());
            }
        }
        cache.reset();
        return false;
    }

    @Override
    public <T extends Recoverable> boolean load(T t) {
        boolean loaded = get(t.key(),(k,v)->{
            Recoverable.DataHeader h = v.readHeader();
            t.read(v);
            t.revision(h.revision());
            return true;
        });
        if(loaded){
            lmdbDataStoreProvider.metricsListener.onUpdated(METRICS_LOAD,1);
        }
        Recoverable.DataBufferPair cache = lmdbDataStoreProvider.dataBufferPair();
        Recoverable.DataBuffer key = cache.key();
        if(!t.writeKey(key)) {
            cache.reset();
            return loaded;
        }
        Recoverable.DataBuffer value = cache.value();
        key.flip();
        if(!lmdbDataStoreProvider.onRecovering(metadata,key,value)) {
            cache.reset();
            return loaded;
        }
        value.flip();
        Recoverable.DataHeader header = value.readHeader();
        //System.out.println("RV : "+t.revision()+" : "+header.revision()+" : "+header.factoryId()+" : "+header.classId());
        if(loaded && t.revision() == header.revision()){
            cache.reset();
            return true;
        }
        t.read(value);
        t.revision(header.revision());
        lmdbDataStoreProvider.metricsListener.onUpdated(METRICS_LOAD,1);
        set(key.rewind(),value.rewind());
        cache.reset();
        return true;
    }

    public <T extends Recoverable> boolean createEdge(T t,String label){
        if(label==null) return false;
        lmdbDataStoreProvider.createEdgeDB(scope,name,label);
        Txn<ByteBuffer> txn = env.txnWrite();
        final long transactionId = txn.getId();
        try{
            if(!onEdge(t.ownerKey(),label,t.key(),txn)) return false;
            txn.commit();
            lmdbDataStoreProvider.onCommit(metadata.scope(),transactionId);
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

        final Txn<ByteBuffer> txn = env.txnWrite();
        final long transactionId = txn.getId();
        try{
            if(!dbi.delete(txn, key.flip())) return false;
            txn.commit();
            key.rewind();
            lmdbDataStoreProvider.onDeleting(metadata,key, cache.value(),transactionId);
            lmdbDataStoreProvider.onCommit(metadata.scope(),transactionId);
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
            key.flip();
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
            else{
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
        if(!key.write(cache.key())) {
            cache.reset();
            return;
        }
        try(final Txn<ByteBuffer> txn = env.txnRead()){
            try(Cursor<ByteBuffer> cursor = localEdgeDataStore.dbi.openCursor(txn)){
                if(cursor.get(cache.key().flip(),GetOp.MDB_SET)){
                    if(cursor.seek(SeekOp.MDB_FIRST_DUP)) bufferStream.on(cache.key(),BufferProxy.buffer(cursor.val()));
                    while (cursor.seek(SeekOp.MDB_NEXT_DUP)){
                        bufferStream.on(cache.key(),BufferProxy.buffer(cursor.val()));
                    }
                }
            }
        }
        finally {
            cache.reset();
        }
    }
    public void forEachEdgeKeyValue(Recoverable.Key key,String label,BufferStream bufferStream){
        LocalEdgeDataStore localEdgeDataStore = lmdbDataStoreProvider.createEdgeDB(metadata.scope(),name,label);
        Recoverable.DataBufferPair cache = lmdbDataStoreProvider.dataBufferPair();
        if(!key.write(cache.key())){
            cache.reset();
            return;
        }
        try(final Txn<ByteBuffer> txn = env.txnRead()){
            try(Cursor<ByteBuffer> cursor = localEdgeDataStore.dbi.openCursor(txn)){
                if(cursor.get(cache.key().flip(),GetOp.MDB_SET)){
                    if(cursor.seek(SeekOp.MDB_FIRST_DUP)){
                        if(dbi.get(txn,cursor.val()) !=null) bufferStream.on(BufferProxy.buffer(cursor.val().rewind()),BufferProxy.buffer(txn.val()));
                    }
                    while (cursor.seek(SeekOp.MDB_NEXT_DUP)){
                        if(dbi.get(txn,cursor.val()) ==null) continue;
                        bufferStream.on(BufferProxy.buffer(cursor.val().rewind()),BufferProxy.buffer(txn.val()));
                    }
                }
            }
        }
        finally {
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
            txn.commit();
            return true;
        }finally {
            txn.close();
            cache.reset();
        }
    }
    public void drop(boolean delete){
        Txn<ByteBuffer> txn = env.txnWrite();
        dbi.drop(txn,delete);
        txn.commit();
    }
    //help methods

    private <T extends Recoverable> boolean list(ByteBuffer key,LocalEdgeDataStore localEdgeDataStore,RecoverableFactory<T> query, Stream<T> stream){
        try(final Txn<ByteBuffer> txn = env.txnRead()){
            try(Cursor<ByteBuffer> cursor = localEdgeDataStore.dbi.openCursor(txn)){
                if(cursor.get(key,GetOp.MDB_SET)) {
                    boolean keepGoing = true;
                    if(cursor.seek(SeekOp.MDB_FIRST_DUP)) {
                        if (dbi.get(txn, cursor.val()) != null) {
                            T t = query.create();
                            Recoverable.DataBuffer proxy = BufferProxy.buffer(txn.val());
                            Recoverable.DataHeader local = proxy.readHeader();
                            t.read(proxy);
                            t.revision(local.revision());
                            t.readKey(BufferProxy.buffer(cursor.val().rewind()));
                            t.label(query.label());
                            keepGoing = stream.on(t);
                        }
                    }
                    while (keepGoing && cursor.seek(SeekOp.MDB_NEXT_DUP)){
                        if(dbi.get(txn,cursor.val())!=null){
                            T t = query.create();
                            Recoverable.DataBuffer proxy = BufferProxy.buffer(txn.val());
                            Recoverable.DataHeader local = proxy.readHeader();
                            t.read(proxy);
                            t.revision(local.revision());
                            t.readKey(BufferProxy.buffer(cursor.val().rewind()));
                            t.label(query.label());
                            keepGoing = stream.on(t);
                        }
                    }
                    return true;
                }
                return false;
            }
        }
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
            LocalEdgeDataStore localEdgeDataStore = lmdbDataStoreProvider.createEdgeDB(scope,name,label);
            if(!localEdgeDataStore.dbi.put(txn,key.flip(),value.flip(), PutFlags.MDB_NODUPDATA)) return false;//no duplicate entry
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
