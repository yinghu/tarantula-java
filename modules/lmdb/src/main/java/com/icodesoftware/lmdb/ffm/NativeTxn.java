package com.icodesoftware.lmdb.ffm;

import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

public class NativeTxn implements AutoCloseable{

    private static final TarantulaLogger logger = JDKLogger.getLogger(NativeTxn.class);

    //private final NativeTxn parent;

    private final NativeEnv env;
    private final MemorySegment txn;
    private final boolean readOnly;

    public NativeTxn(final NativeEnv env,final MemorySegment txn,final boolean readOnly){
        this.env = env;
        this.txn = txn;
        this.readOnly = readOnly;
    }

    public MemorySegment pointer(){
        return txn;
    }

    public boolean readOnly(){
        return readOnly;
    }

    public void commit(){
        mdbTxnCommit();
    }

    public void abort(){
        mdbTxnAbort();
    }

    public void reset(){
        mdbTxnReset();
    }

    public void renew(){
        mdbTxnRenew();
    }

    public long transactionId(){
        return mdbTxnId();
    }

    private void mdbTxnCommit(){
        try{
            MemorySegment mdbEnvSetMaxDbs = env.lib.find("mdb_txn_commit").get();
            MethodHandle caller = Linker.nativeLinker().downcallHandle(mdbEnvSetMaxDbs, FunctionDescriptor.of(ValueLayout.JAVA_INT,ValueLayout.ADDRESS));
            int ret = (int)caller.invokeExact(txn);
            if(ret != 0) throw new RuntimeException("code ["+ret+"]");
        }catch (Throwable throwable){
            logger.error("mdb_txn_commit",throwable);
            throw new RuntimeException(throwable);
        }
    }

    public void mdbTxnAbort(){
        try{
            MemorySegment mdbEnvSetMaxDbs = env.lib.find("mdb_txn_abort").get();
            MethodHandle caller = Linker.nativeLinker().downcallHandle(mdbEnvSetMaxDbs,FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));
            caller.invokeExact(txn);
        }catch (Throwable throwable){
            logger.error(" mdb_txn_abort",throwable);
            throw new RuntimeException(throwable);
        }
    }

    private long mdbTxnId(){
        try{
            MemorySegment mdbTxnRenew = env.lib.find("mdb_txn_id").get();
            MethodHandle caller = Linker.nativeLinker().downcallHandle(mdbTxnRenew,FunctionDescriptor.of(ValueLayout.JAVA_LONG,ValueLayout.ADDRESS));
            long ret = (long)caller.invokeExact(txn);
            return ret;
        }catch (Throwable throwable){
            logger.error(" mdb_txn_id",throwable);
            throw new RuntimeException(throwable);
        }
    }

    private void mdbTxnReset(){
        try{
            MemorySegment mdbEnvSetMaxDbs = env.lib.find("mdb_txn_reset").get();
            MethodHandle caller = Linker.nativeLinker().downcallHandle(mdbEnvSetMaxDbs,FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));
            caller.invokeExact(txn);
        }catch (Throwable throwable){
            logger.error(" mdb_txn_reset",throwable);
            throw new RuntimeException(throwable);
        }
    }

    private void mdbTxnRenew(){
        try{
            MemorySegment mdbTxnRenew = env.lib.find("mdb_txn_renew").get();
            MethodHandle caller = Linker.nativeLinker().downcallHandle(mdbTxnRenew,FunctionDescriptor.of(ValueLayout.JAVA_INT,ValueLayout.ADDRESS));
            int ret = (int)caller.invokeExact(txn);
            if(ret != 0) throw new RuntimeException("code ["+ret+"]");
        }catch (Throwable throwable){
            logger.error(" mdb_txn_renew",throwable);
            throw new RuntimeException(throwable);
        }
    }

    @Override
    public void close(){
        logger.warn("TXN CLOSE");
    }
}
