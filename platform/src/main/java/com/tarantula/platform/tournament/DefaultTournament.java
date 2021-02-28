package com.tarantula.platform.tournament;

import com.icodesoftware.Tournament;
import com.icodesoftware.util.RecoverableObject;

import java.util.concurrent.ConcurrentHashMap;

public class DefaultTournament extends RecoverableObject implements Tournament {

    private String type;
    private Listener listener;
    private Creator creator;
    private ConcurrentHashMap<String,Instance> instanceIndex = new ConcurrentHashMap<>();
    public DefaultTournament(String type,Creator creator){
        this.type = type;
        this.creator = creator;
    }

    @Override
    public String type() {
        return type;
    }

    @Override
    public Instance join(String systemId) {
        Instance instance = creator.instance();
        instance.enter(creator.entry(systemId));
        instanceIndex.put(instance.id(),instance);
        return instance;
    }
    @Override
    public void score(String systemId,OnInstance onInstance){
        
    }
    @Override
    public void registerListener(Listener listener){
        this.listener = listener;
    }
}
