package com.icodesoftware.protocol.presence;

import com.icodesoftware.Access;

public class TRRole implements Access.Role {

    public static final Access.Role root = new TRRole("root",Access.ROOT_ACCESS_CONTROL);
    public static final Access.Role admin = new TRRole("admin",Access.ADMIN_ACCESS_CONTROL);
    public static final Access.Role account = new TRRole("account",Access.ACCOUNT_ACCESS_CONTROL);
    public static final Access.Role player = new TRRole("player",Access.PLAYER_ACCESS_CONTROL);

    private final String name;
    private final int accessControl;

    public TRRole(final String name, final int accessControl){
        this.name = name;
        this.accessControl = accessControl;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public int accessControl() {
        return accessControl;
    }
    public String toString(){
        return "Role->"+name+"/"+accessControl;
    }
}
