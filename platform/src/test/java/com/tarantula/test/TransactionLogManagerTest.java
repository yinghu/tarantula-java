package com.tarantula.test;

import com.icodesoftware.DataStore;
import com.icodesoftware.Distributable;
import com.icodesoftware.Recoverable;
import com.icodesoftware.Transaction;
import com.icodesoftware.lmdb.LocalMetadata;
import com.icodesoftware.service.Metadata;
import com.icodesoftware.util.BufferProxy;
import com.icodesoftware.util.SnowflakeKey;
import com.tarantula.platform.PresenceIndex;
import com.tarantula.platform.presence.PresencePortableRegistry;
import com.tarantula.platform.util.RecoverableQuery;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;


public class TransactionLogManagerTest extends DataStoreHook{


    @Test(groups = { "TransactionLogManager" })
    public void transactionLogManagerTest(){
        DataStore dataStore = serviceContext.dataStore(Distributable.DATA_SCOPE,"test");
        int sz = 100;
        for(int i=0;i<sz;i++){
            PresenceIndex presenceIndex = new PresenceIndex();
            presenceIndex.distributionId(serviceContext.distributionId());
            presenceIndex.onEdge(true);
            presenceIndex.label("presence_list");
            presenceIndex.ownerKey(SnowflakeKey.from(100));
            Assert.assertTrue(dataStore.createIfAbsent(presenceIndex,true));
        }
        PresenceIndex presenceIndex = new PresenceIndex();
        presenceIndex.label("presence_list");
        RecoverableQuery<PresenceIndex> query = RecoverableQuery.query(100,presenceIndex, PresencePortableRegistry.INS);
        Assert.assertTrue(dataStore.list(query).size()==sz);
        List<Transaction.History> logs = transactionLogManager.history(Distributable.DATA_SCOPE,serviceContext.node());
        logs.forEach(log->{
            List<Transaction.Log> pg = transactionLogManager.committed(Distributable.DATA_SCOPE,log.transactionId());
            transactionLogManager.onTransaction(pg);
            pg.forEach(p->{
                p.source("foo_test");
            });
            transactionLogManager.onTransaction(pg);
        });
        DataStore foo = serviceContext.dataStore(Distributable.DATA_SCOPE,"foo_test");

        int[] recovered = {0};
        foo.list(query).forEach(p->{
            recovered[0]++;
        });
        Assert.assertTrue(recovered[0]==sz);
        Metadata metadata = new LocalMetadata(Distributable.DATA_SCOPE,"test","presence_list");
        Recoverable.DataBuffer key = BufferProxy.buffer(8,true);
        key.writeLong(100).flip();
        recovered[0]=0;
        transactionLogManager.get(metadata,key,(k,v)->{
            if(k==null && v==null){
                return false;
            }
            recovered[0]++;
            return true;
        });
        Assert.assertTrue(recovered[0]==sz);
    }

}
