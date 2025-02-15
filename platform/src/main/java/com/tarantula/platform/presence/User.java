package com.tarantula.platform.presence;

import com.icodesoftware.protocol.presence.AbstractAccess;

public class User extends AbstractAccess {


    public User(){
        super();
    }
    public User(String login,boolean validated,String validator){
        this();
        this.login = login;
        this.validated = validated;
        this.validator = validator;
    }

    public int getFactoryId() {
        return UserPortableRegistry.OID;
    }

    public int getClassId() {
        return UserPortableRegistry.USER_CID;
    }


}
