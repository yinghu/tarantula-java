package com.tarantula;

/**
 * Updated by yinghu lu on 6/17/2019.
 */
public interface OnSession extends OnApplication,Response{

    String token();
    void token(String token);

    String login();
    void login(String login);

    String ticket();
    void ticket(String ticket);
}
