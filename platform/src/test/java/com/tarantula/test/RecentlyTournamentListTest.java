package com.tarantula.test;

import com.icodesoftware.DataStore;
import com.icodesoftware.Statistics;
import com.icodesoftware.protocol.statistics.UserStatistics;
import com.tarantula.platform.tournament.PlatformTournamentServiceProvider;
import com.tarantula.platform.tournament.RecentlyTournamentList;
import com.tarantula.platform.tournament.TournamentManager;
import org.testng.Assert;
import org.testng.annotations.Test;


public class RecentlyTournamentListTest extends DataStoreHook{


    @Test(groups = { "RecentlyTournamentList" })
    public void recentlyTournamentListTest(){
        DataStore ds = dataStoreProvider.createDataStore("test-recently-tournament-list");
        RecentlyTournamentList recentlyTournamentList = RecentlyTournamentList.lookup(ds,100,"hero",4);
        Long[] ids = recentlyTournamentList.pop();
        Assert.assertEquals(ids.length,4);
        for(Long id : ids){
            Assert.assertNull(id);
        }
        RecentlyTournamentList loaded = RecentlyTournamentList.lookup(ds,100,"hero",4);
        Long[] lds = loaded.pop();
        Assert.assertEquals(lds.length,4);
        for(Long id : lds){
            Assert.assertEquals(id,0);
        }

        RecentlyTournamentList add300 = addTournament(300,ds);
        Long[] ads = add300.pop();
        Assert.assertEquals(ads.length,4);
        Assert.assertEquals(ads.length,4);
        Assert.assertEquals(ads[0],0);
        Assert.assertEquals(ads[1],0);
        Assert.assertEquals(ads[2],0);
        Assert.assertEquals(ads[3],300);

        RecentlyTournamentList add400 = addTournament(400,ds);
        Long[] ads2 = add400.pop();
        Assert.assertEquals(ads2.length,4);
        Assert.assertEquals(ads2[0],0);
        Assert.assertEquals(ads2[1],0);
        Assert.assertEquals(ads2[2],300);
        Assert.assertEquals(ads2[3],400);

        RecentlyTournamentList add500 = addTournament(500,ds);
        Long[] ads3 = add500.pop();
        Assert.assertEquals(ads3.length,4);
        Assert.assertEquals(ads3[0],0);
        Assert.assertEquals(ads3[1],300);
        Assert.assertEquals(ads3[2],400);
        Assert.assertEquals(ads3[3],500);

        RecentlyTournamentList add600 = addTournament(600,ds);
        Long[] ads4 = add600.pop();
        Assert.assertEquals(ads4.length,4);
        Assert.assertEquals(ads4[0],300);
        Assert.assertEquals(ads4[1],400);
        Assert.assertEquals(ads4[2],500);
        Assert.assertEquals(ads4[3],600);

        RecentlyTournamentList add700 = addTournament(700,ds);
        Long[] ads5 = add700.pop();
        Assert.assertEquals(ads5.length,4);
        Assert.assertEquals(ads5[0],400);
        Assert.assertEquals(ads5[1],500);
        Assert.assertEquals(ads5[2],600);
        Assert.assertEquals(ads5[3],700);


    }

    private RecentlyTournamentList addTournament(long tournamentId,DataStore ds){
        RecentlyTournamentList recentlyTournamentList = RecentlyTournamentList.lookup(ds,100,"hero",4);
        TournamentManager tournamentManager= new TournamentManager();
        tournamentManager.distributionId(tournamentId);
        recentlyTournamentList.push(tournamentManager);
        recentlyTournamentList.update();
        return recentlyTournamentList;
    }

}
