package com.icodesoftware.service;

import com.icodesoftware.DataStore;
import com.icodesoftware.OnApplication;


public interface LoginProvider extends OnApplication, DataStore.Updatable {

    String DataStore = "tarantula_login_provider";

    String provider();
    String password();
    String deviceId();
    void deviceId(String deviceId);

    String thirdPartyToken();

    void setThirdPartyToken(String thirdPartyToken);
}
