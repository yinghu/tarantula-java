package com.icodesoftware;

import com.icodesoftware.service.AccessKey;

import java.util.Map;

public interface TokenValidator {


    //validate and parse the token, call before dispatching event
    OnSession validateToken(String token);

    //hash password to avoid direct password persistence
    String hashPassword(String password);
    OnSession validatePassword(Access access, String password);

    //generate the ticket with limited life circle
    String ticket(long id,long stub);
    //validate ticket, call before application callback
    boolean validateTicket(Session session);
    default OnSession validateTicket(String ticket){ throw new UnsupportedOperationException();}
    default void checkPermission(Event pending){}
    void offSession(long id,long stub);

    boolean validateToken(Map<String,Object> params);

    boolean upgradeRole(Access access,String role);

    Access.Role role(long systemId);

    String accessKey(AccessKey accessKey);
    AccessKey validateAccessKey(String access);

}
