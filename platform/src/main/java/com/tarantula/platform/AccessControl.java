package com.tarantula.platform;

import com.tarantula.Access;

public class AccessControl implements Access.Role {

    public static final Access.Role root = new AccessControl("root",Access.ROOT_ACCESS_CONTROL);
    public static final Access.Role admin = new AccessControl("admin",Access.OWNER_ADMIN_CONTROL);
    public static final Access.Role owner = new AccessControl("owner",Access.OWNER_PLAYER_CONTROL);
    public static final Access.Role player = new AccessControl("player",Access.PLAYER_ACCESS_CONTROL);

    private final String name;
    private final int accessControl;

    public AccessControl(final String name,final int accessControl){
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
