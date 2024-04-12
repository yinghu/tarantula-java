package com.tarantula.test;


import com.icodesoftware.DataStore;
import com.tarantula.platform.tournament.TournamentRaceBoard;
import org.testng.Assert;
import org.testng.annotations.Test;


public class TournamentRaceBoardTest extends DataStoreHook{


    @Test(groups = { "TournamentRaceBoard" })
    public void tournamentRaceBoardTTest() {
        DataStore dataStore = dataStoreProvider.createDataStore("test_tournament");
        TournamentRaceBoard tournamentRaceBoard = new TournamentRaceBoard(10);
        Assert.assertEquals(tournamentRaceBoard.size(),10);
        //PresenceIndex presenceIndex = new PresenceIndex();
        //presenceIndex.distributionId(presenceId);
        //Assert.assertTrue(dataStore.createIfAbsent(presenceIndex,true));
        //SessionIndex t1 = new SessionIndex();
        //t1.ownerKey(presenceIndex.key());
        //Assert.assertTrue(dataStore.create(t1));
        //SessionIndex t2 = new SessionIndex();
        //t2.ownerKey(presenceIndex.key());
        //Assert.assertTrue(dataStore.create(t2));
        //List<SessionIndex> tlist = dataStore.list(new OnSessionQuery(presenceIndex.key()));
        //Assert.assertEquals(tlist.size(),2);
    }

}
