package com.tarantula.platform.service;

import com.tarantula.*;

public interface TokenValidatorProvider extends ServiceProvider {

    String NAME = "TokenValidatorProvider";
    String MDA = "SHA-1";

    TokenValidator tokenValidator();
    boolean validateAccessKey(String accessKey);
    String ticket(String key,int stub,int duration);
    boolean validateTicket(String key,int stub,String ticket);

    void timeout(int minutes,int seconds);

    Presence presence(String systemId);

    Access.Role role(String systemId);
}
