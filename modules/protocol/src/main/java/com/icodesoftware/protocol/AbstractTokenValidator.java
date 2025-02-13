package com.icodesoftware.protocol;

import com.icodesoftware.Access;
import com.icodesoftware.OnSession;
import com.icodesoftware.Session;
import com.icodesoftware.TokenValidator;
import com.icodesoftware.protocol.session.OnSessionTrack;

import java.util.Map;

public class AbstractTokenValidator implements TokenValidator {

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
        if(!CryptoManager.hash(password).equals(access.password())) return OnSessionTrack.PASSWORD_NOT_MATCHED;
        OnSessionTrack onSessionTrack = new OnSessionTrack();
        return onSessionTrack;
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
    public void offSession(long systemId, long stub) {

    }

    @Override
    public boolean validateToken(Map<String, Object> params) {
        return false;
    }

    @Override
    public boolean upgradeRole(Access access, String role) {
        return false;
    }

    @Override
    public Access.Role role(long systemId) {
        return null;
    }
}
