package com.tarantula.platform.tournament;

import com.icodesoftware.Tournament;
import com.icodesoftware.util.RecoverableObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class TournamentInstance extends RecoverableObject implements Tournament.Instance {

    private ConcurrentHashMap<String, Tournament.Entry> entryIndex = new ConcurrentHashMap<>();
    private String id ="t10001";
    public TournamentInstance(){
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public void enter(Tournament.Entry entry) {
        this.entryIndex.putIfAbsent(entry.systemId(),entry);
    }

    @Override
    public Tournament.Entry entry(String systemId) {
        return entryIndex.get(systemId);
    }

    @Override
    public List<Tournament.Entry> list() {
        ArrayList<Tournament.Entry> entries = new ArrayList<>();
        entryIndex.forEach((k,v)->{
            entries.add(v);
        });
        return entries;
    }
}
