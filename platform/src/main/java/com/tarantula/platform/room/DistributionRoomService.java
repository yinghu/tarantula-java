package com.tarantula.platform.room;

import com.icodesoftware.service.ServiceProvider;
import com.tarantula.game.Rating;

public interface DistributionRoomService extends ServiceProvider {

    String NAME = "DistributionRoomService";

    RoomJoinStub onRegisterRoom(String serviceName,String zoneId,Rating rating);
    GameRoom onJoinRoom(String serviceName, String roomId, String systemId);
    void onLeaveRoom(String serviceName,String roomId,String systemId);


    void release(String serviceName,String zoneId,String roomId,String systemId);
    void sync(String serviceName,String zoneId,String roomId,String[] joined);
    GameRoom view(String serviceName, String roomId);
    GameZoneIndex localManaged(String key);
    GameChannelIndex localManaged(int channelId);
    void create(String serviceName,String zoneId,String roomId);
    void load(String serviceName,String roomId);
}
