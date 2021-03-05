package com.icodesoftware.service;

import com.icodesoftware.Tournament;


public interface TournamentServiceProvider extends ServiceProvider{

    void registerCreator(Tournament.Creator creator);
    void registerListener(Tournament.Listener listener);

    Tournament register(String type,Tournament.Schedule schedule);
    Tournament tournament(String tournamentId);

    Tournament.Instance instance(String instanceId);

    void reload();

}
