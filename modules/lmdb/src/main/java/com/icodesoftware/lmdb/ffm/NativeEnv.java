package com.icodesoftware.lmdb.ffm;

import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.lmdb.EnvSetting;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.Serviceable;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.util.concurrent.ConcurrentHashMap;

public class NativeEnv extends NativeStat implements Serviceable {

    private static TarantulaLogger logger = JDKLogger.getLogger(NativeEnv.class);

    private static String nativeLibPath = "/home/yinghu/lmdb.dll";
    private static String databasePath = "/home/yinghu/tst";
    SymbolLookup lib;
    Linker linker;

    private Arena arena = Arena.ofShared();
    private MemorySegment env;

    private final ConcurrentHashMap<String,NativeDbi> nativeDbs = new ConcurrentHashMap<>();

    @Override
    public void start() throws Exception {
        lib = SymbolLookup.libraryLookup(nativeLibPath,arena);
        linker = Linker.nativeLinker();
        mdbEnvCreate();
        mdbEnvSetMapSize();
        mdbEnvSetMaxReaders();
        mdbEnvSetMaxDbs();
        mdbEnvOpen();
        logger.warn("Native Env Opened");
    }

    @Override
    public void shutdown() throws Exception {
        mdbEnvSync(1);
        nativeDbs.forEach((k,v)->{
            try{v.shutdown();}catch (Exception ex){}
        });
        mdbEnvClose();
        arena.close();
        logger.warn("Native Env Closed");
    }

    public MemorySegment pointer(){
        return env;
    }

    public MemorySegment allocate(MemoryAllocator memoryAllocator){
        return memoryAllocator.onAllocate(arena);
    }

    public NativeDbi createDbi(String name){
        return nativeDbs.computeIfAbsent(name,key->{
            NativeDbi nativeDbi = new NativeDbi(this,name);
            try{nativeDbi.start();}catch (Exception ex){
                throw new RuntimeException(ex);
            }
            return nativeDbi;
        });
    }

    public NativeDbi createDbi(String name,String label){
        return nativeDbs.computeIfAbsent(name+"#"+label,key->{
            NativeDbi nativeDbi = new NativeDbi(this,name,label);
            try{nativeDbi.start();}catch (Exception ex){
                throw new RuntimeException(ex);
            }
            return nativeDbi;
        });
    }

    public NativeTxn read(Arena arena){
        MemorySegment pointer = arena.allocate(AddressLayout.ADDRESS);
        mdbTxnBegin(MemorySegment.NULL,pointer, TxnMask.TXN_RD_ONLY.mask());
        return new NativeTxn(this,pointer.get(ValueLayout.ADDRESS,0),true);
    }

    public NativeTxn write(Arena arena){
        MemorySegment pointer = arena.allocate(AddressLayout.ADDRESS);
        mdbTxnBegin(MemorySegment.NULL,pointer,0);
        return new NativeTxn(this,pointer.get(ValueLayout.ADDRESS,0),true);
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
            int ret = (int)caller.invokeExact(env,dir,EnvMask.ENV_NO_SYNC.mask(),EnvMask.ACCESS_MODE.mask());
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
