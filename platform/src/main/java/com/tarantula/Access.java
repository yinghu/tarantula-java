package com.tarantula;


/**
 * Updated by yinghu on 8/26/2019.
 */
public interface Access extends Recoverable {


    String login();
    void login(String login);
    String password();
    void password(String password);
    boolean active();
    void active(boolean active);
    String role();
    void role(String role);

    interface Role{
        String name();
        int accessControl();
    }

}
