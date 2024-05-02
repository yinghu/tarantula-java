package com.tarantula.test;


import com.icodesoftware.DataStore;

import com.icodesoftware.util.SnowflakeKey;
import com.tarantula.game.SimpleStub;
import com.tarantula.platform.tournament.TournamentJoin;
import com.tarantula.platform.tournament.TournamentJoinQuery;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;


public class TournamentJoinTest extends DataStoreHook{


    @Test(groups = { "TournamentJoin" })
    public void tournamentJoinTest() {
        DataStore dataStore = dataStoreProvider.createDataStore("test_tournament_join");
        SimpleStub stub = new SimpleStub();
        stub.distributionId(100);
        stub.stub(200);
        long scheduleId = 1000;
        long tournamentId = 2000;
        long instanceId = 3000;
        long entryId = 4000;
        TournamentJoin tournamentJoin = TournamentJoin.lookup(dataStore,stub,scheduleId);
        Assert.assertEquals(tournamentJoin.scheduleId,scheduleId);
        Assert.assertEquals(tournamentJoin.stub(),stub.distributionId());
        Assert.assertEquals(tournamentJoin.closed,true);
        Assert.assertEquals(tournamentJoin.tournamentId,0);
        Assert.assertEquals(tournamentJoin.instanceId,0);

        TournamentJoin loaded = TournamentJoin.lookup(dataStore,stub,scheduleId);
        Assert.assertEquals(tournamentJoin.distributionId(),loaded.distributionId());
        Assert.assertEquals(tournamentJoin.scheduleId,loaded.scheduleId);
        Assert.assertEquals(tournamentJoin.stub(),loaded.stub());
        Assert.assertEquals(tournamentJoin.closed,loaded.closed);
        Assert.assertEquals(tournamentJoin.tournamentId,loaded.tournamentId);
        Assert.assertEquals(tournamentJoin.instanceId,loaded.instanceId);

        tournamentJoin.onTournament(tournamentId,instanceId,entryId);
        Assert.assertEquals(tournamentJoin.distributionId(),loaded.distributionId());
        Assert.assertEquals(tournamentJoin.scheduleId,loaded.scheduleId);
        Assert.assertEquals(tournamentJoin.stub(),loaded.stub());
        Assert.assertEquals(tournamentJoin.closed,false);
        Assert.assertEquals(tournamentJoin.tournamentId,tournamentId);
        Assert.assertEquals(tournamentJoin.instanceId,instanceId);

        List<TournamentJoin> joined = dataStore.list(new TournamentJoinQuery(SnowflakeKey.from(tournamentId),TournamentJoin.TOURNAMENT_JOIN_LABEL));
        Assert.assertEquals(joined.size(),1);
        TournamentJoin join = joined.get(0);
        Assert.assertEquals(tournamentJoin.distributionId(),join.distributionId());
        Assert.assertEquals(tournamentJoin.scheduleId,join.scheduleId);
        Assert.assertEquals(tournamentJoin.stub(),join.stub());
        Assert.assertEquals(tournamentJoin.closed,false);
        Assert.assertEquals(tournamentJoin.tournamentId,join.tournamentId);
        Assert.assertEquals(tournamentJoin.instanceId,join.instanceId);



    }

}
