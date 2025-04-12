package com.icodesoftware.lmdb.ffm;

import com.icodesoftware.DataStore;
import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.util.LocalHeader;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;


public class NativeDataStore {

    private NativeDataStoreProvider nativeDataStoreProvider;
    private NativeEnv env;
    private String name;

    public NativeDataStore(String name,NativeDataStoreProvider nativeDataStoreProvider,NativeEnv nativeEnv){
        this.name = name;
        this.nativeDataStoreProvider = nativeDataStoreProvider;
        this.env = nativeEnv;
    }

    public <T extends Recoverable> boolean create(T t) {
        NativeDbi dbi = env.createDbi(name);
        try(Arena arena = Arena.ofConfined(); NativeTxn txn = env.write(arena)){
            MemorySegment key = NativeData.in(arena,100).write(buffer -> nativeDataStoreProvider.assign(buffer))
                    .read(buffer -> t.readKey(buffer));
            MemorySegment value = NativeData.in(arena,100).write(buffer -> {
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
            MemorySegment key = NativeData.in(arena,100).write(buffer ->{
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
            MemorySegment value = NativeData.in(arena,1000).write(buffer -> t.write(buffer)).pointer();
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
            MemorySegment key = NativeData.in(arena,100).write(buffer -> t.writeKey(buffer)).read(buffer -> {});
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
            MemorySegment key = NativeData.in(arena,100).write(buffer -> t.writeKey(buffer)).pointer();
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
            MemorySegment update = NativeData.in(arena,1000).write(buffer -> {
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
            MemorySegment key = NativeData.in(arena,100).write(buffer -> t.writeKey(buffer)).pointer();
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
        }
    }

    public <T extends Recoverable> void list(RecoverableFactory<T> query, DataStore.Stream<T> stream) {

    }

    public void forEach(DataStore.BufferStream stream) {
        NativeDbi dbi = env.createDbi(name);
        NativeCursor cursor = dbi.cursor();
        cursor.read().forEach(stream);
    }
}
