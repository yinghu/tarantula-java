package com.tarantula;

/**
 * Updated by yinghu 6/17/19
 */
public interface TokenValidator{

    String MDA = "SHA-1";

    OnSession validToken(String token,String clientId);
    String hashPassword(String password);
    OnSession validPassword(Access hash,String password,String clientId);
    String ticket(String systemId,int stub,int durationSeconds);
    boolean validTicket(String systemId,int stub,String ticket);
    OnSession token(String systemId,int stub,int durationSeconds);
    boolean onSession(String systemId,int stub,String trackId,String ticket);
    void offSession(String systemId,int stub,String trackId);
}
