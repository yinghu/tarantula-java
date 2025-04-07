package com.icodesoftware.lmdb.ffm;

import com.icodesoftware.Recoverable;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

public class NativeCursor {

    private static final TarantulaLogger logger = JDKLogger.getLogger(NativeCursor.class);
    private final NativeEnv env;
    private final NativeDbi dbi;

    private MemorySegment cursor;

    public NativeCursor(final NativeEnv env,final NativeDbi dbi){
        this.env = env;
        this.dbi = dbi;
    }

    public void open(){
        try(Arena arena = Arena.ofConfined(); NativeTxn txn = env.read(arena)){
            MemorySegment cm = arena.allocate(AddressLayout.ADDRESS);
            mdbCursorOpen(txn,cm);
            cursor = cm.get(ValueLayout.ADDRESS,0);
        }
    }

    public void next(Recoverable.DataBuffer key, Recoverable.DataBuffer value){
        try(Arena arena = Arena.ofConfined()){
            MemorySegment k = dbi.mdbVal(arena);
            MemorySegment v = dbi.mdbVal(arena);
            mdbCursorGet(k,v,CursorOp.MDB_NEXT.mask());

            MemorySegment keyData = k.get(ValueLayout.ADDRESS,8);
            long kLen = k.get(ValueLayout.JAVA_LONG,0);
            MemorySegment xk = keyData.reinterpret(kLen,arena,null);
            for(long i=0;i<kLen-1;i++){
                value.writeByte(xk.getAtIndex(ValueLayout.JAVA_BYTE,i));
            }
            key.flip();

            MemorySegment valueData = v.get(ValueLayout.ADDRESS,8);
            long vLen = v.get(ValueLayout.JAVA_LONG,0);
            MemorySegment xv = valueData.reinterpret(vLen,arena,null);
            for(long i=0;i<vLen-1;i++) {
                value.writeByte(xv.getAtIndex(ValueLayout.JAVA_BYTE,i));
            }
            value.flip();
        }
    }

    //count of duplicates for current key
    public long count(){
        long count;
        try(Arena arena = Arena.ofConfined()){
            MemorySegment cm = arena.allocate(ValueLayout.JAVA_LONG);
            mdbCursorCount(cm);
            count = cm.get(ValueLayout.JAVA_LONG,0);
        }
        return count;
    }

    public void close(){
        mdbCursorClose();
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

    private void mdbCursorGet(MemorySegment key,MemorySegment value,int op){
        try{
            MemorySegment mdbGet = env.lib.find("mdb_cursor_get").get();
            MethodHandle caller = env.linker.downcallHandle(mdbGet,FunctionDescriptor.of(ValueLayout.JAVA_INT,ValueLayout.ADDRESS,ValueLayout.ADDRESS,ValueLayout.ADDRESS,ValueLayout.JAVA_INT));
            int ret = (int)caller.invokeExact(cursor,key,value,op);
            if(ret != 0) throw new RuntimeException("code ["+ret+"]");
        }catch (Throwable throwable){
            logger.error("mdb_cursor_get",throwable);
            throw new RuntimeException(throwable);
        }
    }


    private void mdbCursorCount(MemorySegment count){
        try{
            MemorySegment mdbCursorCount = env.lib.find("mdb_cursor_count").get();
            MethodHandle caller = env.linker.downcallHandle(mdbCursorCount, FunctionDescriptor.of(ValueLayout.JAVA_INT,ValueLayout.ADDRESS,ValueLayout.ADDRESS));
            int ret = (int)caller.invokeExact(cursor,count);
            if(ret != 0) throw new RuntimeException("code ["+ret+"]");
        }catch (Throwable throwable){
            logger.error("mdb_cursor_count",throwable);
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
