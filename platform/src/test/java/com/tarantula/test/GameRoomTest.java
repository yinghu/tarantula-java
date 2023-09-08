package com.tarantula.test;

import com.icodesoftware.DataStore;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.platform.room.*;
import com.icodesoftware.service.DataStoreProvider;
import com.tarantula.platform.util.SystemUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

public class GameRoomTest extends DataStoreHook{



    @Test(groups = { "GameRoom" })
    public void setupTest() {
        DataStore dataStore = dataStoreProvider.createDataStore("test_room");
        PVPGameRoom room = new PVPGameRoom(10);
        Assert.assertTrue(dataStore.create(room));
        room.dataStore(dataStore);
        //room.load();
        //List<GameEntry> entries = dataStore.list(new GameEntryQuery(room.id()));
        //Assert.assertEquals(entries.size(),10);

        //PVPGameRoom load = new PVPGameRoom(10);
        ///load.distributionKey(room.distributionKey());
        ///Assert.assertTrue(dataStore.load(load));
        //load.dataStore(dataStore);
        //load.load();
        //List<GameEntry> loadEntries = dataStore.list(new GameEntryQuery(room.roomId()));
        //Assert.assertEquals(loadEntries.size(),10);
    }
    //@Test(groups = { "GameRoom" })
    public void joinTest() {
        DataStore dataStore = dataStoreProvider.createDataStore("test_room");
        PVPGameRoom room = new PVPGameRoom(5);
        dataStore.create(room);
        room.dataStore(dataStore);
        room.load();
        room.join("player1",(room1,entry) -> {
            Assert.assertTrue(room1.available());
            Assert.assertEquals(room.totalJoined(),1);
        });
        room.join("player2",(room1,entry) -> {
            Assert.assertTrue(room1.available());
            Assert.assertEquals(room.totalJoined(),2);
        });
        room.join("player3",(room1,entry) -> {
            Assert.assertTrue(room1.available());
            Assert.assertEquals(room.totalJoined(),3);
        });
        room.join("player4",(room1,entry) -> {
            Assert.assertTrue(room1.available());
            Assert.assertEquals(room.totalJoined(),4);
        });
        room.join("player5",(room1,entry) -> {
            Assert.assertFalse(room1.available());
            Assert.assertEquals(room.totalJoined(),5);
            Assert.assertTrue(room.totalJoined()==room.capacity());
        });
        room.leave("player1",(room1,entry) -> {
            Assert.assertFalse(room1.available());
            Assert.assertEquals(room.totalLeft(),1);
        });
        room.leave("player2",(room1,entry) -> {
            Assert.assertFalse(room1.available());
            Assert.assertEquals(room.totalLeft(),2);
        });
        room.leave("player3",(room1,entry) -> {
            Assert.assertFalse(room1.available());
            Assert.assertEquals(room.totalLeft(),3);
        });
        room.leave("player4",(room1,entry) -> {
            Assert.assertFalse(room1.available());
            Assert.assertEquals(room.totalLeft(),4);
        });
        room.leave("player5",(room1,entry) -> {
            Assert.assertFalse(room1.available());
            Assert.assertEquals(room.totalLeft(),5);
            Assert.assertTrue(room.totalLeft()==room.capacity());
        });
    }

    //@Test(groups = { "GameRoom" })
    public void removeTest() {
        LinkedBlockingDeque q = new LinkedBlockingDeque(3);
        PVEGameRoom p1 = new PVEGameRoom();
        p1.distributionKey("bds/"+ SystemUtil.oid());
        PVEGameRoom p2 = new PVEGameRoom();
        p2.distributionKey("bds/"+ SystemUtil.oid());
        PVEGameRoom p3 = new PVEGameRoom();
        p3.distributionKey("bds/"+ SystemUtil.oid());
        PVEGameRoom p4 = new PVEGameRoom();
        p4.distributionKey("bds/"+ SystemUtil.oid());

        q.offer(p1);
        q.offer(p2);
        q.offer(p3);
        q.offer(p4);

        Assert.assertEquals(3,q.size());
        Assert.assertTrue(q.remove(p2));
        Assert.assertEquals(2,q.size());
    }

}
