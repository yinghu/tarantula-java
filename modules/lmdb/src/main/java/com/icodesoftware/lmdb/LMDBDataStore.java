package com.icodesoftware.lmdb;

import com.icodesoftware.*;
import com.icodesoftware.service.DataStoreSummary;
import com.icodesoftware.service.Metadata;
import com.icodesoftware.util.BufferProxy;
import com.icodesoftware.util.BufferUtil;
import com.icodesoftware.util.LocalHeader;
import org.lmdbjava.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class LMDBDataStore implements DataStore,DataStore.Backup ,Closable {

    public final LMDBEnv env;
    private final Dbi<ByteBuffer> dbi;

    private final String name;

    private final Metadata metadata;

    private int scope;

    //NOTES : key+value < 2032 bytes ( 511 bytes for key ; value <= 1521 bytes (2032 - 511 - 8)

    private final LocalLMDBProvider lmdbDataStoreProvider;
    private final Txn<ByteBuffer> ptxn;
    private final long transactionId;

    public LMDBDataStore(String name, Dbi<ByteBuffer> dbi,Txn<ByteBuffer> ptxn,long transactionId,LMDBEnv env){
        this.metadata = new LocalMetadata(env.envSetting.scope,name);
        this.scope = env.envSetting.scope;
        this.name = name;
        this.dbi = dbi;
        this.env = env;
        this.lmdbDataStoreProvider = env.lmdbDataStoreProvider;
        this.ptxn = ptxn;
        this.transactionId = transactionId;
    }

    @Override
    public int scope() {
        return scope;
    }

    @Override
    public String name() {
        return name;
    }




    @Override
    public void view(DataStoreSummary dataStoreSummary){
        try(final Txn<ByteBuffer> txn = env.txn(ptxn)){
            Stat st = dbi.stat(txn);
            dataStoreSummary.count(st.entries);
            dataStoreSummary.depth(st.depth);
            dataStoreSummary.leafPages(st.leafPages);
            dataStoreSummary.overflowPages(st.overflowPages);
            dataStoreSummary.branchPages(st.branchPages);
            dataStoreSummary.pageSize(st.pageSize);
            dataStoreSummary.edgeList(new ArrayList<>());
        }
    }

    @Override
    public <T extends Recoverable> boolean create(T t) {
        try( final Recoverable.DataBufferPair cache = lmdbDataStoreProvider.dataBufferPair()){
            Recoverable.DataBuffer key = cache.key();
            Recoverable.DataBuffer value = cache.value();
            lmdbDataStoreProvider.assign(key);
            key.flip();
            if(!t.readKey(key)){
                return false;
            }
            value.writeHeader(new LocalHeader(Long.MIN_VALUE,t.getFactoryId(),t.getClassId()));
            if(!t.write(value)){
                return false;
            }
            try(final Txn<ByteBuffer> txn = env.txn(ptxn)){
                if(!dbi.put(txn,key.rewind(),value.flip())) throw new RuntimeException("lmdb failure to insert key/value");
                txn.commit();
                if(t.onEdge()) onEdge(t.ownerKey(),t.label(),t.key());
                key.rewind();
                value.rewind();
                t.revision(Long.MIN_VALUE);
                lmdbDataStoreProvider.onUpdating(metadata,key,value,transactionId);
                return true;
            }
        }
        finally {
            lmdbDataStoreProvider.onUpdated(METRICS_CREATE,1);
        }
    }

    @Override
    public <T extends Recoverable> boolean update(T t) {
        try(Recoverable.DataBufferPair cache = lmdbDataStoreProvider.dataBufferPair()){
            Recoverable.DataBuffer key = cache.key();
            if(!t.writeKey(key)){
                return false;
            }
            try(final Txn<ByteBuffer> txn = env.txn(ptxn)){
                if (dbi.get(txn, key.flip()) != null){
                    Recoverable.DataBuffer proxy = com.icodesoftware.util.BufferProxy.buffer(txn.val());
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
                        return true;
                    }
                }
            }
            key.rewind();
            Recoverable.DataBuffer value = cache.value();
            if(!lmdbDataStoreProvider.onRecovering(metadata,key,value)){
                return false;
            }
            try(final Txn<ByteBuffer> txn = env.txn(ptxn);){
                value.flip();
                Recoverable.DataHeader header = value.readHeader();
                if(header.revision() != t.revision()) return false;
                value.clear();
                header.update(1);
                value.writeHeader(header);
                t.revision(header.revision());
                if(!t.write(value)) return false;
                if(!dbi.put(txn,key.rewind(),value.flip())) throw new RuntimeException("lmdb failure to insert key/value");
                txn.commit();
                key.rewind();
                value.rewind();
                lmdbDataStoreProvider.onUpdating(metadata,key,value,transactionId);
            }
            return true;
        }finally {
            lmdbDataStoreProvider.onUpdated(METRICS_UPDATE,1);
        }
    }

    @Override
    public <T extends Recoverable> boolean createIfAbsent(T t, boolean loading) {
        try(final Recoverable.DataBufferPair cache = lmdbDataStoreProvider.dataBufferPair()){
            Recoverable.DataBuffer key = cache.key();
            Recoverable.DataBuffer value = cache.value();
            if(!t.writeKey(key)) {
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
                    throw new RuntimeException("Error on write value");
                }
                try(final Txn<ByteBuffer> txn = env.txn(ptxn)){
                    if (!dbi.put(txn, key.rewind(),value.flip())) throw new RuntimeException("lmdb failure to insert key/value");
                    txn.commit();
                    if(t.onEdge()) onEdge(t.ownerKey(),t.label(),t.key());
                    t.revision(Long.MIN_VALUE);
                    key.rewind();
                    value.rewind();
                    lmdbDataStoreProvider.onUpdating(metadata,key,value,transactionId);
                    return true;
                }
            }
            if(recovered){
                if(!set(key.rewind(),value.flip())){
                    throw new RuntimeException("Error on set recovered data");
                }
                if(loading){
                    value.rewind();
                    Recoverable.DataHeader h = value.readHeader();
                    t.read(value);
                    t.revision(h.revision());
                }
            }
            return false;
        }finally {
            lmdbDataStoreProvider.onUpdated(METRICS_CREATE_IF_ABSENT,1);
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
        try(final Recoverable.DataBufferPair cache = lmdbDataStoreProvider.dataBufferPair()){
            Recoverable.DataBuffer key = cache.key();
            if(!t.writeKey(key)) {
                return loaded;
            }
            Recoverable.DataBuffer value = cache.value();
            key.flip();
            if(!lmdbDataStoreProvider.onRecovering(metadata,key,value)) {
                return loaded;
            }
            value.flip();
            Recoverable.DataHeader header = value.readHeader();
            //System.out.println("RV : "+t.revision()+" : "+header.revision()+" : "+header.factoryId()+" : "+header.classId());
            if(loaded && t.revision() == header.revision()){
                return true;
            }
            t.read(value);
            t.revision(header.revision());
            set(key.rewind(),value.rewind());
            return true;
        }
        finally {
            lmdbDataStoreProvider.onUpdated(METRICS_LOAD,1);
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
        try(Recoverable.DataBufferPair cache = lmdbDataStoreProvider.dataBufferPair()){
            Recoverable.DataBuffer key = cache.key();
            if(!t.writeKey(key)){
                return false;
            }
            try(final Txn<ByteBuffer> txn = env.txn(ptxn)){
                if(!dbi.delete(txn, key.flip())) return false;
                txn.commit();
                key.rewind();
                lmdbDataStoreProvider.onDeleting(metadata,key, cache.value(),transactionId);
                return true;
            }
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
        try(final Recoverable.DataBufferPair cache = lmdbDataStoreProvider.dataBufferPair()){
            Recoverable.DataBuffer key = cache.key();
            if(!query.key().write(key)) return;
            LocalEdgeDataStore localEdgeDataStore = lmdbDataStoreProvider.localEdgeDataStore(scope,name,query.label(),ptxn);
            key.flip();
            if(lmdbDataStoreProvider.onRecovering(localEdgeDataStore.metadata(),key,(e,v)->{
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
            else{
                list(key.rewind(),localEdgeDataStore,query,stream);
            }
        }
    }
    @Override
    public Backup backup() {
        return this;
    }

    @Override
    public void close() {
        //dbi.close();
    }

    //BACKUP METHODS
    public boolean get(Recoverable.Key key, BufferStream buffer) {
        try(final Recoverable.DataBufferPair cache = lmdbDataStoreProvider.dataBufferPair()){
            Recoverable.DataBuffer akey = cache.key();
            if(!key.write(akey)) return false;
            return get(akey.flip(),buffer);
        }
    }

    @Override
    public boolean set(BufferStream bufferStream) {
        try(final Recoverable.DataBufferPair cache = lmdbDataStoreProvider.dataBufferPair()){
            Recoverable.DataBuffer key = cache.key();
            Recoverable.DataBuffer value = cache.value();
            if(!bufferStream.on(key,value)) return false;
            try(final Txn<ByteBuffer> txn = env.txn(ptxn)) {
                if(dbi.get(txn,key.flip())==null){
                    if(!dbi.put(txn,key.rewind(),value.flip())) return false;
                    txn.commit();
                    return true;
                }
                Recoverable.DataBuffer existed = com.icodesoftware.util.BufferProxy.buffer(txn.val());
                Recoverable.DataHeader existingHeader = existed.readHeader();
                value.flip();
                Recoverable.DataHeader header = value.readHeader();
                if(header.revision() < existingHeader.revision()) return false;

                if(!dbi.put(txn,key.rewind(),value.rewind())) return false;
                txn.commit();
                return true;
            }
        }
    }

    public void forEachEdgeKey(Recoverable.Key key,String label,BufferStream bufferStream){
        LocalEdgeDataStore localEdgeDataStore = lmdbDataStoreProvider.localEdgeDataStore(metadata.scope(),name,label,ptxn);
        try(final Recoverable.DataBufferPair cache = lmdbDataStoreProvider.dataBufferPair()){
            if(!key.write(cache.key())) {
                return;
            }
            try(final Cursor<ByteBuffer> cursor = localEdgeDataStore.openCursor(ptxn)){

                if(cursor.get(cache.key().flip(),GetOp.MDB_SET)){
                    if(cursor.seek(SeekOp.MDB_FIRST_DUP)) bufferStream.on(cache.key(), com.icodesoftware.util.BufferProxy.buffer(cursor.val()));
                    while (cursor.seek(SeekOp.MDB_NEXT_DUP)){
                        bufferStream.on(cache.key(), com.icodesoftware.util.BufferProxy.buffer(cursor.val()));
                    }
                }

            }
        }

    }

    public void forEachEdgeKeyValue(Recoverable.Key key,String label,BufferStream bufferStream){
        LocalEdgeDataStore localEdgeDataStore = lmdbDataStoreProvider.localEdgeDataStore(metadata.scope(),name,label,ptxn);
        ArrayList<Recoverable.DataBuffer> edgeKeys = new ArrayList<>();
        try(final Recoverable.DataBufferPair cache = lmdbDataStoreProvider.dataBufferPair()){
            if(!key.write(cache.key())){
                return;
            }
            try(final Cursor<ByteBuffer> cursor = localEdgeDataStore.openCursor(ptxn)){

                if(cursor.get(cache.key().flip(),GetOp.MDB_SET)){
                    if(cursor.seek(SeekOp.MDB_FIRST_DUP)){
                        Recoverable.DataBuffer data = com.icodesoftware.util.BufferProxy.buffer(LMDBDataStoreProvider.KEY_SIZE,true);
                        BufferUtil.copy(cursor.val(),data);
                        edgeKeys.add(data);
                    }
                    while (cursor.seek(SeekOp.MDB_NEXT_DUP)){
                        Recoverable.DataBuffer data = com.icodesoftware.util.BufferProxy.buffer(LMDBDataStoreProvider.KEY_SIZE,true);
                        BufferUtil.copy(cursor.val(),data);
                        edgeKeys.add(data);
                    }
                }

            }
        }
        edgeKeys.forEach((edgeKey)->{
            try(final Txn<ByteBuffer> txn = env.txn(ptxn)){
                if(dbi.get(txn,edgeKey.flip()) !=null) {
                    edgeKey.rewind();
                    bufferStream.on(edgeKey, com.icodesoftware.util.BufferProxy.buffer(txn.val()));
                }
            }
        });
    }

    @Override
    public void forEach(BufferStream stream) {
        try(final Cursor<ByteBuffer> cursor = dbi.openCursor(ptxn)){
            while (cursor.next()){
                Recoverable.DataBuffer dataBuffer = com.icodesoftware.util.BufferProxy.buffer(cursor.val());
                if(!stream.on(com.icodesoftware.util.BufferProxy.buffer(cursor.key()),dataBuffer)) break;
            }
        }
    }

    public boolean setEdge(String label,BufferStream bufferStream){
        if(label==null) return false;
        LocalEdgeDataStore localEdgeDataStore = lmdbDataStoreProvider.localEdgeDataStore(scope,name,label,ptxn);
        try(final Recoverable.DataBufferPair cache = lmdbDataStoreProvider.dataBufferPair();final Txn<ByteBuffer> txn = env.txn(ptxn)){
            if(!bufferStream.on(cache.key(),cache.value())) return false;
            if(!localEdgeDataStore.addEdge(txn,cache.key().flip(),cache.value().flip())) return false;
            txn.commit();
            return true;
        }
    }

    public boolean unsetEdge(String label,BufferStream bufferStream,boolean fromLabel){
        if(label==null) return false;
        LocalEdgeDataStore localEdgeDataStore = lmdbDataStoreProvider.localEdgeDataStore(scope,name,label,ptxn);
        try(final Recoverable.DataBufferPair cache = lmdbDataStoreProvider.dataBufferPair();final Txn<ByteBuffer> txn = env.txn(ptxn)){
            Recoverable.DataBuffer key = cache.key();
            Recoverable.DataBuffer value = cache.value();
            if(!bufferStream.on(key,value)) return false;
            if(fromLabel){
                if(!localEdgeDataStore.deleteEdge(txn,key.flip())) return false;
            }
            else {
                if(!localEdgeDataStore.deleteEdge(txn,key.flip(),value.flip())) return false;
            }
            txn.commit();
            return true;
        }
    }
    public boolean unset(BufferStream bufferStream){
        try(final Recoverable.DataBufferPair cache = lmdbDataStoreProvider.dataBufferPair();final Txn<ByteBuffer> txn = env.txn(ptxn)){
            Recoverable.DataBuffer key = cache.key();
            Recoverable.DataBuffer value = cache.value();
            if(!bufferStream.on(key,value)) return false;
            if(!dbi.delete(txn,key.flip())) return false;
            txn.commit();
            return true;
        }
    }

    public void drop(boolean delete){
        //using cached lmdb
    }

    //help methods
    private <T extends Recoverable> boolean list(ByteBuffer key,LocalEdgeDataStore localEdgeDataStore,RecoverableFactory<T> query, Stream<T> stream){
        ArrayList<Recoverable.DataBuffer> edgeKeys = new ArrayList<>();
        try(final Cursor<ByteBuffer> cursor = localEdgeDataStore.openCursor(ptxn)){
            if(cursor.get(key,GetOp.MDB_SET)) {
                boolean keepGoing = true;
                if(cursor.seek(SeekOp.MDB_FIRST_DUP)){
                    Recoverable.DataBuffer data = com.icodesoftware.util.BufferProxy.buffer(LMDBDataStoreProvider.KEY_SIZE,true);
                    BufferUtil.copy(cursor.val(),data);
                    edgeKeys.add(data);
                }
                while (keepGoing && cursor.seek(SeekOp.MDB_NEXT_DUP)){
                    Recoverable.DataBuffer data = com.icodesoftware.util.BufferProxy.buffer(LMDBDataStoreProvider.KEY_SIZE,true);
                    BufferUtil.copy(cursor.val(),data);
                    edgeKeys.add(data);
                }
            }
        }
        if(edgeKeys.isEmpty()) return false;
        for(Recoverable.DataBuffer edgeKey : edgeKeys){
            try(final Txn<ByteBuffer> txn = env.txn(ptxn)){
                if(dbi.get(txn,edgeKey.flip()) !=null) {
                    edgeKey.rewind();
                    T t = query.create();
                    Recoverable.DataBuffer proxy = com.icodesoftware.util.BufferProxy.buffer(txn.val());
                    Recoverable.DataHeader local = proxy.readHeader();
                    t.read(proxy);
                    t.revision(local.revision());
                    t.readKey(edgeKey);
                    t.label(query.label());
                    if(!stream.on(t)) break;
                }
            }
        }
        return true;

    }
    private boolean set(ByteBuffer key, ByteBuffer value){
        try(final Txn<ByteBuffer> txn = env.txn(ptxn)){
            if(!dbi.put(txn,key,value)) return false;
            txn.commit();
            return true;
        }
    }

    private boolean onEdge(Recoverable.Key ownerKey, String label, Recoverable.Key edgeKey){
        if(ownerKey ==null || label==null) return false;
        try(final Recoverable.DataBufferPair cache = lmdbDataStoreProvider.dataBufferPair()){
            Recoverable.DataBuffer key = cache.key();
            Recoverable.DataBuffer value = cache.value();
            LocalEdgeDataStore localEdgeDataStore = lmdbDataStoreProvider.localEdgeDataStore(scope,name,label,ptxn);
            try(final Txn<ByteBuffer> txn = env.txn(ptxn)){
                if(!ownerKey.write(key)) return false;
                if(!edgeKey.write(value)) return false;
                if(!localEdgeDataStore.addEdge(txn,key.flip(),value.flip())) return false;
                txn.commit();
                key.rewind();
                value.rewind();
                lmdbDataStoreProvider.onUpdating(localEdgeDataStore.metadata(),key,value,transactionId);
                return true;
            }
        }
    }
    private boolean offEdge(Recoverable.Key t,String label,Recoverable.Key edge){
        if(t==null || edge==null || label ==null) return false;
        try(final Recoverable.DataBufferPair cache = lmdbDataStoreProvider.dataBufferPair()){
            Recoverable.DataBuffer key = cache.key();
            Recoverable.DataBuffer value = cache.value();
            LocalEdgeDataStore localEdgeDataStore = lmdbDataStoreProvider.localEdgeDataStore(scope,name,label,ptxn);
            try(final Txn<ByteBuffer> txn = env.txn(ptxn);){
                if(!t.write(key)) return false;
                if(!edge.write(value)) return false;
                if(!localEdgeDataStore.deleteEdge(txn,key.flip(),value.flip())) return false;
                txn.commit();
                key.rewind();
                value.rewind();
                lmdbDataStoreProvider.onDeleting(localEdgeDataStore.metadata(),key,value,transactionId);
                return true;
            }
        }
    }
    private  boolean offEdge(Recoverable.Key t,String label){
        if(t==null || label ==null) return false;
        try(final Recoverable.DataBufferPair cache = lmdbDataStoreProvider.dataBufferPair()){
            Recoverable.DataBuffer key = cache.key();
            if(!t.write(key)) return false;
            LocalEdgeDataStore localEdgeDataStore = lmdbDataStoreProvider.localEdgeDataStore(scope,name,label,ptxn);
            try(final Txn<ByteBuffer> txn = env.txn(ptxn)){
                if(!localEdgeDataStore.deleteEdge(txn,key.flip())) return false;
                txn.commit();
                key.rewind();
                this.lmdbDataStoreProvider.onDeleting(localEdgeDataStore.metadata(),key,null,transactionId);
                return true;
            }
        }
    }

    private boolean get(ByteBuffer key, BufferStream buffer) {
         //read only
        try(final Txn<ByteBuffer> txn = env.txn(ptxn)){
            if (dbi.get(txn,key) == null) return false;
            Recoverable.DataBuffer data = com.icodesoftware.util.BufferProxy.buffer(txn.val());
            return buffer.on(BufferProxy.buffer(key.rewind()),data);
        }
    }

}
