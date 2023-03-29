package com.icodesoftware;

public interface Room {

    int capacity();

    long duration();

    int round();

    //game end buffer timer
    long overtime();

    int joinsOnStart();

    //session timeout minutes
    int timeout();

    boolean dedicated();
}
