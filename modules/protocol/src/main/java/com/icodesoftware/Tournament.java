package com.icodesoftware;

import java.time.LocalDateTime;
import java.util.List;

public interface Tournament extends Configurable {

    String ENTRY_LABEL = "TEE";

    enum Status{
        SCHEDULED,STARTED,CLOSED,ENDED
    }
    String description();
    String icon();
    Status status();
    LocalDateTime startTime();
    LocalDateTime closeTime();
    LocalDateTime endTime();
    int maxEntriesPerInstance();
    int durationMinutesPerInstance();

    String join(String systemId);

    interface Entry extends Configurable {
        String systemId();
        double score(double delta);
        int rank();
    }
    interface Instance extends Configurable {
        String id();
        Status status();
        int maxEntries();
        LocalDateTime startTime();
        LocalDateTime closeTime();
        LocalDateTime endTime();
        Entry enter(String systemId);
        void update(String systemId,OnEntry onEntry);
        List<Entry> list();
    }
    interface Listener extends Configurable.Listener{
        default void tournamentScheduled(Tournament tournament){}
        default void tournamentStarted(Tournament tournament){}
        default void tournamentClosed(Tournament tournament){}
        default void tournamentEnded(Tournament tournament){}

        default void onStarted(Instance instance){}
        default void onClosed(Instance instance){}
        default void onEnded(Instance instance){}

        default void onCreated(Entry entry){}
        default void onUpdated(Entry entry){}
    }

    interface Schedule{
        String type();
        String description();
        String icon();
        LocalDateTime startTime();
        LocalDateTime closeTime();
        LocalDateTime endTime();
        int maxEntriesPerInstance();
        int instanceDurationInMinutes();
    }
    interface OnEntry{
        void on(Entry entry);
    }

}
