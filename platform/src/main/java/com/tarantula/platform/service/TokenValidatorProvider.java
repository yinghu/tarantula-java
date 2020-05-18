package com.tarantula.platform.service;

import com.tarantula.*;

import java.util.List;
import java.util.Map;

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

    boolean upgradeRole(Access access,String role);

    List<ApplicationCluster> list(String systemId);
    List<Access.Role> list();
    AuthVendor authVendor(String name);

    interface AuthVendor{
        String name();
        String clientId();
        String secureKey();
        String authUri();
        String tokenUri();
        String certUri();
        String[] origins();

        boolean validate(Map<String,Object> params);
    }
}
