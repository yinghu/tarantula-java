package com.icodesoftware.lmdb.ffm;

import com.icodesoftware.DataStore;
import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.lmdb.EnvSetting;
import com.icodesoftware.service.DataStoreSummary;
import com.icodesoftware.service.Metadata;
import com.icodesoftware.util.BufferProxy;
import com.icodesoftware.util.DataBufferKey;
import com.icodesoftware.util.LocalHeader;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.ArrayList;
import java.util.List;


public class NativeDataStore implements DataStore, DataStore.Backup {

    private NativeDataStoreProvider nativeDataStoreProvider;
    private NativeEnv env;
    private String name;
    private NativeTxn parentTxn;

    public NativeDataStore(String name,NativeDataStoreProvider nativeDataStoreProvider,NativeEnv nativeEnv,NativeTxn parent){
        this.name = name;
        this.nativeDataStoreProvider = nativeDataStoreProvider;
        this.env = nativeEnv;
        this.parentTxn = parent;
    }

    public NativeDataStore(String name,NativeDataStoreProvider nativeDataStoreProvider,NativeEnv nativeEnv){
        this.name = name;
        this.nativeDataStoreProvider = nativeDataStoreProvider;
        this.env = nativeEnv;
    }

    public int scope(){
        return env.scope();
    }

    public String name(){
        return name;
    }

    public <T extends Recoverable> boolean create(T t) {
        NativeDbi dbi = env.createDbi(name,parentTxn);
        NativeDbi edge = edgeDbi(t);
        try(Arena arena = Arena.ofConfined(); NativeTxn txn = env.write(arena,parentTxn==null? MemorySegment.NULL:parentTxn.pointer())){
            long txnId = txn.transactionId();
            NativeData.InVal keyIn = NativeData.in(arena, EnvSetting.KEY_SIZE);
            Recoverable.DataBuffer key = keyIn.write(buffer -> nativeDataStoreProvider.assign(buffer));
            t.readKey(key);
            NativeData.InVal valueIn = NativeData.in(arena,EnvSetting.VALUE_SIZE);
            Recoverable.DataBuffer value = valueIn.write(buffer -> {
                buffer.writeHeader(LocalHeader.create(t.getFactoryId(),t.getClassId(),1L));
                if(!t.write(buffer)) throw new RuntimeException("no data");
            });
            if(!dbi.put(keyIn.pointer(),valueIn.pointer(),txn)){
                txn.abort();
                return false;
            }
            if(edge!=null){
                NativeData.InVal ownerKey = NativeData.in(arena,EnvSetting.KEY_SIZE);
                Recoverable.DataBuffer bkey = ownerKey.write(buffer -> t.ownerKey().write(buffer));
                NativeData.InVal edgeKey = NativeData.in(arena,EnvSetting.KEY_SIZE);
                Recoverable.DataBuffer bEdge = edgeKey.write(buffer -> t.key().write(buffer));
                if(!edge.put(ownerKey.pointer(),edgeKey.pointer(),txn)){
                    txn.abort();
                    return false;
                }
                nativeDataStoreProvider.onUpdating(edge.metadata(),BufferProxy.copy(bkey.src()),BufferProxy.copy(bEdge.src()),txnId);
            }
            key.rewind();
            nativeDataStoreProvider.onUpdating(dbi.metadata(), BufferProxy.copy(key.src()),BufferProxy.copy(value.src()), txnId);
            if(parentTxn==null) nativeDataStoreProvider.onCommit(dbi.metadata().scope(),txnId);
            txn.commit();
            return true;
        }
        catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public <T extends Recoverable> boolean createIfAbsent(T t, boolean loading) {
        NativeDbi dbi = env.createDbi(name,parentTxn);
        NativeDbi edge = edgeDbi(t);
        recover(dbi.metadata(),t);
        try(Arena arena = Arena.ofConfined(); NativeTxn txn = env.write(arena,parentTxn==null?MemorySegment.NULL:parentTxn.pointer())){
            long txnId = txn.transactionId();
            NativeData.InVal key = NativeData.in(arena,EnvSetting.KEY_SIZE);
            Recoverable.DataBuffer kBuffer = key.write(buffer ->{
                if(!t.writeKey(buffer)){
                    txn.abort();
                    throw new RuntimeException("Key must be assigned");
                }
            });
            NativeData.OutVal outVal = NativeData.out(arena);
            if(dbi.get(key.pointer(),outVal.pointer(),txn)){
                if(!loading){
                    txn.abort();
                    return false;
                }
                outVal.read(arena,buffer -> {
                    Recoverable.DataHeader h = buffer.readHeader();
                    t.read(buffer);
                    t.revision(h.revision());
                });
                txn.abort();
                return false;
            }
            NativeData.InVal value = NativeData.in(arena,EnvSetting.VALUE_SIZE);
            Recoverable.DataBuffer vBuffer = value.write(buffer ->{
                buffer.writeHeader(LocalHeader.create(t.getFactoryId(),t.getClassId(),1l));
                t.write(buffer);
            });
            if(!dbi.put(key.pointer(),value.pointer(),txn)){
                txn.abort();
                throw new RuntimeException("Cannot put data");
            }
            if(edge!=null){
                NativeData.InVal ownerKey = NativeData.in(arena,EnvSetting.KEY_SIZE);
                Recoverable.DataBuffer bkey = ownerKey.write(buffer -> t.ownerKey().write(buffer));
                NativeData.InVal edgeKey = NativeData.in(arena,EnvSetting.KEY_SIZE);
                Recoverable.DataBuffer bEdge = edgeKey.write(buffer -> t.key().write(buffer));
                if(!edge.put(ownerKey.pointer(),edgeKey.pointer(),txn)){
                    txn.abort();
                    return false;
                }
                nativeDataStoreProvider.onUpdating(edge.metadata(),BufferProxy.copy(bkey.src()),BufferProxy.copy(bEdge.src()),txnId);
            }
            t.revision(1L);
            nativeDataStoreProvider.onUpdating(dbi.metadata(), BufferProxy.copy(kBuffer.src()),BufferProxy.copy(vBuffer.src()), txnId);
            if(parentTxn==null) nativeDataStoreProvider.onCommit(dbi.metadata().scope(),txnId);
            txn.commit();
            return true;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public <T extends Recoverable> boolean load(T t) {
        NativeDbi dbi = env.createDbi(name,parentTxn);
        recover(dbi.metadata(),t);
        try(Arena arena = Arena.ofConfined();NativeTxn txn = env.read(arena)){
            NativeData.InVal keyIn = NativeData.in(arena,EnvSetting.KEY_SIZE);
            keyIn.write(buffer -> t.writeKey(buffer));
            NativeData.OutVal value = NativeData.out(arena);
            if(!dbi.get(keyIn.pointer(),value.pointer(),txn)){
                txn.abort();
                return false;
            }
            value.read(arena,buffer -> {
                Recoverable.DataHeader header = buffer.readHeader();
                t.read(buffer);
                t.revision(header.revision());
            });
            txn.abort();
            return true;
        }
        catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public <T extends Recoverable> boolean update(T t) {
        NativeDbi dbi = env.createDbi(name,parentTxn);
        recover(dbi.metadata(),t);
        try(Arena arena = Arena.ofConfined(); NativeTxn txn = env.write(arena,parentTxn==null?MemorySegment.NULL:parentTxn.pointer())){
            long txnId = txn.transactionId();
            NativeData.InVal key = NativeData.in(arena,EnvSetting.KEY_SIZE);
            Recoverable.DataBuffer kBuffer = key.write(buffer -> t.writeKey(buffer));
            NativeData.OutVal value = NativeData.out(arena);
            if(!dbi.get(key.pointer(),value.pointer(),txn)){
                txn.abort();
                return false;
            }
            boolean[] pending ={false};
            value.read(arena,buffer -> {
                Recoverable.DataHeader h = buffer.readHeader();
                pending[0] = h.revision() == t.revision();
            });
            if(!pending[0]){
                txn.abort();
                return false;
            }
            NativeData.InVal update = NativeData.in(arena,EnvSetting.VALUE_SIZE);
            Recoverable.DataBuffer vBuffer = update.write(buffer -> {
                buffer.writeHeader(LocalHeader.create(t.getFactoryId(),t.getClassId(),t.revision()+1));
                t.write(buffer);
            });
            if(!dbi.put(key.pointer(),update.pointer(),txn)){
                txn.abort();
                return false;
            }
            t.revision(t.revision()+1);
            nativeDataStoreProvider.onUpdating(dbi.metadata(), BufferProxy.copy(kBuffer.src()),BufferProxy.copy(vBuffer.src()), txnId);
            if(parentTxn==null) nativeDataStoreProvider.onCommit(dbi.metadata().scope(),txnId);
            txn.commit();
            return true;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public <T extends Recoverable> boolean delete(T t){
        NativeDbi dbi = env.createDbi(name,parentTxn);
        try(Arena arena = Arena.ofConfined(); NativeTxn txn = env.write(arena,parentTxn==null?MemorySegment.NULL:parentTxn.pointer())){
            long txnId = txn.transactionId();
            NativeData.InVal key = NativeData.in(arena,EnvSetting.KEY_SIZE);
            Recoverable.DataBuffer kBuffer = key.write(buffer -> t.writeKey(buffer));
            NativeData.OutVal value = NativeData.out(arena);
            if(!dbi.get(key.pointer(),value.pointer(),txn)){
                txn.abort();
                return false;
            }
            if(!dbi.delete(key.pointer(),txn)){
                txn.abort();
                return false;
            }
            nativeDataStoreProvider.onDeleting(dbi.metadata(), BufferProxy.copy(kBuffer.src()),BufferProxy.copy(kBuffer.src()),txnId);
            if(parentTxn==null) nativeDataStoreProvider.onCommit(dbi.metadata().scope(),txnId);
            txn.commit();
            return true;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public <T extends Recoverable> boolean createEdge(T t,String label){
        t.label(label);
        NativeDbi edge = edgeDbi(t);
        if(edge==null) return false;
        try(Arena arena = Arena.ofConfined(); NativeTxn txn = env.write(arena,parentTxn==null?MemorySegment.NULL:parentTxn.pointer())){
            long txnId = txn.transactionId();
            NativeData.InVal key = NativeData.in(arena,EnvSetting.KEY_SIZE);
            Recoverable.DataBuffer kBuffer = key.write(buffer -> t.ownerKey().write(buffer));
            NativeData.InVal value = NativeData.in(arena,EnvSetting.KEY_SIZE);
            Recoverable.DataBuffer vBuffer = value.write(buffer -> t.key().write(buffer));
            if(!edge.put(key.pointer(),value.pointer(),txn)){
                txn.abort();
                return false;
            }
            nativeDataStoreProvider.onUpdating(edge.metadata(), BufferProxy.copy(kBuffer.src()),BufferProxy.copy(vBuffer.src()), txnId);
            if(parentTxn==null) nativeDataStoreProvider.onCommit(edge.metadata().scope(),txnId);
            txn.commit();
            return true;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public  boolean deleteEdge(Recoverable.Key t,String label){
        NativeDbi edge = env.createDbi(name,label,parentTxn);
        try(Arena arena = Arena.ofConfined(); NativeTxn txn = env.write(arena,parentTxn==null?MemorySegment.NULL:parentTxn.pointer())){
            long txnId = txn.transactionId();
            NativeData.InVal key = NativeData.in(arena,EnvSetting.KEY_SIZE);
            Recoverable.DataBuffer kBuffer = key.write(buffer -> t.write(buffer));
            if(!edge.delete(key.pointer(),txn)){
                txn.abort();
                return false;
            }
            nativeDataStoreProvider.onDeleting(edge.metadata(),kBuffer,kBuffer,txnId);
            if(parentTxn==null) nativeDataStoreProvider.onCommit(edge.metadata().scope(),txnId);
            txn.commit();
            return true;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public boolean deleteEdge(Recoverable.Key t,Recoverable.Key edgeKey,String label){
        NativeDbi edge = env.createDbi(name,label,parentTxn);
        try(Arena arena = Arena.ofConfined(); NativeTxn txn = env.write(arena,parentTxn==null?MemorySegment.NULL:parentTxn.pointer())){
            long txnId = txn.transactionId();
            NativeData.InVal key = NativeData.in(arena,EnvSetting.KEY_SIZE);
            Recoverable.DataBuffer kBuffer = key.write(buffer -> t.write(buffer));
            NativeData.InVal value = NativeData.in(arena,EnvSetting.KEY_SIZE);
            Recoverable.DataBuffer vBuffer = value.write(buffer -> edgeKey.write(buffer));
            if(!edge.delete(key.pointer(),value.pointer(),txn)){
                txn.abort();
                return false;
            }
            nativeDataStoreProvider.onDeleting(edge.metadata(),kBuffer,vBuffer,txnId);
            if(parentTxn==null) nativeDataStoreProvider.onCommit(edge.metadata().scope(),txnId);
            txn.commit();
            return true;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
    public <T extends Recoverable> List<T> list(RecoverableFactory<T> query) {
        ArrayList<T> list = new ArrayList<>();
        this.list(query,t-> list.add(t));
        return list;
    }
    public <T extends Recoverable> void list(RecoverableFactory<T> query, DataStore.Stream<T> stream) {
        NativeDbi dbi = env.createDbi(name,parentTxn);
        NativeDbi edge = env.createDbi(name,query.label(),parentTxn);
        recover(edge.metadata(),query);
        try(NativeCursor cursor = edge.cursor()){
            cursor.read().forEach(query.key(),(k,v)->{
                NativeData.OutVal out = NativeData.out(cursor.arena());
                NativeData.InVal pv = NativeData.in(cursor.arena(),EnvSetting.KEY_SIZE);
                Recoverable.DataBuffer pk = pv.write(buffer -> v.read(buffer));
                boolean[] streaming ={true};
                if(dbi.get(pv.pointer(),out.pointer(),cursor.txn())){
                    out.read(cursor.arena(),buffer -> {
                        Recoverable.DataHeader h = buffer.readHeader();
                        T t = query.create();
                        t.read(buffer);
                        t.revision(h.revision());
                        t.readKey(pk);
                        streaming[0] = stream.on(t);
                    });
                }
                return streaming[0];
            });
        }
    }

    @Override
    public Backup backup() {
        return this;
    }

    //backup
    public void forEach(DataStore.BufferStream stream) {
        NativeDbi dbi = env.createDbi(name,parentTxn);
        try(NativeCursor cursor = dbi.cursor()){
            cursor.read().forEach(stream);
        }
    }

    public void drop(boolean delete){
        NativeDbi dbi = env.createDbi(name,parentTxn);
        dbi.drop(delete);
    }

    public boolean get(Recoverable.Key key, BufferStream stream){
        NativeDbi dbi = env.createDbi(name,parentTxn);
        try(Arena arena = Arena.ofConfined();NativeTxn txn = env.read(arena)){
            NativeData.InVal k = NativeData.in(arena,EnvSetting.KEY_SIZE);
            Recoverable.DataBuffer kBuffer = k.write(buffer -> key.write(buffer));
            NativeData.OutVal v = NativeData.out(arena);
            if(!dbi.get(k.pointer(),v.pointer(),txn)){
                txn.abort();
                return false;
            }
            v.read(arena,buffer -> {
                stream.on(kBuffer,buffer);
            });
            txn.abort();
        }
        return true;
    }
    public boolean set(BufferStream bufferStream){
        NativeDbi dbi = env.createDbi(name,parentTxn);
        try(Arena arena = Arena.ofConfined();NativeTxn txn = env.write(arena,parentTxn==null?MemorySegment.NULL:parentTxn.pointer())) {
            NativeData.InPair keyValue = NativeData.inPair(arena,EnvSetting.KEY_SIZE,EnvSetting.VALUE_SIZE);
            boolean[] pending = {false};
            keyValue.write((k,v)->{
               pending[0] = bufferStream.on(k,v);
            });
            if(!pending[0]){
                txn.abort();
                return false;
            }
            if(!dbi.put(keyValue.keyPointer(),keyValue.valuePointer(),txn)){
                txn.abort();
                return false;
            }
            txn.commit();
            return true;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public void forEachEdgeKey(Recoverable.Key key,String label,BufferStream bufferStream){
        NativeDbi edge = env.createDbi(name,label,parentTxn);
        try(NativeCursor cursor = edge.cursor()){
            cursor.read().forEach(key,bufferStream);
        }
    }

    public void forEachEdgeKeyValue(Recoverable.Key key,String label,BufferStream bufferStream){
        NativeDbi edge = env.createDbi(name,label,parentTxn);
        NativeDbi dbi = env.createDbi(name,parentTxn);
        try(NativeCursor cursor = edge.cursor()){
            cursor.read().forEach(key,(k,v)->{
                NativeData.OutVal out = NativeData.out(cursor.arena());
                NativeData.InVal pv = NativeData.in(cursor.arena(),EnvSetting.KEY_SIZE);
                pv.write(buffer -> v.read(buffer));
                boolean[] streaming ={true};
                if(dbi.get(pv.pointer(),out.pointer(),cursor.txn())){
                    out.read(cursor.arena(),buffer ->{
                        v.rewind();
                        streaming[0] = bufferStream.on(v,buffer);
                    });
                }
                return streaming[0];
            });
        }
    }
    public boolean setEdge(String label,BufferStream bufferStream){
        NativeDbi edge = env.createDbi(name,label,parentTxn);
        try(Arena arena = Arena.ofConfined(); NativeTxn txn = env.write(arena,parentTxn==null?MemorySegment.NULL:parentTxn.pointer())){
            boolean[] pending={false};
            NativeData.InPair inPair = NativeData.inPair(arena,EnvSetting.KEY_SIZE,EnvSetting.KEY_SIZE).write((b1,b2)->{
                pending[0] = bufferStream.on(b1,b2);
            });
            if(!pending[0]){
                txn.abort();
                return false;
            }
            if(!edge.put(inPair.keyPointer(),inPair.valuePointer(),txn)){
                txn.abort();
                return false;
            }
            txn.commit();
            return true;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public boolean unsetEdge(String label,BufferStream bufferStream,boolean fromLabel){
        NativeDbi edge = env.createDbi(name,label,parentTxn);
        try(Arena arena = Arena.ofConfined(); NativeTxn txn = env.write(arena,parentTxn==null?MemorySegment.NULL:parentTxn.pointer())){
            boolean[] pending={false};
            NativeData.InPair inPair = NativeData.inPair(arena,EnvSetting.KEY_SIZE,EnvSetting.KEY_SIZE).write((b1,b2)->{
                pending[0] = bufferStream.on(b1,b2);
            });
            if(!pending[0]){
                txn.abort();
                return false;
            }
            if(fromLabel){
                if(!edge.delete(inPair.keyPointer(),txn)){
                    txn.abort();
                    return false;
                }
            }
            else if(!edge.delete(inPair.keyPointer(),inPair.valuePointer(),txn)){
                txn.abort();
                return false;
            }
            txn.commit();
            return true;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
    public boolean unset(BufferStream bufferStream){
        NativeDbi dbi = env.createDbi(name,parentTxn);
        try(Arena arena = Arena.ofConfined(); NativeTxn txn = env.write(arena,parentTxn==null?MemorySegment.NULL:parentTxn.pointer())){
            NativeData.InVal key = NativeData.in(arena,EnvSetting.KEY_SIZE);
            boolean[] pending = {false};
            key.write(buffer -> pending[0] = bufferStream.on(buffer,buffer));
            if(!pending[0]){
                txn.abort();
                return false;
            }
            NativeData.OutVal value = NativeData.out(arena);
            if(!dbi.get(key.pointer(),value.pointer(),txn)){
                txn.abort();
                return false;
            }
            if(!dbi.delete(key.pointer(),txn)){
                txn.abort();
                return false;
            }
            txn.commit();
            return true;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }


    public void view(DataStoreSummary dataStoreSummary){}

    private <T extends Recoverable> NativeDbi edgeDbi(T t){
        return (t.onEdge() && t.label() != null && t.ownerKey() != null)? env.createDbi(name,t.label(),parentTxn) : null;
    }

    private <T extends Recoverable> void recover(Metadata metadata,T t){
        nativeDataStoreProvider.onRecovering(metadata,t.key(),(k,v)->{
            Recoverable.DataHeader h = v.readHeader();
            Recoverable.DataHeader[] eh ={null};
            get(t.key(),(ek,ev)->{
                eh[0] = ev.readHeader();
                return true;
            });
            if(eh[0]==null || h.revision() > eh[0].revision()){

                v.rewind();
                set((rk,rv)->{
                    t.writeKey(rk);
                    rv.write(v);
                    return true;
                });
                if(t.onEdge() && t.ownerKey()!=null && t.label()!=null){
                    setEdge(t.label(),(dk,dv)->{
                        t.ownerKey().write(dk);
                        t.key().write(dv);
                        return true;
                    });
                }
            }
            return true;
        });
    }

    private <T extends Recoverable> void recover(Metadata metadata,RecoverableFactory<T> q){
        nativeDataStoreProvider.onRecovering(metadata,q.key(),(k,v)->{
            Recoverable.DataHeader h = v.readHeader();
            Recoverable.DataHeader[] eh ={null};
            DataBufferKey key = DataBufferKey.from(k);
            get(key,(ek,ev)->{
                eh[0] = ev.readHeader();
                return true;
            });
            if(eh[0]==null || h.revision() > eh[0].revision()){
                k.rewind();
                v.rewind();
                set((rk,rv)->{
                    rk.write(k);
                    rv.write(v);
                    return true;
                });
                k.rewind();
                setEdge(q.label(),(dk,dv)->{
                    q.key().write(dk);
                    dv.write(k);
                    return true;
                });
            }
            return true;
        });
    }

}
