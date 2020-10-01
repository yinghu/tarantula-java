package com.icodesoftware;

/**
 * Updated by yinghu lu on 6/17/2019.
 */
public interface OnSession extends OnApplication, Response {

    String DataStore = "session";

    String token();
    void token(String token);

    String login();
    void login(String login);

    String ticket();
    void ticket(String ticket);
}
