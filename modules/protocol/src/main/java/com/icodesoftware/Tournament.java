package com.icodesoftware;

import java.time.LocalDateTime;
import java.util.List;

public interface Tournament extends Configurable {

    String ENTRY_LABEL = "TEE";
    String HISTORY_LABEL = "History";

    int DAILY_SCHEDULE = 0;
    int WEEKLY_SCHEDULE = 1;
    int MONTHLY_SCHEDULE = 2;
    int ON_DEMAND_SCHEDULE = 3;

    enum Status{
        PENDING,SCHEDULED,STARTING,STARTED,CLOSED,ENDED
    }
    int schedule();
    String type();

    String name();

    String description();

    double enterCost();

    Status status();
    LocalDateTime startTime();
    LocalDateTime closeTime();
    LocalDateTime endTime();
    int maxEntriesPerInstance();
    int durationMinutesPerInstance();

    //void close();


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
        int join(String systemId);
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
        void tournamentStarted(Tournament tournament);
        void tournamentClosed(Tournament tournament);
        void tournamentEnded(Tournament tournament);
    }

    interface OnEntry{
        void on(Entry entry);
    }

}
