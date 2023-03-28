package com.icodesoftware.integration.udp;

import com.icodesoftware.Room;

public class DedicatedRoom implements Room {

    @Override
    public int capacity() {
        return 0;
    }

    @Override
    public long duration() {
        return 0;
    }

    @Override
    public int round() {
        return 0;
    }

    @Override
    public int timeout() {
        return 0;
    }

    @Override
    public boolean dedicated() {
        return true;
    }
}
