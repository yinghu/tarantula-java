package com.icodesoftware.service;

import com.icodesoftware.EventListener;

public interface RequestHandler extends EventListener, ServiceProvider {

    String HEALTH_CHECK_PATH = "/health";

    String ACCOUNT_PATH = "/account";
    String ADMIN_PATH = "/admin";
    String DEVELOPMENT_PATH = "/development";
    String GAME_SERVER_PATH = "/server";
    String PRESENCE_PATH = "/presence";
    String PUSH_PATH = "/push";
    String RESOURCE_PATH = "/resource";
    String ROOT_PATH = "/";
    String SERVICE_PATH = "/service";
    String SUDO_PATH = "/sudo";
    String UPLOAD_PATH = "/upload";
    String USER_PATH = "/user";
    String VIEW_PATH = "/view";

    String BACKUP_PATH = "/backup";

    //String name();
    void onRequest(OnExchange exchange) throws Exception;
    //void setup(ServiceContext tcx);
    default void onCheck(){}
    default boolean deployable(){return false;}
}
