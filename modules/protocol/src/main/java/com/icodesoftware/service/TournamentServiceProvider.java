package com.icodesoftware.service;

import com.icodesoftware.Tournament;

import java.util.List;

public interface TournamentServiceProvider extends ConfigurationServiceProvider{

    void registerTournamentListener(Tournament.Listener listener);

    Tournament tournament(long tournamentId);
    List<Tournament> list();
    List<Tournament> list(String type);

    boolean available(long tournamentId);

    //List<Tournament.History> playerHistory(String systemId);
    //Tournament.Instance tournamentHistory(String tournamentId);
}
