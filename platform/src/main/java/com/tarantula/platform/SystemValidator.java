package com.tarantula.platform;

import com.icodesoftware.*;
import com.icodesoftware.service.TokenValidatorProvider;
import com.tarantula.platform.service.SystemValidatorProvider;
import com.tarantula.platform.util.SystemUtil;

import java.nio.ByteBuffer;
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
            OnSession onSession = SystemUtil.validToken(systemValidatorProvider.messageDigest(),token);
            //byte[] mark = systemValidatorProvider.encrypt(ByteBuffer.allocate(4).putInt(onSession.stub()).array());
            //String wmark = SystemUtil.toHexString(mark);
            //if(!wmark.equals(onSession.label())) throw new RuntimeException("Illegal access");
            return onSession;
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
                byte[] mark = systemValidatorProvider.encrypt(ByteBuffer.allocate(4).putInt(_ox.stub()).array());
                _ox.token(SystemUtil.token(systemValidatorProvider.messageDigest(),access.distributionKey(),_ox.stub(),timeoutMinutes,SystemUtil.toHexString(mark)));
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
        public String ticket(String input, int stub) {//short live ticket
            return systemValidatorProvider.ticket(input,stub,timeoutSeconds);
        }
        @Override
        public boolean validateTicket(Session session) {
            Presence ptx = systemValidatorProvider.presence(session);
            String waterMark = SystemUtil.validTicket(systemValidatorProvider.messageDigest(),session.systemId(),session.stub(),session.ticket());
            byte[] data = ByteBuffer.allocate(4).putInt(session.stub()).array();
            byte[] mark = ptx.local()?systemValidatorProvider.encrypt(data) : systemValidatorProvider.encryptFromRemoteKey(data);
            return SystemUtil.toHexString(mark).equals(waterMark);
        }

        @Override
        public void offSession(String systemId, int stub) {
            systemValidatorProvider.offSession(systemId);
        }
        @Override
        public boolean validateToken(Map<String,Object> params){
            String _vname = (String)params.remove("provider");
            if(_vname==null) return false;
            TokenValidatorProvider.AuthVendor authVendor = systemValidatorProvider.authVendor(_vname);
            if(authVendor==null) return false;
            return authVendor.validate(params);
        }
        public boolean upgradeRole(Access access,String role){
            return systemValidatorProvider.upgradeRole(access,role);
        }
        public Access.Role role(String systemId){
            return systemValidatorProvider.role(systemId);
        }
    }
}
