package com.tarantula.test;

import com.icodesoftware.DataStore;
import com.icodesoftware.LeaderBoard;
import com.icodesoftware.protocol.statistics.StatisticsEntry;
import com.icodesoftware.util.SnowflakeKey;
import com.tarantula.platform.presence.leaderboard.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;


public class LeaderBoardTest extends DataStoreHook{



    @Test(groups = { "LeaderBoard" })
    public void setupTest() {
        DataStore dataStore = dataStoreProvider.createDataStore("test_ldb_entry");
        long boardId = serviceContext.distributionId();
        LeaderBoardEntry leaderBoardEntry = new LeaderBoardEntry(LeaderBoard.DAILY,"kills");
        leaderBoardEntry.ownerKey(SnowflakeKey.from(boardId));
        Assert.assertEquals(leaderBoardEntry.label(),LeaderBoard.DAILY+"_kills");
        Assert.assertTrue(dataStore.create(leaderBoardEntry));
        Assert.assertTrue(leaderBoardEntry.distributionId()>0);
        List<LeaderBoardEntry> board = dataStore.list(new LeaderBoardEntryQuery(boardId,LeaderBoard.DAILY,"kills"));
        Assert.assertEquals(board.size(),1);
        board.forEach((e)->{
            Assert.assertEquals(e.classifier(),LeaderBoard.DAILY);
            Assert.assertEquals(e.category(),"kills");
        });
    }

    @Test(groups = { "LeaderBoard" })
    public void boardSyncTest() {
        DataStore dataStore = dataStoreProvider.createDataStore("test_ldb_sync");
        long boardId = serviceContext.distributionId();
        BoardSync boardSync = new BoardSync(LeaderBoard.DAILY,"wins",10,new EntryComparator());
        boardSync.distributionId(boardId);
        boardSync.dataStore(dataStore);
        int[] touched ={0};
        boardSync.load(e->{});
        //boardSync.load((e)->{
            //touched[0]++;
        //});
        Assert.assertEquals(touched[0],0);
        boardSync.onBoard(new LeaderBoardEntry(100,100,System.currentTimeMillis()),(e)->{
            Assert.assertEquals(e.value(),100);
            Assert.assertEquals(e.systemId(),100);
            touched[0]++;
        });
        Assert.assertEquals(touched[0],1);
        BoardSync loaded = new BoardSync(LeaderBoard.DAILY,"wins",10,new EntryComparator());
        loaded.distributionId(boardId);
        loaded.dataStore(dataStore);
        //loaded.load((e)->{
            //Assert.assertEquals(e.systemId(),100);
            //Assert.assertEquals(e.value(),100);
            //Assert.assertNotNull(e.category());
            //Assert.assertNotNull(e.classifier());
        //});
        loaded.load(e->{});
        for(int i=1;i<11;i++){
            long id = i;
            double v = 100+i;
            loaded.onBoard(new LeaderBoardEntry(id,v,System.currentTimeMillis()),(e)->{
                Assert.assertEquals(e.value(),v);
                Assert.assertEquals(e.systemId(),id);
            });
        }
        Assert.assertFalse(loaded.onBoard(100));
        loaded.reset();
        BoardSync reset = new BoardSync(LeaderBoard.DAILY,"wins",10,new EntryComparator());
        reset.distributionId(boardId);
        reset.dataStore(dataStore);
        touched[0]=0;
        //reset.load((e)->{
            //touched[0]++;
        //});
        Assert.assertEquals(touched[0],0);
    }

    @Test(groups = { "LeaderBoard" })
    public void boardViewTest() {
        DataStore dataStore = dataStoreProvider.createDataStore("test_ldb_view");
        long boardId = serviceContext.distributionId();
        BoardSync boardSync = new BoardSync(LeaderBoard.DAILY,"wins",10,new EntryComparator());
        boardSync.distributionId(boardId);
        boardSync.dataStore(dataStore);
        int[] updates = {0};
        BoardView view = new BoardView(boardSync,(e)->updates[0]++,new EntryComparator());
        //view.load();
        view.onBoard(new LeaderBoardEntry(100,100,System.currentTimeMillis()));
        view.onBoard(new LeaderBoardEntry(200,90,System.currentTimeMillis()));
        view.onBoard(new LeaderBoardEntry(300,120,System.currentTimeMillis()));
        view.onBoard(new LeaderBoardEntry(400,93,System.currentTimeMillis()));
        view.onBoard(new LeaderBoardEntry(500,1000,System.currentTimeMillis()));
        view.onBoard(new LeaderBoardEntry(600,920,System.currentTimeMillis()));
        view.onBoard(new LeaderBoardEntry(700,101,System.currentTimeMillis()));
        view.onBoard(new LeaderBoardEntry(800,190,System.currentTimeMillis()));
        view.onBoard(new LeaderBoardEntry(900,200,System.currentTimeMillis()));
        view.onBoard(new LeaderBoardEntry(1000,190,System.currentTimeMillis()));

        view.onBoard(new LeaderBoardEntry(1000,2290,System.currentTimeMillis()));
        view.onBoard(new LeaderBoardEntry(2000,91,System.currentTimeMillis()));

        List<LeaderBoard.Entry> board = new ArrayList<>();
        view.rank((e)->{
            board.add(e);
        });
        Assert.assertEquals(board.size(),10);
        Assert.assertEquals(board.get(0).value(),2290);
        Assert.assertEquals(board.get(0).systemId(),1000);
        Assert.assertEquals(board.get(0).rank(),1);
        Assert.assertEquals(board.get(1).rank(),2);
        Assert.assertEquals(board.get(2).rank(),3);
        Assert.assertEquals(board.get(3).rank(),4);
        Assert.assertEquals(board.get(4).rank(),5);
        Assert.assertEquals(board.get(5).rank(),6);
        Assert.assertEquals(board.get(6).rank(),7);
        Assert.assertEquals(board.get(7).rank(),8);
        Assert.assertEquals(board.get(8).rank(),9);
        Assert.assertEquals(board.get(9).rank(),10);
        Assert.assertEquals(board.get(9).value(),91);
        Assert.assertEquals(board.get(9).systemId(),2000);
        Assert.assertEquals(updates[0],12);
    }

    @Test(groups = { "LeaderBoard" })
    public void leaderBoardSyncTest() {
        DataStore dataStore = dataStoreProvider.createDataStore("test_ldb");
        long boardId = serviceContext.distributionId();
        int[] updates ={0};

        LeaderBoardSync ldb = new LeaderBoardSync("jams",10,boardId, e->{
            updates[0]++;
            Assert.assertNotNull(e.category());
            Assert.assertTrue(e.category().equals("jams"));
            Assert.assertNotNull(e.classifier());
        });
        ldb.dataStore(dataStore);
        ldb.load();
        Assert.assertNotNull(ldb.daily());
        Assert.assertNotNull(ldb.weekly());
        Assert.assertNotNull(ldb.monthly());
        Assert.assertNotNull(ldb.yearly());
        Assert.assertNotNull(ldb.total());
        StatisticsEntry entry = new StatisticsEntry(SnowflakeKey.from(1000),"jams");
        entry.systemId(1000);
        entry.update(100);
        ldb.onAllBoard(entry);
        Assert.assertEquals(updates[0],5);

    }

}
