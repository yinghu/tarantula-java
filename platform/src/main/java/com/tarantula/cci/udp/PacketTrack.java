package com.tarantula.cci.udp;

import java.time.LocalDateTime;

public class PacketTrack {
    public int count;
    public final LocalDateTime creationTime;

    public PacketTrack(long timeout){
        creationTime = LocalDateTime.now().plusSeconds(timeout);
    }
}
