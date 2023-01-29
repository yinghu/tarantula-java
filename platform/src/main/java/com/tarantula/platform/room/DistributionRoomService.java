package com.tarantula.platform.room;

import com.icodesoftware.service.ServiceProvider;

public interface DistributionRoomService extends ServiceProvider {

    String NAME = "DistributionRoomService";

    GameRoom onJoinRoom(String serviceName, String zoneId,String roomId, String systemId);

    void onLeaveRoom(String serviceName,String zoneId,String roomId,String systemId);

    GameRoom onRoomView(String serviceName,String zoneId, String roomId);

    void onResetRoom(String serviceName,String zoonId,String roomId);


}
