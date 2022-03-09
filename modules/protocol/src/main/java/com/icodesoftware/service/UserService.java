package com.icodesoftware.service;

import com.icodesoftware.Access;
import com.icodesoftware.Account;
import com.icodesoftware.OnAccess;
import com.icodesoftware.Subscription;

public interface UserService extends ServiceProvider{

    String NAME = "UserService";

    Access createUser(OnAccess access);
    Account createOrUpdateAccount(Access access, Subscription subscription);
}
