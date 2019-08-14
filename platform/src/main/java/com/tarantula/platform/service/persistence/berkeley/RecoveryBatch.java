package com.tarantula.platform.service.persistence.berkeley;

import com.tarantula.Event;

import java.util.concurrent.atomic.AtomicBoolean;


public class RecoveryBatch {
    public AtomicBoolean checked;
    public Event[] pending;
    public RecoveryBatch(){
        pending = new Event[100];
        checked = new AtomicBoolean(false);
    }
}
