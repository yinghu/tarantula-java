package com.icodesoftware.protocol;

import com.icodesoftware.Recoverable;

public interface Room extends Recoverable {

    int capacity();

    long duration();

    //game end buffer timer
    long overtime();

    int joinsOnStart();
    
    Seat[] table();

    Arena arena();

    interface Seat extends Recoverable{
        int team();
        long stub();
        boolean occupied();
    }
}
