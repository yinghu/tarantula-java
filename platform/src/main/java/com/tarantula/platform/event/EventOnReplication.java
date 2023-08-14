package com.tarantula.platform.event;

import com.icodesoftware.Event;
import com.icodesoftware.service.OnReplication;
import com.tarantula.platform.service.persistence.ScopedOnReplication;

public interface EventOnReplication extends Event {

    boolean offer(ScopedOnReplication scopedOnReplication);
    void drain();

    OnReplication[] data();

    void drop();
}
