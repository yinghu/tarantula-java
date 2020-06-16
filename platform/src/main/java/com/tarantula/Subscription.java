package com.tarantula;

public interface Subscription extends Recoverable,Countable {

    String DataStore = "subscription";




    long startTimestamp();
    long endTimestamp();
    void startTimestamp(long startTimestamp);
    void endTimestamp(long endTimestamp);

}
