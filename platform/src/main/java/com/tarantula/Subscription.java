package com.tarantula;

public interface Subscription extends Recoverable {

    String DataStore = "subscription";

    boolean trial();
    void trial(boolean trial);

    boolean subscribed();
    void subscribed(boolean subscribed);

    long startTimestamp();
    long endTimestamp();
    void startTimestamp(long startTimestamp);
    void endTimestamp(long endTimestamp);


}
