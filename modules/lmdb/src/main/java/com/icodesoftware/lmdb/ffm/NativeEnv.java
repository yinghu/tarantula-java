package com.icodesoftware.lmdb.ffm;

import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.lmdb.EnvSetting;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.Serviceable;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.nio.charset.StandardCharsets;

public class NativeEnv implements Serviceable {

    private static TarantulaLogger logger = JDKLogger.getLogger(NativeEnv.class);

    private static String nativeLibPath = "/home/yinghu/lmdb.dll";
    private static String databasePath = "/home/yinghu/tst";
    private SymbolLookup lib;
    private Linker linker;

    private Arena arena = Arena.ofShared();
    private MemorySegment env;
    private MemorySegment dbi;

    @Override
    public void start() throws Exception {
        lib = SymbolLookup.libraryLookup(nativeLibPath,arena);
        linker = Linker.nativeLinker();
        mdbEnvCreate();
        mdbEnvSetMapSize();
        mdbEnvSetMaxReaders();
        mdbEnvSetMaxDbs();
        mdbEnvOpen();
        createDbi("test699");
        MemorySegment stat = envStat(arena);
        mdbEnvStat(stat);
        System.out.println(stat.get(ValueLayout.JAVA_INT,0));
        System.out.println(stat.get(ValueLayout.JAVA_LONG,32));
    }

    @Override
    public void shutdown() throws Exception {
        mdbEnvSync(1);
        mdbDbiClose(dbi);
        mdbEnvClose();
        arena.close();
    }

    public void createDbi(String name){
        try(Arena a = Arena.ofConfined()) {
            MemorySegment txn = a.allocate(AddressLayout.ADDRESS);
            MemorySegment dbiName = a.allocateFrom(name);
            MemorySegment dm = arena.allocate(ValueLayout.ADDRESS);
            //System.out.println(dbi.get(ValueLayout.JAVA_INT,0));
            mdbTxnBegin(MemorySegment.NULL,txn,0);
            mdbDbiOpen(txn.get(ValueLayout.ADDRESS,0),dbiName,dm,MaskFlag.DBI_CREATE.mask());
            //System.out.println(dbi.get(ValueLayout.JAVA_INT,0));
            mdbTxnCommit(txn.get(ValueLayout.ADDRESS,0));
            dbi = dm.get(ValueLayout.ADDRESS,0);
            //mdbDbiClose(dbi);
        }
    }

    public void putTest(String key,String value){

        try(Arena a = Arena.ofConfined()) {

            MemorySegment put = a.allocate(AddressLayout.ADDRESS);
            mdbTxnBegin(MemorySegment.NULL,put,0);
            MemorySegment txn = put.get(ValueLayout.ADDRESS,0);
            MemorySegment k = mdbVal(a,key);
            MemorySegment v = mdbVal(a,value);
            //mdbPut(txn,dbi,k,v,0);
            MemorySegment stat = dbiStat(a);
            mdbStat(txn,dbi,stat);
            System.out.println(stat.get(ValueLayout.JAVA_INT,0));
            System.out.println(stat.get(ValueLayout.JAVA_LONG,32));
            mdbTxnCommit(txn);

        }
    }

    private MemorySegment dbiStat(Arena a){
        StructLayout layout = MemoryLayout.structLayout(ValueLayout.JAVA_INT.withName("ms_psize"),ValueLayout.JAVA_INT.withName("ms_depth"),
                ValueLayout.JAVA_LONG.withName("ms_branch_pages"),ValueLayout.JAVA_LONG.withName("ms_leaf_pages"),ValueLayout.JAVA_LONG.withName("ms_overflow_pages"),ValueLayout.JAVA_LONG.withName("ms_entries"));
        MemorySegment memorySegment = a.allocate(layout);
        return memorySegment;
    }

    private MemorySegment envStat(Arena a){
        StructLayout layout = MemoryLayout.structLayout(ValueLayout.JAVA_INT.withName("ms_psize"),ValueLayout.JAVA_INT.withName("ms_depth"),
                ValueLayout.JAVA_LONG.withName("ms_branch_pages"),ValueLayout.JAVA_LONG.withName("ms_leaf_pages"),ValueLayout.JAVA_LONG.withName("ms_overflow_pages"),ValueLayout.JAVA_LONG.withName("ms_entries"));
        MemorySegment memorySegment = a.allocate(layout);
        return memorySegment;
    }

    private MemorySegment mdbVal(Arena a,String val){
        StructLayout struct = MemoryLayout.structLayout(ValueLayout.JAVA_LONG.withName("mv_size"),ValueLayout.ADDRESS.withName("mv_data"));
        MemorySegment pointer = a.allocate(struct);
        VarHandle vSize = struct.varHandle(MemoryLayout.PathElement.groupElement("mv_size"));
        vSize.set(pointer,0,val.length()+1);
        VarHandle vData = struct.varHandle(MemoryLayout.PathElement.groupElement("mv_data"));
        MemorySegment data = arena.allocateFrom(val, StandardCharsets.US_ASCII);
        vData.set(pointer,0,data);
        return pointer;//.get(ValueLayout.ADDRESS,0);
    }


    private void mdbEnvCreate(){
        try{
            MemorySegment mdbEnvCreate = lib.find("mdb_env_create").get();
            MemorySegment segment = arena.allocate(AddressLayout.ADDRESS);
            MethodHandle caller = linker.downcallHandle(mdbEnvCreate,FunctionDescriptor.of(ValueLayout.JAVA_INT,ValueLayout.ADDRESS));
            int ret = (int)caller.invoke(segment);
            if(ret != 0) throw new RuntimeException("code ["+ret+"]");
            env = segment.get(ValueLayout.ADDRESS,0);
        }catch (Throwable throwable){
            logger.error("mdb_env_create",throwable);
            throw new RuntimeException(throwable);
        }
    }

    private void mdbEnvOpen(){
        try{
            MemorySegment mdbEnvOpen = lib.find("mdb_env_open").get();
            MethodHandle caller = Linker.nativeLinker().downcallHandle(mdbEnvOpen,FunctionDescriptor.of(ValueLayout.JAVA_INT,ValueLayout.ADDRESS,ValueLayout.ADDRESS,ValueLayout.JAVA_INT,ValueLayout.JAVA_INT));
            MemorySegment dir = arena.allocateFrom(databasePath);
            int ret = (int)caller.invokeExact(env,dir,MaskFlag.ENV_NO_SYNC.mask(),MaskFlag.LINUX_MODE.mask());
            if(ret != 0) throw new RuntimeException("code ["+ret+"]");
        }catch (Throwable throwable){
            logger.error("mdb_env_open",throwable);
            throw new RuntimeException(throwable);
        }
    }

    private void mdbEnvSync(int force){
        try{
            MemorySegment mdbEnvClose = lib.find("mdb_env_sync").get();
            MethodHandle caller = linker.downcallHandle(mdbEnvClose,FunctionDescriptor.of(ValueLayout.JAVA_INT,ValueLayout.ADDRESS,ValueLayout.JAVA_INT));
            int ret = (int)caller.invokeExact(env,force);
            if(ret != 0) throw new RuntimeException("code ["+ret+"]");
        }catch (Throwable throwable){
            logger.error("mdb_env_sync",throwable);
            throw new RuntimeException(throwable);
        }
    }

    private void mdbEnvClose(){
        try{
            MemorySegment mdbEnvClose = lib.find("mdb_env_close").get();
            MethodHandle caller = linker.downcallHandle(mdbEnvClose,FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));
            caller.invoke(env);
        }catch (Throwable throwable){
            logger.error("mdb_env_close",throwable);
            throw new RuntimeException(throwable);
        }
    }

    private void mdbEnvSetMapSize(){
        try{
            MemorySegment mdbEnvSetMapSize = lib.find("mdb_env_set_mapsize").get();
            MethodHandle caller = Linker.nativeLinker().downcallHandle(mdbEnvSetMapSize,FunctionDescriptor.of(ValueLayout.JAVA_INT,ValueLayout.ADDRESS,ValueLayout.JAVA_LONG));
            int ret = (int)caller.invokeExact(env, EnvSetting.toBytesFromMb(10));
            if(ret != 0) throw new RuntimeException("code ["+ret+"]");
        }catch (Throwable throwable){
            logger.error("mdb_env_set_mapsize",throwable);
            throw new RuntimeException(throwable);
        }
    }

    private void mdbEnvSetMaxReaders(){
        try{
            MemorySegment mdbEnvSetMaxReaders = lib.find("mdb_env_set_maxreaders").get();
            MethodHandle caller = Linker.nativeLinker().downcallHandle(mdbEnvSetMaxReaders,FunctionDescriptor.of(ValueLayout.JAVA_INT,ValueLayout.ADDRESS,ValueLayout.JAVA_INT));
            int ret = (int)caller.invokeExact(env,100);
            if(ret != 0) throw new RuntimeException("code ["+ret+"]");
        }catch (Throwable throwable){
            logger.error("mdb_env_set_maxreaders",throwable);
            throw new RuntimeException(throwable);
        }
    }

    private void mdbEnvSetMaxDbs(){
        try{
            MemorySegment mdbEnvSetMaxDbs = lib.find("mdb_env_set_maxdbs").get();
            MethodHandle caller = Linker.nativeLinker().downcallHandle(mdbEnvSetMaxDbs,FunctionDescriptor.of(ValueLayout.JAVA_INT,ValueLayout.ADDRESS,ValueLayout.JAVA_INT));
            int ret = (int)caller.invokeExact(env,1024);
            if(ret != 0) throw new RuntimeException("code ["+ret+"]");
        }catch (Throwable throwable){
            logger.error("mdb_env_set_maxdbs",throwable);
            throw new RuntimeException(throwable);
        }
    }

    private void mdbTxnBegin(MemorySegment parentTxn,MemorySegment txn,int flags){
        try{
            MemorySegment mdbEnvSetMaxDbs = lib.find("mdb_txn_begin").get();
            MethodHandle caller = Linker.nativeLinker().downcallHandle(mdbEnvSetMaxDbs,FunctionDescriptor.of(ValueLayout.JAVA_INT,ValueLayout.ADDRESS,ValueLayout.ADDRESS,ValueLayout.JAVA_INT,ValueLayout.ADDRESS));
            int ret = (int)caller.invokeExact(env,parentTxn,flags,txn);
            if(ret != 0) throw new RuntimeException("code ["+ret+"]");
        }catch (Throwable throwable){
            logger.error("mdb_txn_begin",throwable);
            throw new RuntimeException(throwable);
        }
    }

    private void mdbTxnCommit(MemorySegment txn){
        try{
            MemorySegment mdbEnvSetMaxDbs = lib.find("mdb_txn_commit").get();
            MethodHandle caller = Linker.nativeLinker().downcallHandle(mdbEnvSetMaxDbs,FunctionDescriptor.of(ValueLayout.JAVA_INT,ValueLayout.ADDRESS));
            int ret = (int)caller.invokeExact(txn);
            if(ret != 0) throw new RuntimeException("code ["+ret+"]");
        }catch (Throwable throwable){
            logger.error("mdb_txn_commit",throwable);
            throw new RuntimeException(throwable);
        }
    }

    private void mdbTxnAbort(MemorySegment txn){
        try{
            MemorySegment mdbEnvSetMaxDbs = lib.find("mdb_txn_abort").get();
            MethodHandle caller = Linker.nativeLinker().downcallHandle(mdbEnvSetMaxDbs,FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));
            caller.invokeExact(txn);
        }catch (Throwable throwable){
            logger.error(" mdb_txn_abort",throwable);
            throw new RuntimeException(throwable);
        }
    }

    private void mdbTxnReset(MemorySegment txn){
        try{
            MemorySegment mdbEnvSetMaxDbs = lib.find("mdb_txn_reset").get();
            MethodHandle caller = Linker.nativeLinker().downcallHandle(mdbEnvSetMaxDbs,FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));
            caller.invokeExact(txn);
        }catch (Throwable throwable){
            logger.error(" mdb_txn_reset",throwable);
            throw new RuntimeException(throwable);
        }
    }

    private void mdbTxnRenew(MemorySegment txn){
        try{
            MemorySegment mdbTxnRenew = lib.find("mdb_txn_renew").get();
            MethodHandle caller = Linker.nativeLinker().downcallHandle(mdbTxnRenew,FunctionDescriptor.of(ValueLayout.JAVA_INT,ValueLayout.ADDRESS));
            int ret = (int)caller.invokeExact(txn);
            if(ret != 0) throw new RuntimeException("code ["+ret+"]");
        }catch (Throwable throwable){
            logger.error(" mdb_txn_renew",throwable);
            throw new RuntimeException(throwable);
        }
    }

    private long mdbTxnId(MemorySegment txn){
        try{
            MemorySegment mdbTxnRenew = lib.find("mdb_txn_id").get();
            MethodHandle caller = Linker.nativeLinker().downcallHandle(mdbTxnRenew,FunctionDescriptor.of(ValueLayout.JAVA_LONG,ValueLayout.ADDRESS));
            long ret = (long)caller.invokeExact(txn);
            return ret;
        }catch (Throwable throwable){
            logger.error(" mdb_txn_id",throwable);
            throw new RuntimeException(throwable);
        }
    }

    private void mdbDbiOpen(MemorySegment txn,MemorySegment dbName,MemorySegment dbi,int flags){
        try{
            MemorySegment mdbDbiOpen = lib.find("mdb_dbi_open").get();
            MethodHandle caller = Linker.nativeLinker().downcallHandle(mdbDbiOpen,FunctionDescriptor.of(ValueLayout.JAVA_INT,ValueLayout.ADDRESS,ValueLayout.ADDRESS,ValueLayout.JAVA_INT,ValueLayout.ADDRESS));
            int ret = (int)caller.invokeExact(txn,dbName,flags,dbi);
            if(ret != 0) throw new RuntimeException("code ["+ret+"]");
        }catch (Throwable throwable){
            logger.error("mdb_dbi_open",throwable);
            throw new RuntimeException(throwable);
        }
    }

    private void mdbDbiClose(MemorySegment dbi){
        try{
            MemorySegment mdbDbiClose = lib.find("mdb_dbi_close").get();
            MethodHandle caller = Linker.nativeLinker().downcallHandle(mdbDbiClose,FunctionDescriptor.ofVoid(ValueLayout.ADDRESS,ValueLayout.ADDRESS));
            caller.invokeExact(env,dbi);
        }catch (Throwable throwable){
            logger.error("mdb_dbi_close",throwable);
            throw new RuntimeException(throwable);
        }
    }

    private void mdbDrop(MemorySegment txn,MemorySegment dbi,int deleted){
        try{
            MemorySegment mdbDrop = lib.find("mdb_drop").get();
            MethodHandle caller = Linker.nativeLinker().downcallHandle(mdbDrop,FunctionDescriptor.of(ValueLayout.JAVA_INT,ValueLayout.ADDRESS,ValueLayout.ADDRESS,ValueLayout.JAVA_INT));
            int ret = (int)caller.invokeExact(txn,dbi,deleted);
            if(ret != 0) throw new RuntimeException("code ["+ret+"]");
        }catch (Throwable throwable){
            logger.error("mdb_drop",throwable);
            throw new RuntimeException(throwable);
        }
    }

    private void mdbPut(MemorySegment txn,MemorySegment dbi,MemorySegment key,MemorySegment value,int flags){
        try{
            MemorySegment mdbPut = lib.find("mdb_put").get();//-30781 BAD VALUE SIZE
            MethodHandle caller = Linker.nativeLinker().downcallHandle(mdbPut,FunctionDescriptor.of(ValueLayout.JAVA_INT,ValueLayout.ADDRESS,ValueLayout.ADDRESS,ValueLayout.ADDRESS,ValueLayout.ADDRESS,ValueLayout.JAVA_INT));
            int ret = (int)caller.invokeExact(txn,dbi,key,value,flags);
            if(ret != 0) throw new RuntimeException("code ["+ret+"]");
        }catch (Throwable throwable){
            logger.error(" mdb_put",throwable);
            mdbTxnAbort(txn);
            throw new RuntimeException(throwable);
        }
    }

    private void mdbGet(MemorySegment txn,MemorySegment dbi,MemorySegment key,MemorySegment value){
        try{
            MemorySegment mdbGet = lib.find("mdb_get").get();
            MethodHandle caller = Linker.nativeLinker().downcallHandle(mdbGet,FunctionDescriptor.of(ValueLayout.JAVA_INT,ValueLayout.ADDRESS,ValueLayout.ADDRESS,ValueLayout.ADDRESS,ValueLayout.ADDRESS));
            int ret = (int)caller.invokeExact(txn,dbi,key,value);
            if(ret != 0) throw new RuntimeException("code ["+ret+"]");
        }catch (Throwable throwable){
            logger.error(" mdb_get",throwable);
            throw new RuntimeException(throwable);
        }
    }

    private void mdbDel(MemorySegment txn,MemorySegment dbi,MemorySegment key,MemorySegment value){
        try{
            MemorySegment mdbDel = lib.find("mdb_del").get();
            MethodHandle caller = Linker.nativeLinker().downcallHandle(mdbDel,FunctionDescriptor.of(ValueLayout.JAVA_INT,ValueLayout.ADDRESS,ValueLayout.ADDRESS,ValueLayout.ADDRESS,ValueLayout.ADDRESS));
            int ret = (int)caller.invokeExact(txn,dbi,key,value);
            if(ret != 0) throw new RuntimeException("code ["+ret+"]");
        }catch (Throwable throwable){
            logger.error("mdb_del",throwable);
            throw new RuntimeException(throwable);
        }
    }

    private void mdbStat(MemorySegment txn,MemorySegment dbi,MemorySegment stat){
        try{
            MemorySegment mdbDel = lib.find("mdb_stat").get();
            MethodHandle caller = Linker.nativeLinker().downcallHandle(mdbDel,FunctionDescriptor.of(ValueLayout.JAVA_INT,ValueLayout.ADDRESS,ValueLayout.ADDRESS,ValueLayout.ADDRESS));
            int ret = (int)caller.invokeExact(txn,dbi,stat);
            if(ret != 0) throw new RuntimeException("code ["+ret+"]");
        }catch (Throwable throwable){
            logger.error("mdb_stat",throwable);
            throw new RuntimeException(throwable);
        }
    }

    private void mdbEnvStat(MemorySegment stat){
        try{
            MemorySegment mdbEnvStat = lib.find("mdb_env_stat").get();
            MethodHandle caller = Linker.nativeLinker().downcallHandle(mdbEnvStat,FunctionDescriptor.of(ValueLayout.JAVA_INT,ValueLayout.ADDRESS,ValueLayout.ADDRESS));
            int ret = (int)caller.invokeExact(env,stat);
            if(ret != 0) throw new RuntimeException("code ["+ret+"]");
        }catch (Throwable throwable){
            logger.error("mdb_env_stat",throwable);
            throw new RuntimeException(throwable);
        }
    }

    private void mdbEnvInfo(MemorySegment info){
        try{
            MemorySegment mdbEnvSetMaxDbs = lib.find("mdb_env_info").get();
            MethodHandle caller = Linker.nativeLinker().downcallHandle(mdbEnvSetMaxDbs,FunctionDescriptor.of(ValueLayout.JAVA_INT,ValueLayout.ADDRESS,ValueLayout.ADDRESS));
            int ret = (int)caller.invokeExact(env,info);
            if(ret != 0) throw new RuntimeException("code ["+ret+"]");
        }catch (Throwable throwable){
            logger.error("mdb_env_info",throwable);
            throw new RuntimeException(throwable);
        }
    }




}
