package com.icodesoftware.lmdb;

import com.icodesoftware.DataStore;
import com.icodesoftware.Recoverable;
import com.icodesoftware.service.DataStoreProvider;
import org.lmdbjava.Txn;

import java.nio.ByteBuffer;

public interface LocalLMDBProvider  extends DataStoreProvider {

     LocalEdgeDataStore createEdgeDB(int scope,String source,String label);
     LocalEdgeDataStore localEdgeDataStore(int scope, String source, String label, Txn<ByteBuffer> txn);
     default LocalDataStore partition(int scope,String name,Recoverable.DataBuffer key){ return null;}
}
