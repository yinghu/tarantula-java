package com.icodesoftware.integration;

import com.icodesoftware.protocol.DataBuffer;

/**
 * Created by yinghu lu on 11/15/2020.
 */
public interface Game {

    String zoneId();
    String roomId();


    void onSpec(DataBuffer dataBuffer);
    void onStart();
    void onClosing();
    void onClose();
    void onEnd();
    void onOvertime();
    void onJoinTimeout();
}
