package com.icodesoftware.service;

import com.icodesoftware.*;

import java.util.List;
import java.util.Map;

public interface TokenValidatorProvider extends ServiceProvider,Resettable {

    String NAME = "TokenValidatorProvider";
    String MDA = "SHA-1";

    TokenValidator tokenValidator();

    byte[] clusterKey(String clusterNameSuffix);
    byte[] tokenKey(String clusterNameSuffix);
    boolean enablePresenceService(String root,String password,String clusterNameSuffix,String presenceServiceHost);
    void disablePresenceService(String clusterNameSuffix);

    boolean resetClusterKey();

    //labeled access key
    String validateAccessKey(String accessKey);
    String createAccessKey(String label);
    List<OnAccess> accessKeyList();
    void revokeAccessKey(String accessKey);

    String ticket(long key,int stub,int duration);
    boolean validateTicket(long key,int stub,String ticket);

    //game server register key on game cluster lobby tyeId
    <T extends OnAccess> T validateGameClusterAccessKey(String gameClusterId);
    String createGameClusterAccessKey(long gameClusterId);
    List<String> gameClusterAccessKeyList(long gameClusterId);


    void timeout(int minutes,int seconds);

    Presence presence(Session session);
    //void updateVendorAccessToken(String systemId,String accessToken);

    Access.Role role(long systemId);
    boolean checkRole(Access access,String role);
    boolean upgradeRole(Access access,String role);
    boolean grantAccess(Access access,Access owner);
    boolean revokeAccess(Access access);
    List<Access.Role> list();
    AuthVendor authVendor(String name);
    void registerAuthVendor(String provider,AuthVendor authVendor);
    void releaseAuthVendor(String provider,AuthVendor authVendor);
    void onCheck(OnLobby onLobby);
    boolean checkSubscription(String systemId);
    int updateSubscription(String systemId,int months);
    interface AuthVendor{
        String name();
        String typeId();
        String clientId();

        void registerMetricsLister(MetricsListener metricsListener);
        void setup(ServiceContext serviceContext);
        boolean validate(Map<String,Object> params);
        boolean upload(String context,byte[] content);
    }
}
