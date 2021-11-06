package com.tarantula.game.service;

import com.icodesoftware.ApplicationContext;
import com.icodesoftware.OnLog;
import com.icodesoftware.Session;
import com.icodesoftware.Statistics;
import com.tarantula.game.*;
import com.tarantula.platform.room.GameRoom;
import com.tarantula.platform.statistics.StatisticsSerializer;

public class PVPRoomProxy extends RoomProxyHeader{

    @Override
    public void setup(ApplicationContext applicationContext, GameLobby gameLobby, GameZone gameZone) {
        super.setup(applicationContext,gameLobby,gameZone);
    }
    @Override
    public Stub join(Session session, Rating rating) {
        Stub stub = new Stub();
        stub.distributionKey(session.systemId());
        stub.label(application.tag());
        this.dataStore.createIfAbsent(stub,true);
        GameRoom _joined = gameServiceProvider.roomServiceProvider().join(gameZone,rating);
        if(_joined==null) {
            stub.joined = false;
            return stub;
        }
        stub.room = _joined;
        stub.joined = true;
        stub.zone = gameZone;
        stub.rating = rating;
        stub.channel = context.register(session.systemId(),(h,m)->{
            //this.context.log(m.readUTF8(), OnLog.WARN);
            Statistics statistics = gameServiceProvider.statistics(stub.systemId());
            statistics.entry("kills").update(1).update();
            statistics.entry("wins").update(1).update();
            statistics.entry("hits").update(1).update();
            statistics.entry("healthy").update(1).update();
            statistics.entry("roll").update(1).update();
            statistics.entry("poll").update(1).update();
            StatisticsSerializer serializer = new StatisticsSerializer();
            return serializer.serialize(statistics,Statistics.class,null).toString().getBytes();
            //return (stub.toJson().toString()).getBytes();
        });
        stub.tag = application.tag();
        stub.ticket = this.context.validator().ticket(session.systemId(),session.stub());
        this.dataStore.update(stub);
        return stub;
    }
    public void leave(Stub stub){
        stub.joined = false;
        this.dataStore.update(stub);
        this.gameServiceProvider.roomServiceProvider().leave(stub.room.roomId(),stub.systemId());
    }
}
