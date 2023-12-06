package com.tarantula.test;

import com.icodesoftware.DataStore;

import com.icodesoftware.Tournament;
import com.icodesoftware.util.BufferUtil;
import com.icodesoftware.util.SnowflakeKey;

import com.tarantula.platform.tournament.TournamentJoin;
import com.tarantula.platform.tournament.TournamentJoinQuery;
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
    public void tournamentJoinTest() {
        long tournamentId = 100;
        long systemId1 = 1000;
        long systemId2 = 2000;
        DataStore dataStore = dataStoreProvider.createDataStore("test_tarantula_tournament_join");
        for(int i=0;i<5;i++){
            TournamentJoin join = new TournamentJoin(i,tournamentId);
            join.ownerKey(SnowflakeKey.from(systemId1));
            dataStore.create(join);
            join.ownerKey(SnowflakeKey.from(tournamentId));
            dataStore.createEdge(join,TournamentJoin.TOURNAMENT_JOIN_LABEL);
        }
        for(int i=0;i<5;i++){
            TournamentJoin join = new TournamentJoin(i,tournamentId);
            join.ownerKey(SnowflakeKey.from(systemId2));
            dataStore.create(join);
            join.ownerKey(SnowflakeKey.from(tournamentId));
            dataStore.createEdge(join,TournamentJoin.TOURNAMENT_JOIN_LABEL);
        }
        Assert.assertEquals(10,dataStore.list(new TournamentJoinQuery(SnowflakeKey.from(tournamentId),TournamentJoin.TOURNAMENT_JOIN_LABEL)).size());
        Assert.assertEquals(5,dataStore.list(new TournamentJoinQuery(SnowflakeKey.from(systemId1),TournamentJoin.PLAYER_JOIN_LABEL)).size());
        Assert.assertEquals(5,dataStore.list(new TournamentJoinQuery(SnowflakeKey.from(systemId2),TournamentJoin.PLAYER_JOIN_LABEL)).size());
        dataStore.deleteEdge(SnowflakeKey.from(tournamentId),TournamentJoin.TOURNAMENT_JOIN_LABEL);
        Assert.assertEquals(0,dataStore.list(new TournamentJoinQuery(SnowflakeKey.from(tournamentId),TournamentJoin.TOURNAMENT_JOIN_LABEL)).size());

    }


}
