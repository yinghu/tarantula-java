package com.tarantula;

/**
 * Updated by yinghu 8/27/19
 */
public interface TokenValidator{

    String MDA = "SHA-1";

    //validate and parse the token, call before dispatching event
    OnSession validateToken(String token);

    //hash password to avoid direct password persistence
    String hashPassword(String password);


    OnSession validatePassword(Access hash,String password);

    //generate the ticket with limited life circle
    String ticket(String systemId,int stub);

    //validate ticket, call before application callback
    boolean validateTicket(String systemId,int stub,String ticket);


    OnSession token(String systemId,int stub);

    void offSession(String systemId,int stub);
}
