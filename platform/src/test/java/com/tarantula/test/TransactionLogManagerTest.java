package com.tarantula.test;

import com.icodesoftware.DataStore;
import com.icodesoftware.Distributable;
import com.icodesoftware.lmdb.TransactionLog;
import com.icodesoftware.lmdb.TransactionResult;
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
        for(int i=0;i<10;i++){
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
        Assert.assertTrue(dataStore.list(query).size()==10);
        List<TransactionResult> logs = transactionLogManager.pending(Distributable.DATA_SCOPE,serviceContext.node().nodeId());
        logs.forEach(log->{
            List<TransactionLog> pg = transactionLogManager.committed(Distributable.DATA_SCOPE,log.distributionId());
            pg.forEach(p->{
                p.source = "foo_test";
            });
            transactionLogManager.onTransaction(pg);
        });
        DataStore foo = serviceContext.dataStore(Distributable.DATA_SCOPE,"foo_test");

        int[] recovered = {0};
        foo.list(query).forEach(p->{
            recovered[0]++;
        });
        System.out.println(recovered[0]);
        Assert.assertTrue(recovered[0]>0);
    }

}
