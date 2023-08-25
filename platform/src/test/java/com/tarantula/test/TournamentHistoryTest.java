package com.tarantula.test;

import com.icodesoftware.DataStore;
import com.icodesoftware.service.DataStoreProvider;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.LongTypeKey;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.service.AccessKey;
import com.tarantula.platform.service.AccessKeyQuery;
import com.tarantula.platform.tournament.PlayerTournamentHistory;
import com.tarantula.platform.util.SystemUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.util.List;

public class TournamentHistoryTest {

    DataStoreProvider dataStoreProvider;
    ServiceContext serviceContext;
    @BeforeClass
    public void setUp() {
        DataStoreTestEvn.setUp();
        dataStoreProvider = DataStoreTestEvn.dataStoreProvider;
        serviceContext = DataStoreTestEvn.serviceContext;
    }


    //@Test(groups = { "DataStore" })
    public void playerTournamentHistoryTest() {
        DataStore dataStore = dataStoreProvider.create("test_tournament",serviceContext.node().partitionNumber());
        String systemId = "BDS/"+ SystemUtil.oid();
        PlayerTournamentHistory history = new PlayerTournamentHistory(10);
        history.distributionKey(systemId);
        dataStore.createIfAbsent(history,true);
        for(int i=0;i<12;i++){
            history.addKey("tx-"+i);
        }
        List<String> input = history.keySet();
        dataStore.update(history);
        PlayerTournamentHistory fromDataStore = new PlayerTournamentHistory(10);
        fromDataStore.distributionKey(systemId);
        dataStore.createIfAbsent(fromDataStore,true);
        List<String> output = fromDataStore.keySet();
        for(int i=0;i<10;i++){
            Assert.assertEquals(output.get(i),input.get(i));
        }
    }

}
