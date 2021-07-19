package com.icodesoftware;

import java.time.LocalDateTime;
import java.util.List;

public interface Tournament extends Recoverable {

    String ENTRY_LABEL = "TEE";

    String DAILY_SCHEDULE = "daily";
    String ON_DEMAND_SCHEDULE = "onDemand";

    enum Status{
        SCHEDULED,STARTED,CLOSED,ENDED
    }
    String type();
    void type(String type);
    String description();
    String icon();
    Status status();
    LocalDateTime startTime();
    LocalDateTime closeTime();
    LocalDateTime endTime();
    int maxEntriesPerInstance();
    int durationMinutesPerInstance();

    String join(String systemId);

    interface Entry extends Recoverable {
        String systemId();
        double score(double delta);
        int rank();
    }
    interface Instance extends Recoverable {
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
    interface Listener{
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

    interface Schedule extends Recoverable{
        String type();
        String schedule();
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
