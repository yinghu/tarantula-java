package com.icodesoftware;


public interface OnSession extends OnApplication{

    String LABEL = "onSession";

    String DataStore = "tarantula_session";
    String token();
    void token(String token);

    String login();
    void login(String login);

    String role();

    String home();
    void home(String home);
}
