package com.icodesoftware.service;

import com.icodesoftware.Recoverable;

public interface LoginProvider extends Recoverable {

    String DataStore = "login_provider";

    String provider();
    String password();
    String clientId();
}
