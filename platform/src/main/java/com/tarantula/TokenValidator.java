package com.tarantula;

import java.util.Map;

/**
 * Updated by yinghu lu 5/7/2020
 */
//user application authentication utility API
public interface TokenValidator{


    //validate and parse the token, call before dispatching event
    OnSession validateToken(String token);
    OnSession token(String systemId,int stub);

    //hash password to avoid direct password persistence
    String hashPassword(String password);
    OnSession validatePassword(Access hash,String password);

    //generate the ticket with limited life circle
    String ticket(String systemId,int stub);
    //validate ticket, call before application callback
    boolean validateTicket(String systemId,int stub,String ticket);


    void offSession(String systemId,int stub);


    boolean validateToken(Map<String,Object> params);

    boolean upgradeRole(Access access,String role);

}
