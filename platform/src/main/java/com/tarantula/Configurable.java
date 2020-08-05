package com.tarantula;

public interface Configurable extends Recoverable, DataStore.Updatable {

    void registerListener(Listener listener);
    void update(Configurable update);
    interface Listener{
        void onUpdated(Configurable c);
    }
}
