package com.tarantula.platform.presence;

import com.icodesoftware.service.LoginProvider;
import com.icodesoftware.util.RecoverableObject;


public class ThirdPartyLogin extends RecoverableObject implements LoginProvider {


    public ThirdPartyLogin(){

    }
    public ThirdPartyLogin(String provider,String password,String deviceId){
        this.owner = provider;
        this.index = password;
        this.name = deviceId;
    }
    @Override
    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PresencePortableRegistry.THIRD_PARTY_LOGIN_CID;
    }

    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeUTF8(owner);
        buffer.writeUTF8(index);
        buffer.writeUTF8(name);
        return true;
    }

    @Override
    public boolean read(DataBuffer buffer) {
        owner = buffer.readUTF8();
        index = buffer.readUTF8();
        name = buffer.readUTF8();
        return true;
    }

    public String provider(){
        return owner;
    }
    public String password(){
        return index;
    }

    @Override
    public String clientId() {
        return name;
    }
}
