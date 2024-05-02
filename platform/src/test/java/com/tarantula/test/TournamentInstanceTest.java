package com.tarantula.test;


import com.icodesoftware.DataStore;
import com.icodesoftware.Tournament;
import com.icodesoftware.util.SnowflakeKey;
import com.tarantula.platform.tournament.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;


public class TournamentInstanceTest extends DataStoreHook{


    @Test(groups = { "TournamentInstance" })
    public void tournamentJoinTest() {
        DataStore dataStore = dataStoreProvider.createDataStore("test_tournament");
        DataStore dataStoreRaceBoard = dataStoreProvider.createDataStore("test_tournament_race_board");
        DataStore dataStoreEntry = dataStoreProvider.createDataStore("test_tournament_entry");
        TournamentInstance tournamentInstance = TournamentInstance.global(0,10);
        tournamentInstance.label(Tournament.GLOBAL_INSTANCE_LABEL);
        tournamentInstance.ownerKey(SnowflakeKey.from(1000));
        tournamentInstance.dataStore(dataStore);
        Assert.assertTrue(dataStore.create(tournamentInstance));
        tournamentInstance.entryDataStore = dataStoreEntry;
        tournamentInstance.raceBoardDataStore = dataStoreRaceBoard;
        tournamentInstance.load();

        long entryId = enterAndScore(tournamentInstance,100,10);
        Assert.assertTrue(entryId>0);

        List<TournamentEntry> entries = dataStoreEntry.list(new TournamentEntryQuery(tournamentInstance.distributionId()));
        Assert.assertEquals(entries.get(0).score(),10);
        Assert.assertEquals(entries.size(),1);


        Assert.assertEquals(dataStoreRaceBoard.list(new TournamentEntryQuery(tournamentInstance.distributionId())).size(),10);

        Tournament.RaceBoard raceBoard = tournamentInstance.raceBoard();
        Assert.assertEquals(raceBoard.list().size(),1);

        enterAndScore(tournamentInstance,200,20);

        Tournament.RaceBoard raceBoard2 = tournamentInstance.raceBoard();
        Assert.assertEquals(raceBoard2.list().size(),2);

        long systemId = 300;
        double score = 30;
        for(int i = 0;i<10000;i++){
            Assert.assertTrue(enterAndScore(tournamentInstance,systemId,score)>0);
            systemId += 100;
            score += 10;
        }

        Tournament.RaceBoard raceBoard10 = tournamentInstance.raceBoard();
        Assert.assertEquals(raceBoard10.list().size(),10);

        List<TournamentEntry> all = dataStoreEntry.list(new TournamentEntryQuery(tournamentInstance.distributionId()));
        Assert.assertEquals(all.size(),10002);
        long st = System.currentTimeMillis();
        Collections.sort(all,new TournamentEntryComparator());
        System.out.println("SORT : "+(System.currentTimeMillis()-st));

        Optional<TournamentEntry> opt = all.stream().filter((e)->e.systemId()==200).findFirst();
        Assert.assertNotNull(opt.get());
        Assert.assertTrue(all.stream().filter((e)->e.systemId()==10).findFirst().isEmpty());

    }

    private long enterAndScore(TournamentInstance tournamentInstance,long systemId,double score){
        long entryId = tournamentInstance.enterSegment(systemId,0);
        tournamentInstance.scoreSegment(entryId,systemId,0,score);
        return entryId;
    }

}
