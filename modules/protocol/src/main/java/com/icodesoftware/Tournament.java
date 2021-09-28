package com.icodesoftware;

import java.time.LocalDateTime;
import java.util.List;

public interface Tournament extends Configurable {

    String ENTRY_LABEL = "TEE";
    String HISTORY_LABEL = "History";


    String DAILY_SCHEDULE = "daily";
    String ON_DEMAND_SCHEDULE = "onDemand";

    enum Status{
        SCHEDULED,STARTED,CLOSED,ENDED
    }
    String schedule();
    String type();
    void type(String type);
    String name();
    Status status();
    LocalDateTime startTime();
    LocalDateTime closeTime();
    LocalDateTime endTime();
    int maxEntriesPerInstance();
    int durationMinutesPerInstance();

    String register(String systemId);

    interface Entry extends Configurable {
        String systemId();
        double score(double delta);
        int rank();
    }
    interface Instance extends Configurable {

        Status status();
        int maxEntries();
        LocalDateTime startTime();
        LocalDateTime closeTime();
        LocalDateTime endTime();
        Entry join(String systemId);
        void update(String systemId,OnEntry onEntry);

        RaceBoard raceBoard();
    }
    interface Prize extends Configurable{
        int rank();
    }
    interface RaceBoard extends Configurable{
        int size();
        List<Entry> list();
    }
    interface History extends Configurable{
        String tournamentId();
        int rank();
        double score();
        LocalDateTime dateTime();
    }
    interface Listener{
        default void tournamentScheduled(Tournament tournament){}
        default void tournamentStarted(Tournament tournament){}
        default void tournamentClosed(Tournament tournament){}
        default void tournamentEnded(Tournament tournament){}

        //default void onStarted(Instance instance){}
        //default void onClosed(Instance instance){}
        //default void onEnded(Instance instance){}

        //default void onCreated(Entry entry){}
        //default void onUpdated(Entry entry){}
    }

    interface Schedule extends Configurable{
        String type();
        String schedule();
        String name();
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
