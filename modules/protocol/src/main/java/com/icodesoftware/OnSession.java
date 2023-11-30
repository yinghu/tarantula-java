package com.icodesoftware;

public interface OnSession extends OnApplication, Response {

    String LABEL = "onSession";

    String DataStore = "tarantula_session";
    String token();
    void token(String token);

    String login();
    void login(String login);

    int tournamentSlot();
    void onTournament(int tournamentSlot,long tournamentId);

}
