package com.icodesoftware.lmdb.ffm;

import com.icodesoftware.DataStore;
import com.icodesoftware.Recoverable;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.lmdb.EnvSetting;
import com.icodesoftware.logging.JDKLogger;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

public class NativeCursor implements AutoCloseable{

    private static final TarantulaLogger logger = JDKLogger.getLogger(NativeCursor.class);
    private final NativeEnv env;
    private final NativeDbi dbi;
    private final Arena arena;
    private MemorySegment cursor;
    private NativeTxn txn;
    private final boolean edge;
    public NativeCursor(final NativeEnv env,final NativeDbi dbi,boolean edge){
        this.env = env;
        this.dbi = dbi;
        this.edge = edge;
        this.arena = Arena.ofConfined();
    }

    public NativeTxn txn(){
        return txn;
    }
    public Arena arena(){
        return arena;
    }
    public NativeCursor read(){
        NativeTxn txn = env.read(arena);
        MemorySegment cm = arena.allocate(AddressLayout.ADDRESS);
        mdbCursorOpen(txn,cm);
        this.txn = txn;
        cursor = cm.get(ValueLayout.ADDRESS,0);
        return this;
    }
    public NativeCursor write(){
        NativeTxn txn = env.write(arena);
        MemorySegment cm = arena.allocate(AddressLayout.ADDRESS);
        mdbCursorOpen(txn,cm);
        this.txn = txn;
        cursor = cm.get(ValueLayout.ADDRESS,0);
        return this;
    }

    public void forEach(Recoverable.Key key, DataStore.BufferStream stream){
        if(!edge) return;
        NativeData.InVal k = NativeData.in(arena, EnvSetting.KEY_SIZE);
        k.write(buffer -> key.write(buffer));
        NativeData.OutVal v = NativeData.out(arena);
        boolean next = mdbCursorGet(k.pointer(),v.pointer(), CursorMask.MDB_SET.mask());
        if(!next) return;
        next = mdbCursorGet(k.pointer(),v.pointer(), CursorMask.MDB_FIRST_DUP.mask());
        boolean[] streaming = {false};
        if(next){
            v.read(arena,buffer -> {
                streaming[0] = stream.on(null,buffer);
            });
        }
        if(!streaming[0]) return;
        while (mdbCursorGet(k.pointer(),v.pointer(), CursorMask.MDB_NEXT_DUP.mask())){
            v.read(arena,buffer -> {
                streaming[0] = stream.on(null,buffer);
            });
            if(!streaming[0]) break;
        }
    }

    public void forEach(DataStore.BufferStream stream){
        NativeData.OutPair kv = NativeData.outPair(arena);
        while (mdbCursorGet(kv.keyPointer(),kv.valuePointer(), CursorMask.MDB_NEXT.mask())){
            if(!kv.stream(arena,stream)) break;
        }
    }

    public void close(){
        txn.abort();
        mdbCursorClose();
        arena.close();
    }


    private void mdbCursorOpen(NativeTxn txn, MemorySegment cursor){
        try{
            MemorySegment mdbCursorOpen = env.lib.find("mdb_cursor_open").get();
            MethodHandle caller = env.linker.downcallHandle(mdbCursorOpen, FunctionDescriptor.of(ValueLayout.JAVA_INT,ValueLayout.ADDRESS,ValueLayout.ADDRESS,ValueLayout.ADDRESS));
            int ret = (int)caller.invokeExact(txn.pointer(),dbi.pointer(),cursor);
            if(ret != 0) throw new RuntimeException("code ["+ret+"]");
        }catch (Throwable throwable){
            txn.abort();
            logger.error("mdb_cursor_open",throwable);
            throw new RuntimeException(throwable);
        }
    }

    private boolean mdbCursorGet(MemorySegment key,MemorySegment value,int op){
        try{
            MemorySegment mdbGet = env.lib.find("mdb_cursor_get").get();
            MethodHandle caller = env.linker.downcallHandle(mdbGet,FunctionDescriptor.of(ValueLayout.JAVA_INT,ValueLayout.ADDRESS,ValueLayout.ADDRESS,ValueLayout.ADDRESS,ValueLayout.JAVA_INT));
            int ret = (int)caller.invokeExact(cursor,key,value,op);
            if(ret == NativeCode.MDB_SUCCESS) return true;
            if(ret == NativeCode.MDB_NOT_FOUND) return false;
            throw new RuntimeException("code ["+ret+"]");
        }catch (Throwable throwable){
            logger.error("mdb_cursor_get",throwable);
            throw new RuntimeException(throwable);
        }
    }


    private void mdbCursorClose(){
        try{
            MemorySegment mdbCursorClose = env.lib.find("mdb_cursor_close").get();
            MethodHandle caller = env.linker.downcallHandle(mdbCursorClose,FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));
            caller.invokeExact(cursor);
        }catch (Throwable throwable){
            logger.error("mdb_cursor_close",throwable);
            throw new RuntimeException(throwable);
        }
    }
}
