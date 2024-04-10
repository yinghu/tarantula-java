package com.tarantula.test;

import com.icodesoftware.DataStore;

import com.icodesoftware.util.SnowflakeKey;
import com.tarantula.game.GameZone;
import com.tarantula.platform.room.*;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class GameRoomTest extends DataStoreHook{



    @Test(groups = { "GameRoom" })
    public void setupTest() {
        DataStore dataStore = dataStoreProvider.createDataStore("test_room");
        long zoneId = serviceContext.distributionId();
        GameRoom room = GameRoom.newGameRoom(GameZone.PLAY_MODE_PVE,1);
        room.ownerKey(new SnowflakeKey(zoneId));
        Assert.assertTrue(dataStore.create(room));
        room.dataStore(dataStore);
        room.load();
        room.join(1,(r,e)->{});
        Assert.assertEquals(room.entries().size(),1);
        List<GameRoom> rooms = dataStore.list(new GameRoomQuery(zoneId,GameZone.PLAY_MODE_PVE,1));
        Assert.assertEquals(rooms.size(),1);
        rooms.get(0).dataStore(dataStore);
        rooms.get(0).load();
        Assert.assertEquals(rooms.get(0).entries().size(),1);
        rooms.get(0).leave(1,(r,e)->{});
        Assert.assertEquals(rooms.get(0).entries().size(),0);

    }

    @Test(groups = { "GameRoom" })
    public void joinTest() {
        DataStore dataStore = dataStoreProvider.createDataStore("test_room");
        GameRoom room = GameRoom.newGameRoom(GameZone.PLAY_MODE_PVP,5);
        dataStore.create(room);
        room.dataStore(dataStore);
        room.load();
        room.join(1,(room1,entry) -> {
            Assert.assertTrue(room1.available());
            Assert.assertEquals(room.totalJoined(),1);
        });
        room.join(2,(room1,entry) -> {
            Assert.assertTrue(room1.available());
            Assert.assertEquals(room.totalJoined(),2);
        });
        room.join(3,(room1,entry) -> {
            Assert.assertTrue(room1.available());
            Assert.assertEquals(room.totalJoined(),3);
        });
        room.join(4,(room1,entry) -> {
            Assert.assertTrue(room1.available());
            Assert.assertEquals(room.totalJoined(),4);
        });
        room.join(5,(room1,entry) -> {
            Assert.assertFalse(room1.available());
            Assert.assertEquals(room.totalJoined(),5);
            Assert.assertTrue(room.totalJoined()==room.capacity());
        });
        room.leave(1,(room1,entry) -> {
            Assert.assertFalse(room1.available());
            Assert.assertEquals(room.totalLeft(),1);
        });
        room.leave(2,(room1,entry) -> {
            Assert.assertFalse(room1.available());
            Assert.assertEquals(room.totalLeft(),2);
        });
        room.leave(3,(room1,entry) -> {
            Assert.assertFalse(room1.available());
            Assert.assertEquals(room.totalLeft(),3);
        });
        room.leave(4,(room1,entry) -> {
            Assert.assertFalse(room1.available());
            Assert.assertEquals(room.totalLeft(),4);
        });
        room.leave(5,(room1,entry) -> {
            Assert.assertFalse(room1.available());
            Assert.assertEquals(room.totalLeft(),5);
            Assert.assertTrue(room.totalLeft()==room.capacity());
        });
    }

    @Test(groups = { "GameRoom" })
    public void removeTest() {
        LinkedBlockingDeque q = new LinkedBlockingDeque(3);

        PVEGameRoom p1 = new PVEGameRoom();
        p1.distributionId(1);

        PVEGameRoom p2 = new PVEGameRoom();
        p2.distributionId(2);

        PVEGameRoom p3 = new PVEGameRoom();
        p3.distributionId(3);

        PVEGameRoom p4 = new PVEGameRoom();
        p4.distributionId(4);

        q.offer(p1);
        q.offer(p2);
        q.offer(p3);
        q.offer(p4);

        Assert.assertEquals(3,q.size());
        Assert.assertTrue(q.remove(p2));
        Assert.assertEquals(2,q.size());
    }

    @Test(groups = { "GameRoom" })
    public void removeRoomSubTest() {
        ArrayBlockingQueue<RoomStub> stubs = new ArrayBlockingQueue<>(100);
        long roomId = 1000;
        for(int i=0;i<100;i++){
            stubs.offer(new RoomStub(roomId,i));
        }
        Assert.assertEquals(stubs.size(),100);
        for(int i=0;i<100;i++){
            Assert.assertTrue(stubs.remove(new RoomStub(roomId,i)));
        }
        Assert.assertEquals(stubs.size(),0);
    }

}
