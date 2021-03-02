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
    void score(String systemId,OnInstance onInstance);
    void registerListener(Listener listener);
    void registerCreator(Creator creator);
    Listener listener();

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
        void tournamentStarted(Tournament tournament);
        void tournamentClosed(Tournament tournament);
        void tournamentEnded(Tournament tournament);

        void onStart(Instance instance);
        void onClose(Instance instance);
        void onEnd(Instance instance);
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
    interface OnInstance{
        void on(Entry entry);
    }
}
