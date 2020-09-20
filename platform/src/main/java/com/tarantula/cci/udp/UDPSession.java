package com.tarantula.cci.udp;

import com.tarantula.Event;
import com.tarantula.cci.OnExchange;

public class UDPSession implements OnExchange {

    @Override
    public String id() {
        return null;
    }

    @Override
    public String path() {
        return null;
    }

    @Override
    public String method() {
        return null;
    }

    @Override
    public String header(String name) {
        return null;
    }

    @Override
    public byte[] payload() {
        return new byte[0];
    }

    @Override
    public boolean onEvent(Event event) {
        return false;
    }
}
