package com.icodesoftware.lmdb.ffm;

import com.icodesoftware.DataStore;
import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.lmdb.EnvSetting;
import com.icodesoftware.service.DataStoreSummary;
import com.icodesoftware.util.LocalHeader;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.ArrayList;
import java.util.List;


public class NativeDataStore implements DataStore, DataStore.Backup {

    private NativeDataStoreProvider nativeDataStoreProvider;
    private NativeEnv env;
    private String name;


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
        NativeDbi dbi = env.createDbi(name);
        try(Arena arena = Arena.ofConfined(); NativeTxn txn = env.write(arena)){
            MemorySegment key = NativeData.in(arena, EnvSetting.KEY_SIZE).write(buffer -> nativeDataStoreProvider.assign(buffer))
                    .read(buffer -> t.readKey(buffer));
            MemorySegment value = NativeData.in(arena,EnvSetting.VALUE_SIZE).write(buffer -> {
                buffer.writeHeader(LocalHeader.create(t.getFactoryId(),t.getClassId(),1L));
                if(!t.write(buffer)) throw new RuntimeException("no data");
            }).pointer();
            if(!dbi.put(key,value,txn)){
                txn.abort();
                return false;
            }
            txn.commit();
            return true;
        }
        catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public <T extends Recoverable> boolean createIfAbsent(T t, boolean loading) {
        NativeDbi dbi = env.createDbi(name);
        try(Arena arena = Arena.ofConfined(); NativeTxn txn = env.write(arena)){
            MemorySegment key = NativeData.in(arena,EnvSetting.KEY_SIZE).write(buffer ->{
                if(!t.writeKey(buffer)){
                    throw new RuntimeException("Key must be assigned");
                }
            }).pointer();
            NativeData.OutVal outVal = NativeData.out(arena);
            if(dbi.get(key,outVal.pointer(),txn)){
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
            MemorySegment value = NativeData.in(arena,EnvSetting.VALUE_SIZE).write(buffer -> t.write(buffer)).pointer();
            if(!dbi.put(key,value,txn)) throw new RuntimeException("Cannot put data");
            txn.commit();
            return true;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public <T extends Recoverable> boolean load(T t) {
        NativeDbi dbi = env.createDbi(name);
        try(Arena arena = Arena.ofConfined();NativeTxn txn = env.read(arena)){
            MemorySegment key = NativeData.in(arena,EnvSetting.KEY_SIZE).write(buffer -> t.writeKey(buffer)).read(buffer -> {});
            NativeData.OutVal value = NativeData.out(arena);
            if(!dbi.get(key,value.pointer(),txn)){
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
        NativeDbi dbi = env.createDbi(name);
        try(Arena arena = Arena.ofConfined(); NativeTxn txn = env.write(arena)){
            MemorySegment key = NativeData.in(arena,EnvSetting.KEY_SIZE).write(buffer -> t.writeKey(buffer)).pointer();
            NativeData.OutVal value = NativeData.out(arena);
            if(!dbi.get(key,value.pointer(),txn)) return false;
            boolean[] pending ={false};
            value.read(arena,buffer -> {
                Recoverable.DataHeader h = buffer.readHeader();
                pending[0] = h.revision() == t.revision();
            });
            if(!pending[0]){
                txn.abort();
                return false;
            }
            MemorySegment update = NativeData.in(arena,EnvSetting.VALUE_SIZE).write(buffer -> {
                buffer.writeHeader(LocalHeader.create(t.getFactoryId(),t.getClassId(),t.revision()+1));
                t.write(buffer);
            }).pointer();
            if(!dbi.put(key,update,txn)){
                txn.abort();
                return false;
            }
            t.revision(t.revision()+1);
            txn.commit();
            return true;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public <T extends Recoverable> boolean delete(T t){
        NativeDbi dbi = env.createDbi(name);
        try(Arena arena = Arena.ofConfined(); NativeTxn txn = env.write(arena)){
            MemorySegment key = NativeData.in(arena,EnvSetting.KEY_SIZE).write(buffer -> t.writeKey(buffer)).pointer();
            NativeData.OutVal value = NativeData.out(arena);
            if(!dbi.get(key,value.pointer(),txn)){
                txn.abort();
                return false;
            }
            if(!dbi.delete(key,txn)){
                txn.abort();
                return false;
            }
            txn.commit();
            return true;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public <T extends Recoverable> boolean createEdge(T t,String label){
        NativeDbi edge = env.createDbi(name,label);
        try(Arena arena = Arena.ofConfined(); NativeTxn txn = env.write(arena)){
            MemorySegment key = NativeData.in(arena,EnvSetting.KEY_SIZE).write(buffer -> t.ownerKey().write(buffer)).pointer();
            MemorySegment value = NativeData.in(arena,EnvSetting.KEY_SIZE).write(buffer -> t.key().write(buffer)).pointer();
            if(!edge.put(key,value,txn)){
                txn.abort();
                return false;
            }
            txn.commit();
            return true;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public  boolean deleteEdge(Recoverable.Key t,String label){
        NativeDbi edge = env.createDbi(name,label);
        try(Arena arena = Arena.ofConfined(); NativeTxn txn = env.write(arena)){
            MemorySegment key = NativeData.in(arena,EnvSetting.KEY_SIZE).write(buffer -> t.write(buffer)).pointer();
            if(!edge.delete(key,txn)){
                txn.abort();
                return false;
            }
            txn.commit();
            return true;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public boolean deleteEdge(Recoverable.Key t,Recoverable.Key edgeKey,String label){
        NativeDbi edge = env.createDbi(name,label);
        try(Arena arena = Arena.ofConfined(); NativeTxn txn = env.write(arena)){
            MemorySegment key = NativeData.in(arena,EnvSetting.KEY_SIZE).write(buffer -> t.write(buffer)).pointer();
            MemorySegment value = NativeData.in(arena,EnvSetting.KEY_SIZE).write(buffer -> edgeKey.write(buffer)).pointer();
            if(!edge.delete(key,value,txn)){
                txn.abort();
                return false;
            }
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
        NativeDbi dbi = env.createDbi(name);
        NativeDbi edge = env.createDbi(name,query.label());
        try(NativeCursor cursor = edge.cursor()){
            cursor.read().forEach(query.key(),(k,v)->{
                v.limit(v.remaining()-1);
                NativeData.OutVal out = NativeData.out(cursor.arena());
                MemorySegment pv = NativeData.in(cursor.arena(),100).write(buffer -> v.read(buffer)).pointer();
                boolean[] streaming ={true};
                if(dbi.get(pv,out.pointer(),cursor.txn())){
                    out.read(cursor.arena(),buffer -> {
                        Recoverable.DataHeader h = buffer.readHeader();
                        T t = query.create();
                        t.read(buffer);
                        t.revision(h.revision());
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
        NativeDbi dbi = env.createDbi(name);
        NativeCursor cursor = dbi.cursor();
        cursor.read().forEach(stream);
    }

    public void drop(boolean delete){
        NativeDbi dbi = env.createDbi(name);
        dbi.drop(delete);
    }

    public boolean get(Recoverable.Key key, BufferStream stream){
        NativeDbi dbi = env.createDbi(name);
        try(Arena arena = Arena.ofConfined();NativeTxn txn = env.read(arena)){
            MemorySegment k = NativeData.in(arena,EnvSetting.KEY_SIZE).write(buffer -> key.write(buffer)).pointer();
            NativeData.OutVal v = NativeData.out(arena);
            if(!dbi.get(k,v.pointer(),txn)){
                txn.abort();
                return false;
            }
            v.read(arena,buffer -> {
                stream.on(null,buffer);
            });
            txn.abort();
        }
        return true;
    }
    public boolean set(BufferStream bufferStream){

        return true;
    }

    public void forEachEdgeKey(Recoverable.Key key,String label,BufferStream bufferStream){
        NativeDbi edge = env.createDbi(name,label);
        try(NativeCursor cursor = edge.cursor()){
            cursor.read().forEach(key,bufferStream);
        }
    }

    public void forEachEdgeKeyValue(Recoverable.Key key,String label,BufferStream bufferStream){
        NativeDbi edge = env.createDbi(name,label);
        NativeDbi dbi = env.createDbi(name);
        try(NativeCursor cursor = edge.cursor()){
            cursor.read().forEach(key,(k,v)->{
                v.limit(v.remaining()-1);
                NativeData.OutVal out = NativeData.out(cursor.arena());
                MemorySegment pv = NativeData.in(cursor.arena(),100).write(buffer -> v.read(buffer)).pointer();
                boolean[] streaming ={true};
                if(dbi.get(pv,out.pointer(),cursor.txn())){
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
        NativeDbi edge = env.createDbi(name,label);
        try(Arena arena = Arena.ofConfined(); NativeTxn txn = env.write(arena)){
            boolean[] pending={false};
            NativeData.InPair inPair = NativeData.inPair(arena,EnvSetting.KEY_SIZE).write((b1,b2)->{
                pending[0] = bufferStream.on(b1,b2);
            });
            if(!pending[0]){
                txn.abort();
                return false;
            }
            if(!edge.put(inPair.pointer1(),inPair.pointer2(),txn)){
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
        NativeDbi edge = env.createDbi(name,label);
        try(Arena arena = Arena.ofConfined(); NativeTxn txn = env.write(arena)){
            boolean[] pending={false};
            NativeData.InPair inPair = NativeData.inPair(arena,EnvSetting.KEY_SIZE).write((b1,b2)->{
                pending[0] = bufferStream.on(b1,b2);
            });
            if(!pending[0]){
                txn.abort();
                return false;
            }
            if(fromLabel){
                if(!edge.delete(inPair.pointer1(),txn)){
                    txn.abort();
                    return false;
                }
            }
            else if(!edge.delete(inPair.pointer1(),inPair.pointer2(),txn)){
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
        NativeDbi dbi = env.createDbi(name);
        try(Arena arena = Arena.ofConfined(); NativeTxn txn = env.write(arena)){
            MemorySegment key = NativeData.in(arena,EnvSetting.KEY_SIZE).write(buffer -> bufferStream.on(buffer,buffer)).pointer();
            NativeData.OutVal value = NativeData.out(arena);
            if(!dbi.get(key,value.pointer(),txn)){
                txn.abort();
                return false;
            }
            if(!dbi.delete(key,txn)){
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

}
