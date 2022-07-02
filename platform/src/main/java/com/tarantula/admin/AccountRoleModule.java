package com.tarantula.admin;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.icodesoftware.service.AccessIndexService;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.service.TokenValidatorProvider;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.IndexSet;
import com.tarantula.platform.presence.User;
import com.tarantula.platform.presence.UserAccount;
import com.tarantula.platform.util.OnAccessDeserializer;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class AccountRoleModule implements Module, AccessIndexService.Listener {

    private ApplicationContext context;
    private GsonBuilder builder;
    private AccessIndexService accessIndexService;
    private DataStore user;
    private DataStore account;
    private DataStore accountIndex;
    private int trialMaxUserCount;
    private int subscribedMaxUserCount;

    private TokenValidatorProvider tokenValidatorProvider;
    private AtomicBoolean accessIndexEnabled;
    @Override
    public boolean onRequest(Session session, byte[] payload) throws Exception {
        //this.context.log(session.action()+"=>"+new String(payload),OnLog.INFO);
        if(session.action().equals("onUserList")){
            User access = _user(session.systemId());
            IndexSet indexSet = new IndexSet();
            indexSet.distributionKey(access.primary()?session.systemId():access.owner());
            indexSet.label(Account.UserLabel);
            AccessContext atc = new AccessContext();
            atc.userList = new ArrayList<>();
            if(accountIndex.load(indexSet)){
                //atc.userList.add()
                indexSet.keySet().forEach((k)->{
                    User u = new User();
                    u.distributionKey(k);
                    if(user.load(u)){
                        atc.userList.add(u);
                    }
                });
            }
            session.write(atc.toJson().toString().getBytes());
        }
        else if(session.action().equals("onUpgradeUser")){
            OnAccess onAccess = this.builder.create().fromJson(new String(payload),OnAccess.class);
            String uid = (String)onAccess.property(OnAccess.ACCESS_ID);
            User owner = new User();
            owner.distributionKey(session.systemId());
            if(user.load(owner)&&owner.primary()){//only primary
                User u = new User();
                u.distributionKey(uid);
                if(user.load(u)){
                    if(!u.role().equals(owner.role())){
                        session.write(JsonUtil.toSimpleResponse(this.tokenValidatorProvider.grantAccess(u,owner),"upgraded").getBytes());
                    }else{
                        session.write(JsonUtil.toSimpleResponse(this.tokenValidatorProvider.revokeAccess(u),"revoked").getBytes());
                    }
                }else{
                    session.write(toMessage("user not existed ["+uid+"]",false).toString().getBytes());
                }
            }else{
                session.write(toMessage("only primary can update role",false).toString().getBytes());
            }
        }
        else if(session.action().equals("onAddUser")){
            if(accessIndexEnabled.get()){
                OnAccess onAccess = this.builder.create().fromJson(new String(payload),OnAccess.class);
                User ua = _user(session.systemId());
                Account acc = new UserAccount();
                acc.distributionKey(ua.primary()?session.systemId():ua.owner());
                if(account.load(acc)){
                    int maxUserCount = acc.trial()?trialMaxUserCount:subscribedMaxUserCount;
                    if(acc.userCount(0)<maxUserCount){
                        AccessIndex query = accessIndexService.set((String)onAccess.property("login"),0);
                        if(query!=null){
                            onAccess.owner(acc.distributionKey());//make sure acc id as the owner
                            onAccess.distributionKey(query.distributionKey());
                            this.context.postOffice().onTag("index/user").send(onAccess.distributionKey(),onAccess);
                            session.write(this.toMessage("user added",true).toString().getBytes());
                        }
                        else{
                            session.write(this.toMessage("user already existed",false).toString().getBytes());
                        }
                    }
                    else{
                        session.write(this.toMessage("you already have max user count",false).toString().getBytes());
                    }
                }
                else{
                    session.write(this.toMessage("no permission to add user",false).toString().getBytes());
                }
            }
            else{
                session.write(this.toMessage("add user service not available",false).toString().getBytes());
            }
        }
        else{
            throw new UnsupportedOperationException(session.action());
        }
        return session.action().equals("onLeave");
    }

    @Override
    public void setup(ApplicationContext context) throws Exception {
        this.context = context;
        this.accessIndexEnabled = new AtomicBoolean(false);
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(OnAccess.class,new OnAccessDeserializer());
        this.accessIndexService = this.context.serviceProvider(AccessIndexService.NAME);
        this.tokenValidatorProvider = this.context.serviceProvider(TokenValidatorProvider.NAME);
        this.user = this.context.dataStore(Access.DataStore);
        this.account = this.context.dataStore(Account.DataStore);
        this.accountIndex = this.context.dataStore(Account.IndexDataStore);
        this.trialMaxUserCount = ((Number)this.context.configuration("user").property("trialMaxUserCount")).intValue();
        this.subscribedMaxUserCount = ((Number)this.context.configuration("user").property("subscribedMaxUserCount")).intValue();
        DeploymentServiceProvider deploymentServiceProvider = this.context.serviceProvider(DeploymentServiceProvider.NAME);
        deploymentServiceProvider.registerAccessIndexListener(this);
        this.context.log("Account role module started with max user count ["+trialMaxUserCount+","+subscribedMaxUserCount+"]", OnLog.INFO);
    }

    private JsonObject toMessage(String msg, boolean suc){
        JsonObject jms = new JsonObject();
        jms.addProperty("successful",suc);
        jms.addProperty("message",msg);
        return jms;
    }
    private User _user(String systemId){
        User u = new User();
        u.distributionKey(systemId);
        if(user.load(u)){
            return u;
        }
        return null;
    }

    @Override
    public void onStop() {
        accessIndexEnabled.set(false);
    }

    @Override
    public void onStart() {
        accessIndexEnabled.set(true);
    }
}
