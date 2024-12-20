package com.tarantula.platform.presence;

import com.icodesoftware.service.LoginProvider;
import com.tarantula.platform.OnApplicationHeader;


public class ThirdPartyLogin extends OnApplicationHeader implements LoginProvider {


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
        buffer.writeLong(stub);
        buffer.writeLong(timestamp);
        return true;
    }

    @Override
    public boolean read(DataBuffer buffer) {
        owner = buffer.readUTF8();
        index = buffer.readUTF8();
        name = buffer.readUTF8();
        try{
            stub = buffer.readLong();
            timestamp = buffer.readLong();
        }catch (Exception ex){
            //ignore
        }
        return true;
    }

    public String provider(){
        return owner;
    }
    public String password(){
        return index;
    }

    @Override
    public String deviceId() {
        return name;
    }
    public void deviceId(String deviceId){
        this.name = deviceId;
    }
}
