package com.icodesoftware.service;

import com.icodesoftware.Tournament;

import java.util.List;

public interface TournamentServiceProvider extends ConfigurationServiceProvider{

    void registerTournamentListener(Tournament.Listener listener);

    List<Tournament> list();
    boolean available(String tournamentId);
    Tournament.Instance enter(String tournamentId,String systemId);
    Tournament.Entry score(String instanceId,String systemId, double delta);

    void finish(String instanceId,String systemId);

    Tournament.RaceBoard list(String instanceId);

    List<Tournament.History> playerHistory(String systemId);
    Tournament.Instance tournamentHistory(String tournamentId);
}
