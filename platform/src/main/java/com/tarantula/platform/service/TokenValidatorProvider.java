package com.tarantula.platform.service;

import com.tarantula.Presence;
import com.tarantula.ServiceProvider;
import com.tarantula.TokenValidator;

public interface TokenValidatorProvider extends ServiceProvider {
    String NAME = "TokenValidatorProvider";
    TokenValidator tokenValidator();
    void timeout(int minutes);
    Presence presence(String systemId);
}
