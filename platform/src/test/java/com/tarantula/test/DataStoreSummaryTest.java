package com.tarantula.test;

import com.icodesoftware.AccessIndex;
import com.icodesoftware.DataStore;
import com.icodesoftware.Distributable;
import com.icodesoftware.Transaction;
import com.icodesoftware.service.AccessIndexService;
import com.icodesoftware.service.DataStoreSummary;
import com.tarantula.platform.AccessIndexTrack;
import com.tarantula.platform.service.persistence.DataStoreViewer;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DataStoreSummaryTest extends DataStoreHook{


    @Test(groups = { "AccessIndex" })
    public void accessIndexTest() {
        DataStore dataStore = dataStoreProvider.createAccessIndexDataStore(AccessIndexService.STORE_NAME+"_x");
        String access = "access100";
        AccessIndexTrack accessIndexTrack = new AccessIndexTrack(access, AccessIndex.USER_INDEX,serviceContext.distributionId());
        Assert.assertTrue(dataStore.createIfAbsent(accessIndexTrack,false));
        DataStoreSummary dataStoreSummary = new DataStoreViewer();
        dataStore.backup().view(dataStoreSummary);
        Assert.assertEquals(dataStoreSummary.count(),1);
        Transaction transaction = dataStoreProvider.transaction(Distributable.INTEGRATION_SCOPE);
        transaction.execute(ctx->{
            DataStore ds = ctx.onDataStore(AccessIndexService.STORE_NAME+"_x");
            DataStoreSummary summary = new DataStoreViewer();
            ds.backup().view(summary);
            Assert.assertEquals(dataStoreSummary.count(),summary.count());
            return false;
        });
        transaction.close();
    }

}
