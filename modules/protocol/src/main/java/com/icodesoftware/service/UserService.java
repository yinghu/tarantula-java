package com.icodesoftware.service;

import com.icodesoftware.*;

import java.util.List;

public interface UserService extends ServiceProvider,MetricsListener{

    String NAME = "UserService";

    Access loadUser(String systemId);

    Account loadAccount(Access access);
    List<Access> loadUsers(Account account);

    List<Access> loadUsers(Access access);

    <T extends Recoverable> T loadGameClusterIndex(Access access);

    Subscription loadSubscription(Account account);
    Subscription loadSubscription(Access access);

    Access createUser(OnAccess access);
    Access createUser(String accountId,OnAccess access);
    boolean changePassword(OnAccess access);
    boolean updateEmail(OnAccess access);
    Account createAccount(Access access, Subscription subscription);
    Subscription subscribe(String accountId,int durationMonth);

    LoginProvider loginProvider(String systemId);
    void createLoginProvider(LoginProvider loginProvider);
}
