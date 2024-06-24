package com.tarantula.test;

import com.icodesoftware.DataStore;

import com.tarantula.platform.tournament.RecentlyTournamentList;
import com.tarantula.platform.tournament.TournamentManager;
import org.testng.Assert;
import org.testng.annotations.Test;


public class RecentlyTournamentListTest extends DataStoreHook{


    @Test(groups = { "RecentlyTournamentList" })
    public void recentlyTournamentListTest(){
        DataStore ds = dataStoreProvider.createDataStore("test-recently-tournament-list");
        int indexSize = 9;
        RecentlyTournamentList recentlyTournamentList = RecentlyTournamentList.lookup(ds,100,"hero",indexSize);
        Long[] ids = recentlyTournamentList.pop();
        Assert.assertEquals(ids.length,indexSize);
        for(Long id : ids){
            Assert.assertNull(id);
        }
        RecentlyTournamentList loaded = RecentlyTournamentList.lookup(ds,100,"hero",indexSize);
        Long[] lds = loaded.pop();
        Assert.assertEquals(lds.length,indexSize);
        for(Long id : lds){
            Assert.assertEquals(id,0);
        }

        RecentlyTournamentList add300 = addTournament(300,ds,indexSize);
        Long[] ads = add300.pop();
        Assert.assertEquals(ads.length,indexSize);
        Assert.assertEquals(ads.length,indexSize);
        Assert.assertEquals(ads[0],0);
        Assert.assertEquals(ads[1],0);
        Assert.assertEquals(ads[2],0);
        Assert.assertEquals(ads[3],0);
        Assert.assertEquals(ads[4],0);
        Assert.assertEquals(ads[5],0);
        Assert.assertEquals(ads[6],0);
        Assert.assertEquals(ads[7],0);
        Assert.assertEquals(ads[8],300);

        RecentlyTournamentList add400 = addTournament(400,ds,indexSize);
        Long[] ads2 = add400.pop();
        Assert.assertEquals(ads2.length,indexSize);
        Assert.assertEquals(ads2[0],0);
        Assert.assertEquals(ads2[1],0);
        Assert.assertEquals(ads2[2],0);
        Assert.assertEquals(ads2[3],0);
        Assert.assertEquals(ads2[4],0);
        Assert.assertEquals(ads2[5],0);
        Assert.assertEquals(ads2[6],0);
        Assert.assertEquals(ads2[7],300);
        Assert.assertEquals(ads2[8],400);

        RecentlyTournamentList add500 = addTournament(500,ds,indexSize);
        Long[] ads3 = add500.pop();
        Assert.assertEquals(ads3.length,indexSize);
        Assert.assertEquals(ads3[0],0);
        Assert.assertEquals(ads3[1],0);
        Assert.assertEquals(ads3[2],0);
        Assert.assertEquals(ads3[3],0);
        Assert.assertEquals(ads3[4],0);
        Assert.assertEquals(ads3[5],0);
        Assert.assertEquals(ads3[6],300);
        Assert.assertEquals(ads3[7],400);
        Assert.assertEquals(ads3[8],500);

        RecentlyTournamentList add600 = addTournament(600,ds,indexSize);
        Long[] ads4 = add600.pop();
        Assert.assertEquals(ads4.length,indexSize);
        Assert.assertEquals(ads4[0],0);
        Assert.assertEquals(ads4[1],0);
        Assert.assertEquals(ads4[2],0);
        Assert.assertEquals(ads4[3],0);
        Assert.assertEquals(ads4[4],0);
        Assert.assertEquals(ads4[5],300);
        Assert.assertEquals(ads4[6],400);
        Assert.assertEquals(ads4[7],500);
        Assert.assertEquals(ads4[8],600);

        RecentlyTournamentList add700 = addTournament(700,ds,indexSize);
        Long[] ads5 = add700.pop();
        Assert.assertEquals(ads5.length,indexSize);
        Assert.assertEquals(ads5[0],0);
        Assert.assertEquals(ads5[1],0);
        Assert.assertEquals(ads5[2],0);
        Assert.assertEquals(ads5[3],0);
        Assert.assertEquals(ads5[4],300);
        Assert.assertEquals(ads5[5],400);
        Assert.assertEquals(ads5[6],500);
        Assert.assertEquals(ads5[7],600);
        Assert.assertEquals(ads5[8],700);

        addTournament(800,ds,indexSize);
        addTournament(900,ds,indexSize);
        addTournament(1000,ds,indexSize);
        RecentlyTournamentList full = addTournament(1100,ds,indexSize);

        Long[] fullAdds = full.pop();
        Assert.assertEquals(fullAdds.length,indexSize);
        Assert.assertEquals(fullAdds[0],300);
        Assert.assertEquals(fullAdds[1],400);
        Assert.assertEquals(fullAdds[2],500);
        Assert.assertEquals(fullAdds[3],600);
        Assert.assertEquals(fullAdds[4],700);
        Assert.assertEquals(fullAdds[5],800);
        Assert.assertEquals(fullAdds[6],900);
        Assert.assertEquals(fullAdds[7],1000);
        Assert.assertEquals(fullAdds[8],1100);

        RecentlyTournamentList pop1 = addTournament(1200,ds,indexSize);

        Long[] pop = pop1.pop();
        Assert.assertEquals(pop.length,indexSize);
        Assert.assertEquals(pop[0],400);
        Assert.assertEquals(pop[1],500);
        Assert.assertEquals(pop[2],600);
        Assert.assertEquals(pop[3],700);
        Assert.assertEquals(pop[4],800);
        Assert.assertEquals(pop[5],900);
        Assert.assertEquals(pop[6],1000);
        Assert.assertEquals(pop[7],1100);
        Assert.assertEquals(pop[8],1200);


    }

    private RecentlyTournamentList addTournament(long tournamentId,DataStore ds,int size){
        RecentlyTournamentList recentlyTournamentList = RecentlyTournamentList.lookup(ds,100,"hero",size);
        TournamentManager tournamentManager= new TournamentManager();
        tournamentManager.distributionId(tournamentId);
        recentlyTournamentList.push(tournamentManager);
        recentlyTournamentList.update();
        return recentlyTournamentList;
    }

}
