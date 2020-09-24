package com.tarantula.platform.service;

import com.tarantula.*;

import java.util.List;
import java.util.Map;

public interface TokenValidatorProvider extends ServiceProvider {

    String NAME = "TokenValidatorProvider";
    String MDA = "SHA-1";

    TokenValidator tokenValidator();

    //labeled access key
    String validateAccessKey(String accessKey);
    String accessKey(String label);

    String ticket(String key,int stub,int duration);
    boolean validateTicket(String key,int stub,String ticket);

    //game server register key on game cluster lobby tyeId
    String validateGameClusterAccessKey(String gameClusterId);
    String gameClusterAccessKey(String gameClusterId);

    void timeout(int minutes,int seconds);

    Presence presence(String systemId);

    Access.Role role(String systemId);
    boolean checkRole(Access access,String role);
    boolean upgradeRole(Access access,String role);
    boolean grantAccess(Access access,Access owner);
    boolean revokeAccess(Access access);
    List<Access.Role> list();
    AuthVendor authVendor(String name);
    void onCheck(OnLobby onLobby);
    boolean checkSubscription(String systemId);
    int updateSubscription(String systemId,int months);
    interface AuthVendor{
        String name();
        String clientId();
        String secureKey();
        String authUri();
        String tokenUri();
        String certUri();
        String[] origins();
        void registerMetricsLister(MetricsListener metricsListener);
        boolean validate(Map<String,Object> params);
    }
}
