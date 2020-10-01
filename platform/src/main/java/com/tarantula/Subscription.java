package com.tarantula;

import com.icodesoftware.Countable;
import com.icodesoftware.Recoverable;

public interface Subscription extends Recoverable, Countable {

    String DataStore = "subscription";




    long startTimestamp();
    long endTimestamp();
    void startTimestamp(long startTimestamp);
    void endTimestamp(long endTimestamp);

}
