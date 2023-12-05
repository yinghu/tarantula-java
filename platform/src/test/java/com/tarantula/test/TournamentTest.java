package com.tarantula.test;

import com.icodesoftware.DataStore;

import com.icodesoftware.Tournament;
import com.icodesoftware.util.BufferUtil;
import com.icodesoftware.util.SnowflakeKey;

import com.tarantula.platform.tournament.TournamentScheduleStatus;
import com.tarantula.platform.tournament.TournamentScheduleStatusQuery;
import org.testng.Assert;
import org.testng.annotations.Test;


public class TournamentTest extends DataStoreHook{


    @Test(groups = { "Tournament" })
    public void tournamentScheduleStatusTest() {
        DataStore dataStore = dataStoreProvider.createDataStore("test_tarantula_tournament");
        TournamentScheduleStatus status = new TournamentScheduleStatus(1000);
        status.distributionId(5000);
        status.ownerKey(new SnowflakeKey(100));
        Assert.assertTrue(dataStore.createIfAbsent(status,false));
        Assert.assertFalse(dataStore.createIfAbsent(status,false));
        Assert.assertTrue(status.status == Tournament.Status.PENDING);
        status.status = Tournament.Status.STARTED;
        Assert.assertTrue(dataStore.update(status));
        Assert.assertEquals(1,dataStore.list(new TournamentScheduleStatusQuery(100)).size());
        TournamentScheduleStatus load = new TournamentScheduleStatus();
        load.distributionId(5000);
        Assert.assertTrue(dataStore.load(load));
        Assert.assertTrue(load.status == Tournament.Status.STARTED);
        Assert.assertTrue(load.tournamentId==1000);
        Assert.assertTrue(dataStore.delete(load));
        Assert.assertEquals(0,dataStore.list(new TournamentScheduleStatusQuery(100)).size());
        Assert.assertFalse(dataStore.load(load));
        Assert.assertEquals(1000,BufferUtil.toLong(status.toBinary()));
    }

    @Test(groups = { "Tournament" })
    public void tournamentScheduleTest() {

    }


}
