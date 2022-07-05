package com.icodesoftware.service;

import com.icodesoftware.*;

import java.util.List;
import java.util.Map;

public interface TokenValidatorProvider extends ServiceProvider {

    String NAME = "TokenValidatorProvider";
    String MDA = "SHA-1";

    TokenValidator tokenValidator();

    String hashJoinTicket(String roomId,String systemId);
    boolean validHash(String roomId,String systemId,String hash);

    //labeled access key
    String validateAccessKey(String accessKey);
    String createAccessKey(String label);
    void revokeAccessKey(String accessKey);

    String ticket(String key,int stub,int duration);
    boolean validateTicket(String key,int stub,String ticket);

    //game server register key on game cluster lobby tyeId
    <T extends OnAccess> T validateGameClusterAccessKey(String gameClusterId);
    String createGameClusterAccessKey(String gameClusterId);
    List<String> gameClusterAccessKeyList(String gameClusterId);


    void timeout(int minutes,int seconds);

    Presence presence(Session session);

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
        String clientId(String typeId);
        String secureKey();
        String authUri();
        String tokenUri();
        String certUri();
        String[] origins();
        void registerMetricsLister(MetricsListener metricsListener);
        void setup(ServiceContext serviceContext);
        boolean validate(Map<String,Object> params);
    }
}
