package com.tarantula.test;


import com.icodesoftware.DataStore;

import com.icodesoftware.protocol.Room;
import com.icodesoftware.util.SnowflakeKey;

import com.tarantula.game.SimpleStub;
import com.tarantula.game.Stub;
import com.tarantula.platform.room.*;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class GameRoomTest extends DataStoreHook{



    @Test(groups = { "GameRoom" })
    public void setupTest() {
        DataStore dataStore = dataStoreProvider.createDataStore("test_room");
        long zoneId = serviceContext.distributionId();
        GameRoomHeader room = new GameRoomHeader(2);
        room.ownerKey(new SnowflakeKey(zoneId));
        Assert.assertTrue(dataStore.create(room));
        GameRoomHeader load = new GameRoomHeader();
        load.distributionId(room.distributionId());
        Assert.assertTrue(dataStore.load(load));
        Assert.assertEquals(load.capacity(),room.capacity());
        room.dataStore(dataStore);
        room.load();
        Room.Seat[] table = room.table();
        for(int i=0 ;i<table.length;i++){
            Assert.assertEquals(table[i].number(),i);
            Assert.assertEquals(table[i].occupied(),false);
        }
        SimpleStub stub = new SimpleStub(100,200);
        room.join(stub,new RoomStub(100,0));
        Assert.assertEquals(room.table()[0].occupied(),true);
        List<GameRoomHeader> rooms = dataStore.list(new GameRoomQuery(zoneId));
        Assert.assertEquals(rooms.size(),1);
        rooms.get(0).dataStore(dataStore);
        rooms.get(0).load();
        Assert.assertEquals(rooms.get(0).table()[0].systemId(),100);
        Stub l = new Stub();
        l.systemId(100);
        l.stub(200);
        rooms.get(0).leave(l);
        Assert.assertEquals(rooms.get(0).table()[0].systemId(),0);

    }


}
