package com.icodesoftware;

import java.time.LocalDateTime;
import java.util.List;

public interface Tournament extends Configurable {

    String ENTRY_LABEL = "TEE";
    String HISTORY_LABEL = "History";


    enum Schedule{
        DAILY_SCHEDULE,WEEKLY_SCHEDULE,MONTHLY_SCHEDULE,ON_DEMAND_SCHEDULE
    }
    enum Status{
        PENDING,STARTING,STARTED,CLOSED,ENDED
    }
    Schedule schedule();
    String type();

    String name();

    boolean global();
    double enterCost();

    Status status();
    LocalDateTime startTime();
    LocalDateTime closeTime();
    LocalDateTime endTime();
    int maxEntriesPerInstance();
    int durationMinutesPerInstance();

    long scheduleId();

    Instance register(Session session);

    interface Entry extends Configurable {
        String systemId();
        void score(double credit,double delta);
        double score();
        double credit();
        void finish();
        boolean finished();
        int rank();
    }
    interface Instance extends Configurable {

        Status status();
        int maxEntries();
        LocalDateTime startTime();
        LocalDateTime closeTime();
        LocalDateTime endTime();
        int enter(String systemId);
        //int enter(Session session);
        //boolean update(String systemId,OnEntry onEntry);
        boolean update(Session session,OnEntry onEntry);
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
        void tournamentStarted(Tournament tournament);
        void tournamentClosed(Tournament tournament);
        void tournamentEnded(Tournament tournament);
    }

    interface OnEntry{
        boolean on(Entry entry);
    }

}
