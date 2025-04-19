package com.icodesoftware.lmdb.ffm;

import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.lmdb.LocalMetadata;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.Metadata;
import com.icodesoftware.service.Serviceable;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.charset.StandardCharsets;


public class NativeDbi extends NativeStat implements Serviceable {

    private static final TarantulaLogger logger = JDKLogger.getLogger(NativeDbi.class);

    private MemorySegment dbi;
    private final NativeEnv env;
    private final String storeName;
    private final String name;
    private final String label;
    private final int openFlag;
    private final int putFlag;
    private final Metadata metadata;
    private final NativeTxn parent;

    public NativeDbi(final NativeEnv env,final String name,NativeTxn parent){
        this(env,name,null,parent);
    }

    public NativeDbi(final NativeEnv env,final String name,final String label,NativeTxn parent){
        this.metadata = new LocalMetadata(env.scope(),name,label);
        this.env = env;
        this.storeName = label==null ? NativeUtil.storeName(name) : NativeUtil.storeName(name,label);
        this.name = NativeUtil.storeName(name);
        this.label = NativeUtil.storeName(label);
        this.openFlag = this.label==null? DbiMask.DBI_CREATE.mask() : (DbiMask.DBI_CREATE.mask() | DbiMask.DBI_DUP_SORT.mask());
        this.putFlag = this.label==null? 0 : PutMask.PUT_NO_DUP_DATA.mask();
        this.parent = parent;
    }

    public String name(){
        return name;
    }
    public String label(){
        return label;
    }
    public Metadata metadata(){
        return metadata;
    }

    public MemorySegment pointer(){
        return dbi;
    }

    @Override
    public void start() throws Exception {
        try(Arena a = Arena.ofConfined(); NativeTxn txn = env.write(a,parent==null?MemorySegment.NULL:parent.pointer())) {
            if(storeName!=null){
                MemorySegment dbiName = a.allocateFrom(storeName, StandardCharsets.US_ASCII);
                MemorySegment dm = env.allocate(arena -> arena.allocate(AddressLayout.ADDRESS));
                mdbDbiOpen(txn,dbiName,dm);
                txn.commit();
                dbi = dm.get(ValueLayout.ADDRESS,0);
            }
            else{
                MemorySegment dm = env.allocate(arena -> arena.allocate(AddressLayout.ADDRESS));
                mdbDbiOpen(txn,dm);
                txn.commit();
                dbi = dm.get(ValueLayout.ADDRESS,0);
            }
        }
        //stat();
    }

    @Override
    public void shutdown() throws Exception {
        mdbDbiClose();
    }

    public void drop(boolean delete){
        try(Arena arena = Arena.ofConfined(); NativeTxn txn = env.write(arena,MemorySegment.NULL)){
            if(delete) {
                mdbDrop(txn,1);
                txn.commit();
                return;
            }
            mdbDrop(txn,0);
            txn.commit();
        }
    }


    public boolean put(MemorySegment key,MemorySegment value,NativeTxn txn){
        return mdbPut(txn,key,value,putFlag) == NativeCode.MDB_SUCCESS;
    }

    public boolean get(MemorySegment key,MemorySegment value,NativeTxn txn){
        return mdbGet(txn,key,value)==NativeCode.MDB_SUCCESS;
    }

    public boolean delete(MemorySegment key,NativeTxn txn){
        return mdbDel(txn,key,MemorySegment.NULL) == NativeCode.MDB_SUCCESS;
    }

    public boolean delete(MemorySegment key,MemorySegment edge,NativeTxn txn){
        return mdbDel(txn,key,edge) == NativeCode.MDB_SUCCESS;
    }


    public NativeCursor cursor(){
        NativeCursor cursor = new NativeCursor(this.env,this,putFlag == PutMask.PUT_NO_DUP_DATA.mask());
        return cursor;
    }

    public void stat(){
        try(Arena arena = Arena.ofConfined() ; NativeTxn txn = env.read(arena)){
            NativeData.Stat stat = NativeData.stat(arena);
            mdbStat(txn,dbi,stat.pointer());
            pageSize.set(stat.pageSize());
            depth.set(stat.depth());
            branchPages.set(stat.branchPages());
            leafPages.set(stat.leafPages());
            overflowPages.set(stat.overflowPages());
            entries.set(stat.entries());
            txn.abort();
        }
    }

    private int mdbPut(NativeTxn txn,MemorySegment key,MemorySegment value,int flags){
        try{
            MemorySegment mdbPut = env.lib.find("mdb_put").get();//-30781 BAD VALUE SIZE
            MethodHandle caller = env.linker.downcallHandle(mdbPut,FunctionDescriptor.of(ValueLayout.JAVA_INT,ValueLayout.ADDRESS,ValueLayout.ADDRESS,ValueLayout.ADDRESS,ValueLayout.ADDRESS,ValueLayout.JAVA_INT));
            return (int)caller.invokeExact(txn.pointer(),dbi,key,value,flags);
        }catch (Throwable throwable){
            txn.abort();
            logger.error("mdb_put",throwable);
            throw new RuntimeException(throwable);
        }
    }

    private int mdbGet(NativeTxn txn,MemorySegment key,MemorySegment value){
        try{
            MemorySegment mdbGet = env.lib.find("mdb_get").get();
            MethodHandle caller = env.linker.downcallHandle(mdbGet,FunctionDescriptor.of(ValueLayout.JAVA_INT,ValueLayout.ADDRESS,ValueLayout.ADDRESS,ValueLayout.ADDRESS,ValueLayout.ADDRESS));
            return (int)caller.invokeExact(txn.pointer(),dbi,key,value);
        }catch (Throwable throwable){
            txn.abort();
            logger.error("mdb_get",throwable);
            throw new RuntimeException(throwable);
        }
    }

    private int mdbDel(NativeTxn txn,MemorySegment key,MemorySegment value){
        try{
            MemorySegment mdbDel = env.lib.find("mdb_del").get();
            MethodHandle caller = env.linker.downcallHandle(mdbDel,FunctionDescriptor.of(ValueLayout.JAVA_INT,ValueLayout.ADDRESS,ValueLayout.ADDRESS,ValueLayout.ADDRESS,ValueLayout.ADDRESS));
            return (int)caller.invokeExact(txn.pointer(),dbi,key,value);
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

    private void mdbDbiOpen(NativeTxn txn,MemorySegment dbName,MemorySegment dbi){
        try{
            MemorySegment mdbDbiOpen = env.lib.find("mdb_dbi_open").get();
            MethodHandle caller = env.linker.downcallHandle(mdbDbiOpen,FunctionDescriptor.of(ValueLayout.JAVA_INT,ValueLayout.ADDRESS,ValueLayout.ADDRESS,ValueLayout.JAVA_INT,ValueLayout.ADDRESS));
            int ret = (int)caller.invokeExact(txn.pointer(),dbName,openFlag,dbi);
            if(ret != 0) throw new RuntimeException("code ["+ret+"]");
        }catch (Throwable throwable){
            txn.abort();
            logger.error("mdb_dbi_open",throwable);
            throw new RuntimeException(throwable);
        }
    }

    private void mdbDbiOpen(NativeTxn txn,MemorySegment dbi){
        try{
            MemorySegment mdbDbiOpen = env.lib.find("mdb_dbi_open").get();
            MethodHandle caller = env.linker.downcallHandle(mdbDbiOpen,FunctionDescriptor.of(ValueLayout.JAVA_INT,ValueLayout.ADDRESS,ValueLayout.ADDRESS,ValueLayout.JAVA_INT,ValueLayout.ADDRESS));
            int ret = (int)caller.invokeExact(txn.pointer(),MemorySegment.NULL,openFlag,dbi);
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
