package com.tarantula;

/**
 * Updated by yinghu on 8/26/2019.
 */
public interface Access extends Recoverable {

    int PUBLIC_ACCESS_MODE = 10;
    int PROTECT_ACCESS_MODE = 11;
    int FORWARD_ACCESS_MODE = 12;
    int PRIVATE_ACCESS_MODE = 13;

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
