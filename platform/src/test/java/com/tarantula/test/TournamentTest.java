package com.tarantula.test;


import com.icodesoftware.DataStore;
import com.tarantula.platform.LobbyTypeIdIndex;
import com.tarantula.platform.tournament.TournamentSchedule;
import org.testng.Assert;
import org.testng.annotations.Test;


public class TournamentTest extends DataStoreHook{


    @Test(groups = { "Tournament" })
    public void tournamentScheduleTest() {
        DataStore dataStore = dataStoreProvider.createDataStore("test_tarantula_tournament");
        TournamentSchedule schedule = new TournamentSchedule();

        //long deploymentId = serviceContext.distributionId();
        //long lobbyId = serviceContext.distributionId();
        //long gameClusterId = serviceContext.distributionId();
        //LobbyTypeIdIndex created = new LobbyTypeIdIndex(deploymentId,"holee-lobby",lobbyId,gameClusterId);
        //Assert.assertTrue(dataStore.createIfAbsent(created,false));
        //Assert.assertEquals(created.lobbyId(),lobbyId);
        //Assert.assertEquals(created.gameClusterId(),gameClusterId);
        //LobbyTypeIdIndex load = new LobbyTypeIdIndex(deploymentId,"holee-lobby");
        //Assert.assertTrue(dataStore.load(load));
        //Assert.assertEquals(load.lobbyId(),lobbyId);
        //Assert.assertEquals(load.gameClusterId(),gameClusterId);
    }
}
