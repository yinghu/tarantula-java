package com.tarantula.platform;

import com.tarantula.*;
import com.tarantula.platform.service.SystemValidatorProvider;
import com.tarantula.platform.util.SystemUtil;
import java.security.MessageDigest;


public class SystemValidator implements Serviceable{

    private int timeoutMinutes;
    private int timeoutSeconds =10;
    private MessageDigest _messageDigest;

    private SystemValidatorProvider systemValidatorProvider;

    private MessageDigest messageDigest(){
        try{
            return (MessageDigest)this._messageDigest.clone();
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public void start() throws Exception{
        this._messageDigest = MessageDigest.getInstance(TokenValidator.MDA);
    }

    @Override
    public void shutdown() throws Exception {

    }

    public void systemValidatorProvider(SystemValidatorProvider systemValidatorProvider){
        this.systemValidatorProvider = systemValidatorProvider;
    }
    public void timeout(int timeoutMinutes,int timeoutSeconds){
        this.timeoutMinutes = timeoutMinutes;
        this.timeoutSeconds = timeoutSeconds;
    }

    public TokenValidator tokenValidator(){
        return new _TokenValidator(this);

    }

    private class _TokenValidator implements TokenValidator{

        private final SystemValidator _singleton;

        public _TokenValidator(SystemValidator _singleton){
            this._singleton = _singleton;

        }
        @Override
        public OnSession validateToken(String token) {
            return SystemUtil.validToken(this._singleton.messageDigest(),token);
        }
        @Override
        public String hashPassword(String password) {
            MessageDigest messageDigest = messageDigest();
            return SystemUtil.hashPassword(messageDigest,password);
        }
        @Override
        public OnSession validatePassword(Access access, String password) {
            if((SystemUtil.hashPassword(messageDigest(),password)).equals(access.password())){
                Presence presence = systemValidatorProvider.presence(access.distributionKey());
                OnSession _ox = new OnSessionTrack();
                _ox.systemId(access.distributionKey());
                _ox.stub(presence.count(1));
                _ox.login(access.login());
                _ox.routingNumber(access.routingNumber());
                _ox.token(SystemUtil.token(messageDigest(),access.distributionKey(),_ox.stub(),timeoutMinutes));
                _ox.ticket(this.ticket(access.distributionKey(),_ox.stub()));
                _ox.successful(true);
                System.out.println(presence.toString());
                return _ox;
            }
            else{
                //return on failed password check
                return OnSessionTrack.PASSWORD_NOT_MATCHED;
            }
        }
        @Override
        public String ticket(String input, int stub) {
            return SystemUtil.ticket(messageDigest(),input,stub,timeoutSeconds);
        }
        @Override
        public boolean validateTicket(String systemId, int stub, String ticket) {
            return SystemUtil.validTicket(messageDigest(),systemId,stub,ticket);
        }
        @Override
        public OnSession token(String systemId,int stub){
            OnSession onSession = new OnSessionTrack(systemId,stub);
            onSession.token(SystemUtil.token(messageDigest(),systemId,stub,timeoutMinutes));
            return onSession;
        }
        @Override
        public void offSession(String systemId, int stub) {
            systemValidatorProvider.offSession(systemId);
        }
    }
}
