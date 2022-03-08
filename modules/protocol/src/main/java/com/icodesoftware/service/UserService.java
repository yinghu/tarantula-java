package com.icodesoftware.service;

import com.icodesoftware.Access;
import com.icodesoftware.Account;
import com.icodesoftware.OnAccess;

public interface UserService extends ServiceProvider{

    String NAME = "UserService";

    Access createUser(OnAccess access);
    boolean createAccount(Account account);
    //boolean subscribe();
}
