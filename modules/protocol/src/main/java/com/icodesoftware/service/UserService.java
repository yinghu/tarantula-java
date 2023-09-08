package com.icodesoftware.service;

import com.icodesoftware.*;

import java.util.List;

public interface UserService extends ServiceProvider,MetricsListener{

    String NAME = "UserService";

    Access loadUser(long systemId);

    Account loadAccount(Access access);
    List<Access> loadUsers(Account account);

    List<Access> loadUsers(Access access);

    //<T extends OnAccess> List<T> gameClusterList(Access access);

    Subscription loadSubscription(Account account);
    Subscription loadSubscription(Access access);

    Access createUser(OnAccess access);
    Access createUser(Account account,OnAccess access);
    boolean changePassword(OnAccess access);
    boolean updateEmail(OnAccess access);
    Account createAccount(Access access, Subscription subscription);
    Subscription subscribe(Account accountId,int durationMonth);

    LoginProvider loginProvider(long systemId);
    void createLoginProvider(LoginProvider loginProvider);
}
