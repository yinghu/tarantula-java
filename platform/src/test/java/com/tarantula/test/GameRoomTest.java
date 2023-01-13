package com.tarantula.test;

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
        //Arena arena = new Arena();
        //arena.capacity = 2;
        //room.setup(arena);
        //Assert.assertEquals(room.capacity(),10);

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
        /**
        Assert.assertEquals(true,"application".startsWith(Configurable.APPLICATION_CONFIG_TYPE));
        Assert.assertEquals(true,"application.lobby".startsWith(Configurable.APPLICATION_CONFIG_TYPE));
        String token = "BDS01/7c88ac77da684826857c63816a1ce442 tarantula 1656978601324 DA2195B286A6A3D3144D5A42123C997D25360361-84-1656972601324-F2FBD235841399B0EFE2A7D88D37F48D9F325397";
        try{
            MessageDigest msg = MessageDigest.getInstance(MDA);
            OnSession session = SystemUtil.validToken(msg,token);
            Assert.assertEquals(true,session.systemId()!=null);
            boolean suc = SystemUtil.validTicket(msg,session.systemId(),session.stub(),session.ticket());
            System.out.println(session.ticket()+">>>"+suc);
            System.out.println(session.systemId());
            System.out.println(session.stub());
        }catch (Exception ex){
            ex.printStackTrace();
        }
        **/
    }

}
