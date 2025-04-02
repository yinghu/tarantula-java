package com.icodesoftware.lmdb.ffm;

import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.lmdb.EnvSetting;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.Serviceable;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;


public class NativeEnv implements Serviceable {

    private static TarantulaLogger logger = JDKLogger.getLogger(NativeEnv.class);

    private static String nativeLibPath = "/home/yinghu/lmdb.dll";
    private SymbolLookup lib;
    private Linker linker;

    private Arena arena = Arena.ofShared();
    private MemorySegment evn;

    @Override
    public void start() throws Exception {
        lib = SymbolLookup.libraryLookup(nativeLibPath,arena);
        linker = Linker.nativeLinker();
        mdbEnvCreate();
        mdbEnvSetMapSize();
        mdbEnvSetMaxReaders();
        mdbEnvSetMaxDbs();
        mdbEnvOpen();
    }

    @Override
    public void shutdown() throws Exception {
        mdbEnvClose();
        arena.close();
    }

    private void mdbEnvCreate(){
        try{
            MemorySegment mdbEnvCreate = lib.find("mdb_env_create").get();
            MemorySegment segment = arena.allocate(AddressLayout.ADDRESS);
            MethodHandle caller = linker.downcallHandle(mdbEnvCreate,FunctionDescriptor.of(ValueLayout.JAVA_INT,ValueLayout.ADDRESS));
            int ret = (int)caller.invoke(segment);
            if(ret != 0) throw new RuntimeException("code ["+ret+"]");
            evn = segment.get(ValueLayout.ADDRESS,0);
        }catch (Throwable throwable){
            logger.error("mdb_env_create",throwable);
            throw new RuntimeException(throwable);
        }
    }

    private void mdbEnvOpen(){
        try{
            MemorySegment mdbEnvOpen = lib.find("mdb_env_open").get();
            MethodHandle caller = Linker.nativeLinker().downcallHandle(mdbEnvOpen,FunctionDescriptor.of(ValueLayout.JAVA_INT,ValueLayout.ADDRESS,ValueLayout.ADDRESS,ValueLayout.JAVA_INT,ValueLayout.JAVA_INT));
            MemorySegment dir = arena.allocateFrom("/home/yinghu/tst");
            int ret = (int)caller.invokeExact(evn,dir,MaskFlag.ENV_NO_SYNC.mask(),MaskFlag.LINUX_MODE.mask());
            if(ret != 0) throw new RuntimeException("code ["+ret+"]");
        }catch (Throwable throwable){
            logger.error("mdb_env_open",throwable);
            throw new RuntimeException(throwable);
        }
    }

    private void mdbEnvClose(){
        try{
            MemorySegment mdbEnvClose = lib.find("mdb_env_close").get();
            MethodHandle caller = linker.downcallHandle(mdbEnvClose,FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));
            caller.invoke(evn);
        }catch (Throwable throwable){
            logger.error("mdb_env_close",throwable);
            throw new RuntimeException(throwable);
        }
    }

    private void mdbEnvSetMapSize(){
        try{
            MemorySegment mdbEnvSetMapSize = lib.find("mdb_env_set_mapsize").get();
            MethodHandle caller = Linker.nativeLinker().downcallHandle(mdbEnvSetMapSize,FunctionDescriptor.of(ValueLayout.JAVA_INT,ValueLayout.ADDRESS,ValueLayout.JAVA_LONG));
            int ret = (int)caller.invokeExact(evn, EnvSetting.toBytesFromMb(10));
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
            int ret = (int)caller.invokeExact(evn,100);
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
            int ret = (int)caller.invokeExact(evn,1024);
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
            int ret = (int)caller.invokeExact(evn,parentTxn,flags,txn);
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
            MemorySegment mdbEnvSetMaxDbs = lib.find(" mdb_txn_abort").get();
            MethodHandle caller = Linker.nativeLinker().downcallHandle(mdbEnvSetMaxDbs,FunctionDescriptor.of(ValueLayout.JAVA_INT,ValueLayout.ADDRESS));
            int ret = (int)caller.invokeExact(txn);
            if(ret != 0) throw new RuntimeException("code ["+ret+"]");
        }catch (Throwable throwable){
            logger.error(" mdb_txn_abort",throwable);
            throw new RuntimeException(throwable);
        }
    }

    private void mdbTxnReset(MemorySegment txn){
        try{
            MemorySegment mdbEnvSetMaxDbs = lib.find(" mdb_txn_reset").get();
            MethodHandle caller = Linker.nativeLinker().downcallHandle(mdbEnvSetMaxDbs,FunctionDescriptor.of(ValueLayout.JAVA_INT,ValueLayout.ADDRESS));
            int ret = (int)caller.invokeExact(txn);
            if(ret != 0) throw new RuntimeException("code ["+ret+"]");
        }catch (Throwable throwable){
            logger.error(" mdb_txn_reset",throwable);
            throw new RuntimeException(throwable);
        }
    }

    private void mdbTxnRenew(MemorySegment txn){
        try{
            MemorySegment mdbTxnRenew = lib.find(" mdb_txn_renew").get();
            MethodHandle caller = Linker.nativeLinker().downcallHandle(mdbTxnRenew,FunctionDescriptor.of(ValueLayout.JAVA_INT,ValueLayout.ADDRESS));
            int ret = (int)caller.invokeExact(txn);
            if(ret != 0) throw new RuntimeException("code ["+ret+"]");
        }catch (Throwable throwable){
            logger.error(" mdb_txn_renew",throwable);
            throw new RuntimeException(throwable);
        }
    }

    private void mdbDbiOpen(MemorySegment txn,MemorySegment dbName,MemorySegment dbi,int flags){
        try{
            MemorySegment mdbDbiOpen = lib.find(" mdb_dbi_open").get();
            MethodHandle caller = Linker.nativeLinker().downcallHandle(mdbDbiOpen,FunctionDescriptor.of(ValueLayout.JAVA_INT,ValueLayout.ADDRESS,ValueLayout.ADDRESS,ValueLayout.JAVA_INT,ValueLayout.ADDRESS));
            int ret = (int)caller.invokeExact(txn,dbName,flags,dbi);
            if(ret != 0) throw new RuntimeException("code ["+ret+"]");
        }catch (Throwable throwable){
            logger.error(" mdb_dbi_open",throwable);
            throw new RuntimeException(throwable);
        }
    }

    private void mdbDbiClose(MemorySegment dbi){
        try{
            MemorySegment mdbDbiClose = lib.find(" mdb_dbi_close").get();
            MethodHandle caller = Linker.nativeLinker().downcallHandle(mdbDbiClose,FunctionDescriptor.ofVoid(ValueLayout.ADDRESS,ValueLayout.ADDRESS));
            int ret = (int)caller.invokeExact(evn,dbi);
            if(ret != 0) throw new RuntimeException("code ["+ret+"]");
        }catch (Throwable throwable){
            logger.error(" mdb_dbi_close",throwable);
            throw new RuntimeException(throwable);
        }
    }

    private void mdbDrop(MemorySegment txn,MemorySegment dbi,int deleted){
        try{
            MemorySegment mdbDrop = lib.find(" mdb_drop").get();
            MethodHandle caller = Linker.nativeLinker().downcallHandle(mdbDrop,FunctionDescriptor.ofVoid(ValueLayout.ADDRESS,ValueLayout.ADDRESS,ValueLayout.JAVA_INT));
            int ret = (int)caller.invokeExact(txn,dbi,deleted);
            if(ret != 0) throw new RuntimeException("code ["+ret+"]");
        }catch (Throwable throwable){
            logger.error(" mdb_drop",throwable);
            throw new RuntimeException(throwable);
        }
    }



}
