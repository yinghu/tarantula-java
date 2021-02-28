package com.icodesoftware;

import java.time.LocalDateTime;
import java.util.List;

public interface Tournament extends Recoverable{

    String type();
    Instance join(String systemId);
    void score(String systemId,OnInstance onInstance);
    void registerListener(Listener listener);


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
        void enter(Entry entry);
        Entry entry(String systemId);
        List<Entry> list();
    }
    interface Listener{
        void onStart(Instance instance);
        void onClose(Instance instance);
        void onEnd(Instance instance);
    }
    interface Creator{
        Tournament tournament(String type);
        Instance instance();
        Entry entry(String systemId);
    }
    interface Schedule{
        LocalDateTime startTime();
        LocalDateTime endTime();
        int instanceDurationInMinutes();
    }
    interface OnInstance{
        void on(Entry entry);
    }
}
