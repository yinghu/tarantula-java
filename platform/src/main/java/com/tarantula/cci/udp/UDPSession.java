package com.tarantula.cci.udp;

import com.tarantula.Connection;
import com.tarantula.Event;
import com.tarantula.cci.OnExchange;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class UDPSession implements OnExchange {

    private final String serverId;
    private final DatagramChannel datagramChannel;

    public UDPSession(String serverId,DatagramChannel datagramChannel){

        this.serverId = serverId;
        this.datagramChannel = datagramChannel;
    }
    @Override
    public String id() {
        return serverId;
    }

    @Override
    public String path() {
        return null;
    }

    @Override
    public String method() {
        return Connection.UDP;
    }

    @Override
    public String header(String name) {
        return this.serverId;
    }

    @Override
    public byte[] payload() {
        return new byte[0];
    }

    @Override
    public boolean onEvent(Event event) {
        try{
            ByteBuffer buffer = ByteBuffer.wrap(event.payload());
            datagramChannel.write(buffer);
        }catch (Exception ex){
            return true;
        }
        return false;
    }
}
