package com.tarantula.platform.service;

import com.tarantula.*;

public interface TokenValidatorProvider extends ServiceProvider {

    String NAME = "TokenValidatorProvider";

    TokenValidator tokenValidator();

    void timeout(int minutes,int seconds);

    Presence presence(String systemId);

    Access.Role role(String systemId);
}
