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
        PresenceIndex presenceIndex = new PresenceIndex();
        presenceIndex.distributionId(serviceContext.distributionId());
        presenceIndex.onEdge(true);
        presenceIndex.label("presence_list");
        presenceIndex.ownerKey(SnowflakeKey.from(100));
        Assert.assertTrue(dataStore.createIfAbsent(presenceIndex,true));
        List<TransactionResult> logs = transactionLogManager.pending(serviceContext.node().nodeId());
        System.out.println("SIZE : "+logs.size());
        logs.forEach(log->{
            System.out.println("TID : "+log.distributionId());
            List<TransactionLog> pg = transactionLogManager.committed(log.distributionId());
            pg.forEach(p->{
                p.source = "foo_test";
                //if(p.edgeLabel==null) {
                    //System.out.println("LBL : "+p.edgeLabel+" : "+p.value+" : "+BufferUtil.toLong(p.key));
                //}
                //else{
                    //System.out.println("LBL : "+p.edgeLabel+" : "+BufferUtil.toLong(p.edgeKey)+" : "+BufferUtil.toLong(p.key));
                //}
            });
            transactionLogManager.onTransaction(pg);
        });
        DataStore foo = serviceContext.dataStore(Distributable.DATA_SCOPE,"foo_test");
        Assert.assertTrue(foo.load(presenceIndex));
        RecoverableQuery<PresenceIndex> query = RecoverableQuery.query(100,presenceIndex, PresencePortableRegistry.INS);
        foo.list(query).forEach(p->{
            System.out.println("PP : "+p.distributionId()+" :: "+presenceIndex.distributionId());
        });
    }

}
