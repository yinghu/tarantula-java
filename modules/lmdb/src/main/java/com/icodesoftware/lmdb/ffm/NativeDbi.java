package com.icodesoftware.lmdb.ffm;

import com.icodesoftware.Recoverable;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.Serviceable;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.nio.charset.StandardCharsets;


public class NativeDbi extends NativeStat implements Serviceable {

    private static final TarantulaLogger logger = JDKLogger.getLogger(NativeDbi.class);

    private MemorySegment dbi;
    private final NativeEnv env;
    private final String name;


    public NativeDbi(final NativeEnv env,final String name){
        this.env = env;
        this.name = name;
    }

    public MemorySegment pointer(){
        return dbi;
    }

    @Override
    public void start() throws Exception {
        try(Arena a = Arena.ofConfined(); NativeTxn txn = env.write(a)) {
            MemorySegment dbiName = a.allocateFrom(name, StandardCharsets.US_ASCII);
            MemorySegment dm = env.allocate(arena -> arena.allocate(AddressLayout.ADDRESS));
            mdbDbiOpen(txn,dbiName,dm,MaskFlag.DBI_CREATE.mask());
            txn.commit();
            dbi = dm.get(ValueLayout.ADDRESS,0);
        }
        stat();
    }

    @Override
    public void shutdown() throws Exception {
        mdbDbiClose();
    }

    public void drop(boolean delete){
        try(Arena arena = Arena.ofConfined(); NativeTxn txn = env.write(arena)){
            if(delete) {
                mdbDrop(txn,1);
                txn.commit();
                return;
            }
            mdbDrop(txn,0);
            txn.commit();
        }
    }

    public void put(Recoverable.DataBuffer key,Recoverable.DataBuffer value){
        try(Arena arena = Arena.ofConfined();NativeTxn txn = env.write(arena)){
            MemorySegment k = mdbVal(arena,key);
            MemorySegment v = mdbVal(arena,value);
            mdbPut(txn,dbi,k,v,0);
            txn.commit();
        }
    }

    public void delete(Recoverable.DataBuffer key){
        try(Arena arena = Arena.ofConfined();NativeTxn txn = env.write(arena)){
            MemorySegment k = mdbVal(arena,key);
            mdbDel(txn,k,MemorySegment.NULL);
            txn.commit();
        }
    }

    public void get(Recoverable.DataBuffer key,Recoverable.DataBuffer value){
        try(Arena arena = Arena.ofConfined(); NativeTxn txn = env.read(arena)){
            MemorySegment k = mdbVal(arena,key);
            MemorySegment v = mdbVal(arena);
            mdbGet(txn,dbi,k,v);
            MemorySegment data = v.get(ValueLayout.ADDRESS,8);
            long len = v.get(ValueLayout.JAVA_LONG,0);
            MemorySegment x = data.reinterpret(len,arena,null);
            for(long i=0;i<len-1;i++){
                value.writeByte(x.getAtIndex(ValueLayout.JAVA_BYTE,i));
            }
            value.flip();
            txn.abort();
        }
    }



    public NativeCursor openCursor(){
        NativeCursor cursor = new NativeCursor(this.env,this);
        cursor.open();
        return cursor;
    }

    public void stat(){
        try(Arena arena = Arena.ofConfined();NativeTxn txn = env.read(arena)){
            MemorySegment stat = dbiStat(arena);
            mdbStat(txn,dbi,stat);
            pageSize.set(stat.get(ValueLayout.JAVA_INT,0));
            depth.set(stat.get(ValueLayout.JAVA_INT,4));
            branchPages.set(stat.get(ValueLayout.JAVA_LONG,8));
            leafPages.set(stat.get(ValueLayout.JAVA_LONG,16));
            overflowPages.set(stat.get(ValueLayout.JAVA_LONG,24));
            entries.set(stat.get(ValueLayout.JAVA_LONG,32));
            txn.abort();
        }
    }


    private MemorySegment mdbVal(Arena a, Recoverable.DataBuffer value){
        long len = value.remaining()+1;
        StructLayout struct = MemoryLayout.structLayout(ValueLayout.JAVA_LONG.withName("mv_size"),ValueLayout.ADDRESS.withName("mv_data"));
        MemorySegment pointer = a.allocate(struct);
        VarHandle vSize = struct.varHandle(MemoryLayout.PathElement.groupElement("mv_size"));
        vSize.set(pointer,0,len);
        VarHandle vData = struct.varHandle(MemoryLayout.PathElement.groupElement("mv_data"));
        MemorySegment sequence = a.allocate(MemoryLayout.sequenceLayout(len,ValueLayout.JAVA_BYTE));
        long offset = 0;
        while (value.hasRemaining()){
            sequence.set(ValueLayout.JAVA_BYTE,offset++,value.readByte());
        }
        sequence.set(ValueLayout.JAVA_BYTE,offset,(byte) '\0');
        vData.set(pointer,0,sequence);
        return pointer;
    }

    public MemorySegment mdbVal(Arena a){
        StructLayout struct = MemoryLayout.structLayout(ValueLayout.JAVA_LONG.withName("mv_size"),ValueLayout.ADDRESS.withName("mv_data"));
        MemorySegment pointer = a.allocate(struct);
        return pointer;
    }

    private MemorySegment dbiStat(Arena a){
        StructLayout layout = MemoryLayout.structLayout(ValueLayout.JAVA_INT.withName("ms_psize"),ValueLayout.JAVA_INT.withName("ms_depth"),
                ValueLayout.JAVA_LONG.withName("ms_branch_pages"),ValueLayout.JAVA_LONG.withName("ms_leaf_pages"),ValueLayout.JAVA_LONG.withName("ms_overflow_pages"),ValueLayout.JAVA_LONG.withName("ms_entries"));
        MemorySegment memorySegment = a.allocate(layout);
        return memorySegment;
    }

    private void mdbPut(NativeTxn txn,MemorySegment dbi,MemorySegment key,MemorySegment value,int flags){
        try{
            MemorySegment mdbPut = env.lib.find("mdb_put").get();//-30781 BAD VALUE SIZE
            MethodHandle caller = env.linker.downcallHandle(mdbPut,FunctionDescriptor.of(ValueLayout.JAVA_INT,ValueLayout.ADDRESS,ValueLayout.ADDRESS,ValueLayout.ADDRESS,ValueLayout.ADDRESS,ValueLayout.JAVA_INT));
            int ret = (int)caller.invokeExact(txn.pointer(),dbi,key,value,flags);
            if(ret != 0) throw new RuntimeException("code ["+ret+"]");
        }catch (Throwable throwable){
            txn.abort();
            logger.error("mdb_put",throwable);
            throw new RuntimeException(throwable);
        }
    }

    private void mdbGet(NativeTxn txn,MemorySegment dbi,MemorySegment key,MemorySegment value){
        try{
            MemorySegment mdbGet = env.lib.find("mdb_get").get();
            MethodHandle caller = env.linker.downcallHandle(mdbGet,FunctionDescriptor.of(ValueLayout.JAVA_INT,ValueLayout.ADDRESS,ValueLayout.ADDRESS,ValueLayout.ADDRESS,ValueLayout.ADDRESS));
            int ret = (int)caller.invokeExact(txn.pointer(),dbi,key,value);
            if(ret != 0) throw new RuntimeException("code ["+ret+"]");
        }catch (Throwable throwable){
            txn.abort();
            logger.error("mdb_get",throwable);
            throw new RuntimeException(throwable);
        }
    }

    private void mdbDel(NativeTxn txn,MemorySegment key,MemorySegment value){
        try{
            MemorySegment mdbDel = env.lib.find("mdb_del").get();
            MethodHandle caller = env.linker.downcallHandle(mdbDel,FunctionDescriptor.of(ValueLayout.JAVA_INT,ValueLayout.ADDRESS,ValueLayout.ADDRESS,ValueLayout.ADDRESS,ValueLayout.ADDRESS));
            int ret = (int)caller.invokeExact(txn.pointer(),dbi,key,value);
            if(ret != 0) throw new RuntimeException("code ["+ret+"]");
        }catch (Throwable throwable){
            txn.abort();
            logger.error("mdb_del",throwable);
            throw new RuntimeException(throwable);
        }
    }

    private void mdbStat(NativeTxn txn,MemorySegment dbi,MemorySegment stat){
        try{
            MemorySegment mdbDel = env.lib.find("mdb_stat").get();
            MethodHandle caller = env.linker.downcallHandle(mdbDel,FunctionDescriptor.of(ValueLayout.JAVA_INT,ValueLayout.ADDRESS,ValueLayout.ADDRESS,ValueLayout.ADDRESS));
            int ret = (int)caller.invokeExact(txn.pointer(),dbi,stat);
            if(ret != 0) throw new RuntimeException("code ["+ret+"]");
        }catch (Throwable throwable){
            txn.abort();
            logger.error("mdb_stat",throwable);
            throw new RuntimeException(throwable);
        }
    }

    private void mdbDbiOpen(NativeTxn txn,MemorySegment dbName,MemorySegment dbi,int flags){
        try{
            MemorySegment mdbDbiOpen = env.lib.find("mdb_dbi_open").get();
            MethodHandle caller = env.linker.downcallHandle(mdbDbiOpen,FunctionDescriptor.of(ValueLayout.JAVA_INT,ValueLayout.ADDRESS,ValueLayout.ADDRESS,ValueLayout.JAVA_INT,ValueLayout.ADDRESS));
            int ret = (int)caller.invokeExact(txn.pointer(),dbName,flags,dbi);
            if(ret != 0) throw new RuntimeException("code ["+ret+"]");
        }catch (Throwable throwable){
            txn.abort();
            logger.error("mdb_dbi_open",throwable);
            throw new RuntimeException(throwable);
        }
    }

    private void mdbDbiClose(){
        try{
            MemorySegment mdbDbiClose = env.lib.find("mdb_dbi_close").get();
            MethodHandle caller = env.linker.downcallHandle(mdbDbiClose,FunctionDescriptor.ofVoid(ValueLayout.ADDRESS,ValueLayout.ADDRESS));
            caller.invokeExact(env.pointer(),dbi);
        }catch (Throwable throwable){
            logger.error("mdb_dbi_close",throwable);
            throw new RuntimeException(throwable);
        }
    }

    private void mdbDrop(NativeTxn txn,int deleted){
        try{
            MemorySegment mdbDrop = env.lib.find("mdb_drop").get();
            MethodHandle caller = env.linker.downcallHandle(mdbDrop,FunctionDescriptor.of(ValueLayout.JAVA_INT,ValueLayout.ADDRESS,ValueLayout.ADDRESS,ValueLayout.JAVA_INT));
            int ret = (int)caller.invokeExact(txn.pointer(),dbi,deleted);
            if(ret != 0) throw new RuntimeException("code ["+ret+"]");
        }catch (Throwable throwable){
            txn.abort();
            logger.error("mdb_drop",throwable);
            throw new RuntimeException(throwable);
        }
    }


}
