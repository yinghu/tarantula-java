package com.tarantula.test;


import com.icodesoftware.DataStore;
import com.icodesoftware.Distributable;
import com.icodesoftware.Transaction;
import com.icodesoftware.util.SnowflakeKey;
import com.tarantula.platform.tournament.TournamentEntry;
import com.tarantula.platform.tournament.TournamentInstance;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.concurrent.CountDownLatch;


public class TournamentScoreSyncTest extends DataStoreHook{


    @Test(groups = { "TournamentScoreSync" })
    public void scoreSyncTest() {
        Transaction transaction = dataStoreProvider.transaction(Distributable.DATA_SCOPE);
        long systemId = 100;
        long instanceId = 200;
        String storeName = "tournament_entry";
        TournamentEntry entry = new TournamentEntry(systemId,0,0);
        entry.ownerKey(SnowflakeKey.from(instanceId));
        DataStore dataStore = dataStoreProvider.createDataStore(storeName);
        Assert.assertTrue(dataStore.create(entry));
        TournamentInstance tournamentInstance = TournamentInstance.global(0,100);
        tournamentInstance.scoreSegmentSync(transaction,entry.distributionId(),systemId,10);
        TournamentEntry loaded = new TournamentEntry();
        loaded.distributionId(entry.distributionId());
        Assert.assertTrue(dataStore.load(loaded));
        Assert.assertEquals(loaded.score(),10);
        CountDownLatch waiting = new CountDownLatch(10);
        Exception[] exception = {null};
        for(int i=0;i<10;i++){
            new Thread(()->{
                try{
                    Transaction tx = dataStoreProvider.transaction(Distributable.DATA_SCOPE);
                    tournamentInstance.scoreSegmentSync(tx,entry.distributionId(),100,10);
                    waiting.countDown();
                }catch (Exception ex){
                    exception[0]=ex;
                    waiting.countDown();
                }
            }).start();
        }
        try{
            waiting.await();
        }catch (Exception ex){

        }
        Assert.assertNull(exception[0]);
        TournamentEntry finalScore = new TournamentEntry();
        finalScore.distributionId(entry.distributionId());
        Assert.assertTrue(dataStore.load(finalScore));
        Assert.assertEquals(finalScore.score(),110);

    }



}
