package com.icodesoftware.service;

import com.icodesoftware.Tournament;

import java.util.List;

public interface TournamentServiceProvider extends ConfigurationServiceProvider{

    void registerTournamentListener(Tournament.Listener listener);

    Tournament tournament(long tournamentId);
    List<Tournament> list();
    boolean available(long tournamentId);
    
    Tournament.RaceBoard list(String tournamentId,String instanceId);
    Tournament.RaceBoard list(String tournamentId);

    List<Tournament.History> playerHistory(String systemId);
    Tournament.Instance tournamentHistory(String tournamentId);
}
