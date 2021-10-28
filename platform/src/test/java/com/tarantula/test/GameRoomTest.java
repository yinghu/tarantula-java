package com.tarantula.test;


import com.tarantula.game.Arena;
import com.tarantula.platform.room.GameRoom;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class GameRoomTest {

    @BeforeClass
    public void setUp() {
    }

    @Test(groups = { "GameRoom" })
    public void setupTest() {
        GameRoom room = new GameRoom(10);
        room.dataStore(new EmptyDataStore());
        room.load();
        Assert.assertEquals(room.capacity(),10);
        Arena arena = new Arena();
        arena.capacity = 2;
        room.setup(arena);
        Assert.assertEquals(room.capacity(),2);

    }
    @Test(groups = { "GameRoom" })
    public void joinTest() {
        GameRoom room = new GameRoom(10);
        room.dataStore(new EmptyDataStore());
        room.load();
        room.join("player1");
        room.join("player1");
        room.join("player1");
        room.join("player1");
        Assert.assertEquals(room.leave("player1"),true);

    }

    @Test(groups = { "GameRoom" })
    public void leaveTest() {
        GameRoom room = new GameRoom(10);
        room.dataStore(new EmptyDataStore());
        room.load();
        room.join("player1");
        room.join("player2");
        room.join("player3");
        room.join("player4");
        Assert.assertEquals(room.leave("player1"),false);
        Assert.assertEquals(room.leave("player2"),false);
        Assert.assertEquals(room.leave("player3"),false);
        Assert.assertEquals(room.leave("player4"),true);
    }

}
