package com.tarantula.integration.udp;

import java.util.concurrent.CopyOnWriteArraySet;

public class SessionGroup {
    public String instanceId;
    public CopyOnWriteArraySet<Session> sessions;

    public SessionGroup(String instanceId){
        this.instanceId = instanceId;
        sessions = new CopyOnWriteArraySet<>();
    }
}
