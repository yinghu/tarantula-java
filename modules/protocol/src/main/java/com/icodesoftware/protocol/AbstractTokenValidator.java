package com.icodesoftware.protocol;

import com.icodesoftware.*;
import com.icodesoftware.protocol.presence.TROnSession;
import com.icodesoftware.protocol.presence.TRRole;
import com.icodesoftware.service.AccessKey;

import java.util.Map;

abstract public class AbstractTokenValidator implements TokenValidator {

    protected int ticketDuration;

    @Override
    public OnSession validateToken(String token) {
        return CryptoManager.verify(token);
    }

    @Override
    public String hashPassword(String password) {
        return CryptoManager.hash(password);
    }

    @Override
    public OnSession validatePassword(Access access, String password) {
        if(!CryptoManager.hash(password).equals(access.password())) return TROnSession.PASSWORD_NOT_MATCHED;
        return onSession(access);
    }

    @Override
    public String ticket(long systemId, long stub) {
        return CryptoManager.ticket(systemId,stub,ticketDuration);
    }

    @Override
    public boolean validateTicket(Session session) {
        return CryptoManager.validateTicket(session.systemId(),session.stub(),session.ticket());
    }

    @Override
    public OnSession validateTicket(String ticket){
        return CryptoManager.validateTicket(ticket);
    }

    @Override
    public void offSession(long systemId, long stub) {

    }

    @Override
    public boolean validateToken(Map<String, Object> params) {
        return onVendorToken(params);
    }

    @Override
    public boolean upgradeRole(Access access, String role) {
        return false;
    }

    @Override
    public Access.Role role(long systemId) {
        return TRRole.player;
    }

    public String accessKey(AccessKey accessKey){
        return CryptoManager.accessKey(accessKey);
    }
    public AccessKey validateAccessKey(String access){
        AccessKey accessKey = CryptoManager.validateAccessKey(access);
        return onAccessKey(accessKey);
    }
    abstract protected boolean onVendorToken(Map<String,Object> params);
    abstract protected OnSession onSession(Access access);
    abstract protected AccessKey onAccessKey(AccessKey accessKey);
}
