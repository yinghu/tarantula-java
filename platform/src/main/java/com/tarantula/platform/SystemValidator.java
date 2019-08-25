package com.tarantula.platform;

import com.tarantula.*;
import com.tarantula.platform.service.SystemValidatorProvider;
import com.tarantula.platform.util.SystemUtil;
import java.security.MessageDigest;


public class SystemValidator implements Serviceable{

    private DataStore dataStore;
    private ServiceContext tsc;
    private int timeoutMinutes;

    private MessageDigest _messageDigest;

    SystemValidatorProvider systemValidatorProvider;
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
    public void dataStore(DataStore dataStore){
        this.dataStore = dataStore;
    }
    public void systemValidatorProvider(SystemValidatorProvider systemValidatorProvider){
        this.systemValidatorProvider = systemValidatorProvider;
    }
    public void timeout(int timeoutMinutes){
        this.timeoutMinutes = timeoutMinutes;
    }

    public TokenValidator tokenValidator(){
        return new _TokenValidator(this);

    }

    public void setup(ServiceContext serviceContext){
        this.tsc = serviceContext;
    }

    private class _TokenValidator implements TokenValidator{

        private final SystemValidator _singleton;

        public _TokenValidator(SystemValidator _singleton){
            this._singleton = _singleton;

        }
        private  String token(OnSession presence,String clientId) {
           return SystemUtil.token(messageDigest(),presence,clientId,timeoutMinutes);
        }
        @Override
        public OnSession validToken(String token, String clientId) {
            return SystemUtil.validToken(this._singleton.messageDigest(),token,clientId);
        }
        @Override
        public String hashPassword(String password) {
            MessageDigest messageDigest = messageDigest();
            return SystemUtil.hashPassword(messageDigest,password);
        }
        @Override
        public OnSession validPassword(Access hash, String password, String clientId) {
            if((SystemUtil.hashPassword(messageDigest(),password)).equals(hash.password())){
                OnSession ox = new OnSessionTrack();
                ox.distributionKey(hash.key().asString());
                if(dataStore.load(ox)){
                    OnSession _ox = new OnSessionTrack();
                    _ox.distributionKey(ox.key().asString());
                    _ox.systemId(hash.distributionKey());
                    ox.stub(ox.stub()+1);
                    _ox.stub(ox.stub());
                    _ox.login(hash.login());
                    _ox.routingNumber(hash.routingNumber());
                    _ox.token(this.token(_ox,clientId));
                    _ox.ticket(this.ticket(hash.distributionKey(),ox.stub(),30));
                    _ox.successful(true);
                    ox.activeSessions(1);
                    ox.timestamp(System.currentTimeMillis());
                    dataStore.update(ox);
                    systemValidatorProvider.onSession(hash.key().asString());
                    return _ox;
                }
                else{
                    //max sessions reached
                    return OnSessionTrack.ON_SESSION_NOT_AVAILABLE;
                }
            }
            else{
                //return on failed password check
                return OnSessionTrack.PASSWORD_NOT_MATCHED;
            }
        }
        @Override
        public String ticket(String input, int stub, int durationSeconds) {
            return SystemUtil.ticket(messageDigest(),input,stub,durationSeconds);
        }
        @Override
        public boolean validTicket(String systemId, int stub, String ticket) {
            return SystemUtil.validTicket(messageDigest(),systemId,stub,ticket);
        }
        @Override
        public OnSession token(String systemId,int stub,int durationSeconds){
            OnSession onSession = new OnSessionTrack(systemId,stub);
            onSession.token(SystemUtil.token(messageDigest(),onSession,"clientId",durationSeconds));
            return onSession;
        }
        @Override
        public boolean onSession(String systemId, int stub, String trackId, String ticket) {
            return this.validTicket(systemId,stub,ticket);
        }
        @Override
        public void offSession(String systemId, int stub, String trackId) {
            OnSessionTrack offSession = new OnSessionTrack();
            offSession.distributionKey(systemId);
            if(dataStore.load(offSession)){
                offSession.activeSessions(-1);
                systemValidatorProvider.offSession(systemId);
                dataStore.update(offSession);
            }
        }
    }
}
