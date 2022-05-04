package com.tarantula.test;


import com.tarantula.game.Arena;
import com.tarantula.platform.room.PVPGameRoom;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class GameRoomTest {

    @BeforeClass
    public void setUp() {
    }

    @Test(groups = { "GameRoom" })
    public void setupTest() {
        PVPGameRoom room = new PVPGameRoom(10);
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
        PVPGameRoom room = new PVPGameRoom(10);
        room.dataStore(new EmptyDataStore());
        room.load();
        room.join("player1",r->true);
        room.join("player1",r->true);
        room.join("player1",r->true);
        room.join("player1",r->true);
        room.leave("player1",(r)->{
            Assert.assertEquals(true,r.resetIfEmpty());
            return true;
        });
    }

    @Test(groups = { "GameRoom" })
    public void leaveTest() {
        PVPGameRoom room = new PVPGameRoom(10);
        room.dataStore(new EmptyDataStore());
        room.load();
        room.join("player1",r->true);
        room.join("player2",r->true);
        room.join("player3",r->true);
        room.join("player4",r->true);

        room.leave("player1",(r)->{
            Assert.assertEquals(false,r.resetIfEmpty());
            return false;
        });
        room.leave("player2",(r)->{
            Assert.assertEquals(false,r.resetIfEmpty());
            return false;
        });
        room.leave("player3",(r)->{
            Assert.assertEquals(false,r.resetIfEmpty());
            return false;
        });
        room.leave("player4",(r)->{
            Assert.assertEquals(true,r.resetIfEmpty());
            return false;
        });
        String p = "player's mac";
        System.out.println(p.replace('\'', ' '));
    }

}
