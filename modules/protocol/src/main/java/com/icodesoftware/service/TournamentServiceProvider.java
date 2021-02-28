package com.icodesoftware.service;

import com.icodesoftware.Tournament;


public interface TournamentServiceProvider {

    void registerCreator(Tournament.Creator creator);
    boolean register(String type,Tournament.Schedule schedule,Tournament.Listener listener);
    Tournament tournament(String type);
}
