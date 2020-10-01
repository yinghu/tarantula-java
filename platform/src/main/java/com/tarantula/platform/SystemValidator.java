package com.tarantula.platform;

import com.icodesoftware.OnSession;
import com.tarantula.*;
import com.tarantula.platform.service.Metrics;
import com.tarantula.platform.service.SystemValidatorProvider;
import com.tarantula.platform.service.TokenValidatorProvider;
import com.tarantula.platform.util.SystemUtil;
import java.security.MessageDigest;
import java.util.Map;

//hook system validator provider and token validator
public class SystemValidator{

    private int timeoutMinutes;
    private int timeoutSeconds =10;
    private SystemValidatorProvider systemValidatorProvider;

    public void systemValidatorProvider(SystemValidatorProvider systemValidatorProvider){
        this.systemValidatorProvider = systemValidatorProvider;
    }
    public void timeout(int timeoutMinutes,int timeoutSeconds){
        this.timeoutMinutes = timeoutMinutes;
        this.timeoutSeconds = timeoutSeconds;
    }

    public TokenValidator tokenValidator(){
        return new _TokenValidator();
    }

    private class _TokenValidator implements TokenValidator{

        @Override
        public OnSession validateToken(String token) {
            return SystemUtil.validToken(systemValidatorProvider.messageDigest(),token);
        }
        @Override
        public String hashPassword(String password) {
            MessageDigest messageDigest = systemValidatorProvider.messageDigest();
            return SystemUtil.hashPassword(messageDigest,password);
        }
        @Override
        public OnSession validatePassword(Access access, String password) {
            if(SystemUtil.hashPassword(systemValidatorProvider.messageDigest(),password).equals(access.password())){
                Presence presence = systemValidatorProvider.presence(access.distributionKey());
                OnSession _ox = new OnSessionTrack();
                _ox.systemId(access.distributionKey());
                _ox.stub(presence.count(1));
                _ox.login(access.login());
                _ox.routingNumber(access.routingNumber());
                _ox.token(SystemUtil.token(systemValidatorProvider.messageDigest(),access.distributionKey(),_ox.stub(),timeoutMinutes));
                _ox.ticket(this.ticket(access.distributionKey(),_ox.stub()));
                _ox.successful(true);
                return _ox;
            }
            else{
                //return on failed password check
                return OnSessionTrack.PASSWORD_NOT_MATCHED;
            }
        }
        @Override
        public String ticket(String input, int stub) {
            return SystemUtil.ticket(systemValidatorProvider.messageDigest(),input,stub,timeoutSeconds);
        }
        @Override
        public boolean validateTicket(String systemId, int stub, String ticket) {
            Presence ptx = systemValidatorProvider.presence(systemId);
            if(stub!=ptx.count(0)){
                return false;
            }
            return SystemUtil.validTicket(systemValidatorProvider.messageDigest(),systemId,stub,ticket);
        }
        @Override
        public OnSession token(String systemId,int stub){
            OnSession onSession = new OnSessionTrack(systemId,stub);
            onSession.token(SystemUtil.token(systemValidatorProvider.messageDigest(),systemId,stub,timeoutMinutes));
            return onSession;
        }
        @Override
        public void offSession(String systemId, int stub) {
            systemValidatorProvider.offSession(systemId);
        }
        @Override
        public boolean validateToken(Map<String,Object> params){
            String _vname = (String)params.remove("name");
            if(_vname==null){
                return false;
            }
            TokenValidatorProvider.AuthVendor authVendor = systemValidatorProvider.authVendor(_vname);
            return authVendor.validate(params);
        }
        public boolean upgradeRole(Access access,String role){
            return systemValidatorProvider.upgradeRole(access,role);
        }
    }
}
