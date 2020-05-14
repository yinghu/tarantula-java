package com.tarantula;

/**
 * Updated by yinghu on 8/26/2019.
 */
public interface Access extends Recoverable {

    //application access level control
    int PUBLIC_ACCESS_MODE = 10;
    int PROTECT_ACCESS_MODE = 12;
    int PRIVATE_ACCESS_MODE = 13;

    //LOWEST O - HIGHEST 100
    int PLAYER_ACCESS_CONTROL = 0;//manage own assets on app

    int OWNER_PLAYER_CONTROL = 20;//manage own assets by account

    int OWNER_ADMIN_CONTROL = 30;//manage own assets by account


    int ROOT_ACCESS_CONTROL = 100;//manage all assets with super permission

    String login();
    void login(String login);
    String password();
    void password(String password);

    String emailAddress();
    void emailAddress(String emailAddress);

    boolean active();
    void active(boolean active);
    boolean validated();//validated to skip check password if user login from third party login token

    String validator();

    String role();
    void role(String role);

    interface Role{
        String name();
        int accessControl();
    }

}
