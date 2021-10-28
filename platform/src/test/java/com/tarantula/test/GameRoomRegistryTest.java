package com.tarantula.test;

import com.tarantula.game.Arena;
import com.tarantula.game.Rating;
import com.tarantula.platform.RoomRegistry;
import com.tarantula.platform.room.GameRoomRegistry;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

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
        Assert.assertEquals(roomRegistry.addPlayer("player1"), RoomRegistry.JOINED);
        Assert.assertEquals(roomRegistry.fullJoined(),false);
        Assert.assertEquals(roomRegistry.addPlayer("player2"), RoomRegistry.FULLY_JOINED);
        Assert.assertEquals(roomRegistry.fullJoined(),true);
        Assert.assertEquals(roomRegistry.addPlayer("player1"), RoomRegistry.ALREADY_JOINED);
        Assert.assertEquals(roomRegistry.addPlayer("player2"), RoomRegistry.ALREADY_JOINED);
        Assert.assertEquals(roomRegistry.addPlayer("player3"), RoomRegistry.NOT_JOINED);

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
        roomRegistry.addPlayer("player1");
        Assert.assertEquals(roomRegistry.fullJoined(),false);
        roomRegistry.addPlayer("player2");
        Assert.assertEquals(roomRegistry.fullJoined(),true);
        Assert.assertEquals(roomRegistry.empty(),false);
    }


}
