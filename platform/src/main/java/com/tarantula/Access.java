package com.tarantula;

/**
 * Updated by yinghu on 8/26/2019.
 */
public interface Access extends Recoverable {

    int PUBLIC_ACCESS_MODE = 10;
    int PROTECT_ACCESS_MODE = 12;
    int PRIVATE_ACCESS_MODE = 13;

    //LOWEST O - HIGHEST 100
    int PLAYER_ACCESS_CONTROL = 0;
    int ADMIN_ACCESS_CONTROL = 10;

    int ROOT_ACCESS_CONTROL = 100;//MAX ACCESS CONTROL

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
