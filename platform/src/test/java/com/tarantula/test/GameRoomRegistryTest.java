package com.tarantula.test;

import com.tarantula.game.Arena;
import com.tarantula.game.Rating;
import com.tarantula.platform.RoomRegistry;
import com.tarantula.platform.room.GameRoomRegistry;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class GameRoomRegistryTest {

    @BeforeClass
    public void setUp() {
    }

    @Test(groups = { "GameRoomRegistry" })
    public void joinTest() {
        Arena arena = new Arena();
        arena.level = 1;
        arena.capacity = 2;
        GameRoomRegistry roomRegistry = new GameRoomRegistry(arena);
        Assert.assertEquals(roomRegistry.addPlayer("player1",r->true), RoomRegistry.JOINED);
        Assert.assertEquals(roomRegistry.fullJoined(),false);
        Assert.assertEquals(roomRegistry.addPlayer("player2",r->true), RoomRegistry.FULLY_JOINED);
        Assert.assertEquals(roomRegistry.fullJoined(),true);
        Assert.assertEquals(roomRegistry.addPlayer("player1",r->true), RoomRegistry.ALREADY_JOINED);
        Assert.assertEquals(roomRegistry.addPlayer("player2",r->true), RoomRegistry.ALREADY_JOINED);
        Assert.assertEquals(roomRegistry.addPlayer("player3",r->true), RoomRegistry.NOT_JOINED);

    }
    @Test(groups = { "GameRoomRegistry" })
    public void emptyTest() {
        GameRoomRegistry roomRegistry = new GameRoomRegistry();
        Assert.assertEquals(roomRegistry.fullJoined(),false);
        Assert.assertEquals(roomRegistry.empty(),true);
    }

    @Test(groups = { "GameRoomRegistry" })
    public void resetTest() {
        GameRoomRegistry roomRegistry = new GameRoomRegistry();
        Arena arena = new Arena();
        arena.level = 1;
        arena.capacity = 2;
        Assert.assertEquals(roomRegistry.fullJoined(),false);
        Assert.assertEquals(roomRegistry.empty(),true);
        roomRegistry.reset(arena);
        roomRegistry.addPlayer("player1",r->true);
        Assert.assertEquals(roomRegistry.fullJoined(),false);
        roomRegistry.addPlayer("player2",r->true);
        Assert.assertEquals(roomRegistry.fullJoined(),true);
        Assert.assertEquals(roomRegistry.empty(),false);
    }
    @Test(groups = { "GameRoomRegistry" })
    public void queueTest() {
        Arena arena = new Arena();
        arena.level = 1;
        arena.capacity = 2;
        ConcurrentHashMap<String,GameRoomRegistry> _m = new ConcurrentHashMap<>();
        ConcurrentLinkedDeque<GameRoomRegistry> _q = new ConcurrentLinkedDeque<>();
        GameRoomRegistry roomRegistry = new GameRoomRegistry();
        roomRegistry.reset(arena);
        roomRegistry.distributionKey("BDS01/T10000");
        _m.put("abc",roomRegistry);
        _q.offerFirst(roomRegistry);
        GameRoomRegistry _rm = _m.get("abc");
        GameRoomRegistry _rq = _q.poll();
        Assert.assertEquals(_rq==roomRegistry,true);
        Assert.assertEquals(_rm==roomRegistry,true);
        Assert.assertEquals(_rm==_rq,true);
        roomRegistry.addPlayer("abc",r->true);
        Assert.assertEquals(roomRegistry.empty(),false);
        Assert.assertEquals(_rm.empty(),false);
        Assert.assertEquals(_rq.empty(),false);

    }


}
