package com.icodesoftware;

import java.time.LocalDateTime;
import java.util.List;

public interface Tournament extends Configurable {

    String SCHEDULE_LABEL = "tournament_schedule";

    String MANAGER_LABEL = "tournament_manager";
    String REGISTER_LABEL = "tournament_register";
    String INSTANCE_LABEL = "tournament_instance";
    String GLOBAL_INSTANCE_LABEL = "tournament_global";
    String ENTRY_LABEL = "tournament_entry";

    String HISTORY_LABEL = "tournament_history";


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

    Instance register(Session session, int playerLevel);

    interface Entry extends Configurable {
        long systemId();
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

        boolean update(Session session,OnEntry onEntry);
        RaceBoard raceBoard();
    }
    interface Prize extends Configurable{
        int rank();
    }
    interface RaceBoard extends Configurable{
        int size();
        List<Entry> list();

        Entry myPosition();
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
