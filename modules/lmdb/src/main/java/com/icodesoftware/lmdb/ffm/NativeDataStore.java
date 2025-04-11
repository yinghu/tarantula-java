package com.icodesoftware.lmdb.ffm;

import com.icodesoftware.DataStore;
import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.util.LocalHeader;

import java.lang.foreign.Arena;

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
        //if(t.onEdge() && t.label() != null){

        //}
        try(Arena arena = Arena.ofConfined(); NativeTxn txn = env.write(arena); Recoverable.DataBufferPair pair = nativeDataStoreProvider.dataBufferPair()){
            Recoverable.DataBuffer key = pair.key();
            Recoverable.DataBuffer value = pair.value();
            nativeDataStoreProvider.assign(key);
            key.flip();
            t.readKey(key);
            key.rewind();
            value.writeHeader(LocalHeader.create(t.getFactoryId(),t.getClassId(),1L));
            if(!t.write(value)) return false;
            value.flip();
            dbi.put(key,value,txn);
            txn.commit();
            return true;
        }
    }

    public <T extends Recoverable> boolean load(T t) {
        NativeDbi dbi = env.createDbi(name);
        try(Recoverable.DataBufferPair pair = nativeDataStoreProvider.dataBufferPair()){
            Recoverable.DataBuffer key = pair.key();
            Recoverable.DataBuffer value = pair.value();
            if(!t.writeKey(key)){
                return false;
            }
            key.flip();
            dbi.get(key,value);
            Recoverable.DataHeader header = value.readHeader();
            t.read(value);
            t.revision(header.revision());
            return true;
        }
    }

    public <T extends Recoverable> boolean update(T t) {
        NativeDbi dbi = env.createDbi(name);
        try(Arena arena = Arena.ofConfined(); NativeTxn txn = env.write(arena); Recoverable.DataBufferPair pair = nativeDataStoreProvider.dataBufferPair()){
            Recoverable.DataBuffer key = pair.key();
            Recoverable.DataBuffer value = pair.value();
            if(!t.readKey(key)) return false;
            key.flip();

            value.writeHeader(LocalHeader.create(t.getFactoryId(),t.getClassId(),1L));
            if(!t.write(value)) return false;
            value.flip();
            dbi.put(key,value,txn);
            txn.commit();
            return true;
        }
    }


    public <T extends Recoverable> void list(RecoverableFactory<T> query, DataStore.Stream<T> stream) {

    }
}
