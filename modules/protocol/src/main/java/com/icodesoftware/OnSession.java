package com.icodesoftware;

public interface OnSession extends OnApplication, Response,DataStore.Updatable {

    String LABEL = "onSession";

    String DataStore = "tarantula_session";
    String token();
    void token(String token);

    String login();
    void login(String login);


}
