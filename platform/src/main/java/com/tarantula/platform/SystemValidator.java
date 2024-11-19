package com.tarantula.platform;

import com.icodesoftware.*;
import com.icodesoftware.service.TokenValidatorProvider;
import com.tarantula.platform.service.SystemValidatorProvider;
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

    private class _TokenValidator implements TokenValidator {

        @Override
        public OnSession validateToken(String token) {
            return systemValidatorProvider.jwtToken(token);
        }
        @Override
        public String hashPassword(String password) {
            MessageDigest messageDigest = systemValidatorProvider.messageDigest();
            return SystemUtil.hashPassword(messageDigest,password);
        }
        @Override
        public OnSession validatePassword(Access access, String password) {
            if(SystemUtil.hashPassword(systemValidatorProvider.messageDigest(),password).equals(access.password())){
                Presence presence = systemValidatorProvider.presence(access.distributionId());
                OnSession _ox = presence.stub();
                if(!_ox.successful()) return _ox;
                //_ox.stub();
                _ox.login(access.login());
                _ox.routingNumber(access.routingNumber());
                _ox.token(systemValidatorProvider.jwtToken(access,_ox));
                _ox.successful(true);
                return _ox;
            }
            else
            {
                //return on failed password check
                return OnSessionTrack.PASSWORD_NOT_MATCHED;
            }
        }
        @Override
        public String ticket(long input, long stub) {//short live ticket
            return systemValidatorProvider.ticket(input,stub,timeoutSeconds);
        }
        @Override
        public boolean validateTicket(Session session) {
            return systemValidatorProvider.validateTicket(session.distributionId(),session.stub(),session.ticket());
        }

        @Override
        public void offSession(long systemId, long stub) {
            systemValidatorProvider.offSession(systemId,stub);
        }
        @Override
        public boolean validateToken(Map<String,Object> params){
            String _vname = (String)params.remove(OnAccess.PROVIDER);
            if(_vname==null) {
                params.put(OnAccess.STORE_MESSAGE,"Third party name not provided");
                return false;
            }
            TokenValidatorProvider.AuthVendor authVendor = systemValidatorProvider.authVendor(_vname);
            if(authVendor==null) {
                params.put(OnAccess.STORE_MESSAGE,"Third party ["+_vname+"] not existed");
                return false;
            }
            return authVendor.validate(params);
        }
        public boolean upgradeRole(Access access,String role){
            return systemValidatorProvider.upgradeRole(access,role);
        }
        public Access.Role role(long systemId){
            return systemValidatorProvider.role(systemId);
        }
    }
}
