package com.tarantula.test;


import com.beust.ah.A;
import com.icodesoftware.DataStore;

import com.icodesoftware.Tournament;
import com.tarantula.platform.tournament.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class TournamentRaceBoardTest extends DataStoreHook{



    @Test(groups = { "TournamentRaceBoardSync" })
    public void tournamentRaceBoardSyncTest() {
        DataStore dataStore = dataStoreProvider.createDataStore("test_tournament_race_board");
        RaceBoardSync raceBoardSync = new RaceBoardSync(10,new TournamentEntryComparator());
        raceBoardSync.distributionId(100);
        raceBoardSync.dataStore(dataStore);
        raceBoardSync.load();
        Assert.assertEquals(raceBoardSync.size(),10);
        Assert.assertEquals(dataStore.list(new TournamentEntryQuery(100)).size(),10);
        double credit = 0;
        double score = 10;

        Assert.assertEquals(raceBoardSync.snapshot().size(),0);
        raceBoardSync.onBoard(new TournamentEntry(1,credit,score*1));
        Assert.assertNotNull(raceBoardSync.onBoard(1));
        Assert.assertEquals(raceBoardSync.snapshot().size(),1);
        TournamentRaceBoard raceBoard = new TournamentRaceBoard(raceBoardSync.snapshot());
        Assert.assertEquals(raceBoard.list().size(),1);
        byte[] payload = raceBoard.toBinary();
        TournamentRaceBoard duplicate = TournamentRaceBoard.from(payload);
        Assert.assertEquals(duplicate.list().size(),1);
        for(int i=2;i<10;i++){
            TournamentEntry entry = new TournamentEntry(i,credit,i+score);
            raceBoardSync.onBoard(entry);
        }
        Assert.assertNotNull(raceBoardSync.onBoard(9));

        for(int i=10;i<20;i++){
            TournamentEntry entry = new TournamentEntry(i,credit,i+score);
            raceBoardSync.onBoard(entry);
        }

        Assert.assertNull(raceBoardSync.onBoard(1));
        Assert.assertNull(raceBoardSync.onBoard(9));
        Assert.assertNotNull(raceBoardSync.onBoard(10));
        Assert.assertNotNull(raceBoardSync.onBoard(19));
        List<Tournament.Entry> ranked = raceBoardSync.snapshot();

        Assert.assertEquals(ranked.size(),10);
        Assert.assertEquals(ranked.get(0).rank(),1);
        Assert.assertEquals(ranked.get(9).rank(),10);

        TournamentRaceBoard raceBoard10 = new TournamentRaceBoard(raceBoardSync.snapshot());
        Assert.assertEquals(raceBoard10.list().size(),10);
        TournamentRaceBoard duplicate10 = TournamentRaceBoard.from(raceBoard10.toBinary());
        Assert.assertEquals(duplicate10.list().size(),10);


    }

}
