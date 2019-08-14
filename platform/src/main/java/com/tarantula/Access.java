package com.tarantula;


/**
 * Updated by yinghu on 03/22/2018.
 */
public interface Access extends Recoverable {

    String login();
    void login(String login);
    String password();
    void password(String password);
    boolean active();
    void active(boolean active);

    int routingNumber();
    void routingNumber(int routingNumber);
}
