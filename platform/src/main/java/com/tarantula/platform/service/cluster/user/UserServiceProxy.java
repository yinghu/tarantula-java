package com.tarantula.platform.service.cluster.user;

import com.hazelcast.core.Member;
import com.hazelcast.spi.AbstractDistributedObject;
import com.hazelcast.spi.InvocationBuilder;
import com.hazelcast.spi.NodeEngine;
import com.icodesoftware.*;
import com.icodesoftware.service.AccessIndexService;
import com.icodesoftware.service.LoginProvider;
import com.icodesoftware.service.MetricsListener;
import com.icodesoftware.service.UserService;
import com.tarantula.platform.TarantulaContext;
import com.tarantula.platform.service.cluster.ClusterUtil;
import com.tarantula.platform.service.cluster.user.UserClusterService;
import com.tarantula.platform.service.cluster.user.UserDeleteOperation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class UserServiceProxy extends AbstractDistributedObject<UserClusterService> implements UserService {

    private final String objectName;
    private MetricsListener metricsListener;


    protected UserServiceProxy(String objectName,NodeEngine nodeEngine, UserClusterService service) {
        super(nodeEngine, service);
        this.objectName = objectName;
        this.metricsListener = (k,v)->{};
    }

    @Override
    public String getName() {

        return this.objectName;

    }

    @Override
    public String getServiceName() {
        return UserService.NAME;
    }

    @Override
    public String name() {

        return UserService.NAME;

    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }

    @Override
    public Access loadUser(long systemId) {
        return null;
    }

    @Override
    public Account loadAccount(Access access) {
        return null;
    }

    @Override
    public List<Access> loadUsers(Account account) {
        return null;
    }

    @Override
    public Subscription loadSubscription(Account account) {
        return null;
    }

    @Override
    public Subscription loadSubscription(Access access) {
        return null;
    }

    @Override
    public Access createUser(OnAccess access) {
        return null;
    }

    @Override
    public Access createUser(Account account, Access access) {
        return null;
    }

    @Override
    public boolean changePassword(OnAccess access) {
        return false;
    }

    @Override
    public boolean updateEmail(OnAccess access) {
        return false;
    }

    @Override
    public Account createAccount(Access access, Subscription subscription) {
        return null;
    }

    @Override
    public Subscription subscribe(Account accountId, int durationMonth) {
        return null;
    }

    @Override
    public LoginProvider loginProvider(long systemId) {
        return null;
    }

    @Override
    public void createLoginProvider(LoginProvider loginProvider) {

    }

    @Override
    public List<Boolean> deleteUser(long systemId) {
        NodeEngine nodeEngine = getNodeEngine();
        Set<Member> members = nodeEngine.getClusterService().getMembers();
        UserDeleteOperation operation = new UserDeleteOperation(systemId);
        List<Boolean> succsessfullDeleteList = new ArrayList<>();

        for(Member member : members) {
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(UserService.NAME,operation,member.getAddress());

            ClusterUtil.CallResult callResult = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
                Future<AccessIndex> future = builder.invoke();
                return future.get(TarantulaContext.operationTimeout, TimeUnit.SECONDS);
            },metricsListener);

            if(!callResult.successful) throw new RuntimeException(callResult.exception);

            succsessfullDeleteList.add((Boolean) callResult.result);
        }

        return succsessfullDeleteList;
    }
}
