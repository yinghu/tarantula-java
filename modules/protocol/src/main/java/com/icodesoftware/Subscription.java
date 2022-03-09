package com.icodesoftware;

public interface Subscription extends Recoverable, Countable {

    String DataStore = "subscription";

    boolean trial();
    void trial(boolean trial);
    long startTimestamp();
    long endTimestamp();
    void startTimestamp(long startTimestamp);
    void endTimestamp(long endTimestamp);

}
