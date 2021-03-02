package com.icodesoftware;

import java.time.LocalDateTime;
import java.util.List;

public interface Tournament extends Recoverable{

    String INSTANCE_LABEL = "TIT";
    String ENTRY_LABEL = "TEE";

    String type();
    LocalDateTime startTime();
    LocalDateTime closeTime();
    LocalDateTime endTime();
    int maxEntriesPerInstance();
    int durationMinutesPerInstance();

    Instance join(String systemId);

    interface Entry extends Recoverable{
        String systemId();
        String name();
        void name(String name);
        String icon();
        void icon(String icon);
        double score(double delta);
    }
    interface Instance extends Recoverable{
        String id();
        int maxEntries();
        LocalDateTime startTime();
        LocalDateTime closeTime();
        LocalDateTime endTime();
        void enter(Entry entry);
        Entry entry(String systemId);
        List<Entry> list();
    }
    interface Listener{
        default void tournamentLoaded(Tournament tournament){}
        default void tournamentStarted(Tournament tournament){}
        default void tournamentClosed(Tournament tournament){}
        default void tournamentEnded(Tournament tournament){}

        default void onLoad(Instance instance){}
        default void onStart(Instance instance){}
        default void onClose(Instance instance){}
        default void onEnd(Instance instance){}

        default void onCreate(Entry entry){}
        default void onUpdate(Entry entry){}
    }
    interface Creator{
        Tournament create(String type,Schedule schedule);
        Tournament load(String tournamentId);
        Instance create(Tournament tournament);
        Entry create(String systemId,Instance instance);
    }
    interface Schedule{
        LocalDateTime startTime();
        LocalDateTime closeTime();
        LocalDateTime endTime();
        int maxEntriesPerInstance();
        int instanceDurationInMinutes();
    }

}
