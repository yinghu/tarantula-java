package com.icodesoftware.protocol;

import com.icodesoftware.Recoverable;

public interface Room extends Recoverable {

    int channelId();

    int capacity();

    long duration();

    int round();

    //game end buffer timer
    long overtime();

    int joinsOnStart();

    //session timeout minutes
    int timeout();

    boolean dedicated();

    boolean available();

    int totalJoined();

    int totalLeft();

    Arena arena();
}
