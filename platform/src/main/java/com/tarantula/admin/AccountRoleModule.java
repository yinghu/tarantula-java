package com.tarantula.admin;

import com.google.gson.GsonBuilder;
import com.tarantula.*;
import com.tarantula.Module;
import com.tarantula.platform.IndexSet;
import com.tarantula.platform.ResponseHeader;
import com.tarantula.platform.presence.UserAccount;
import com.tarantula.platform.service.AccessIndexService;
import com.tarantula.platform.util.OnAccessDeserializer;
import com.tarantula.platform.util.SystemUtil;

public class AccountRoleModule implements Module {

    private ApplicationContext context;
    private GsonBuilder builder;
    private AccessIndexService accessIndexService;
    private DataStore user;
    private DataStore account;
    private int maxUserCount;

    @Override
    public boolean onRequest(Session session, byte[] payload, OnUpdate update) throws Exception {
        //this.context.log(session.action()+"=>"+new String(payload),OnLog.INFO);
        if(session.action().equals("onUserList")){
            IndexSet indexSet = new IndexSet();
            indexSet.distributionKey(session.systemId());
            indexSet.label(Account.UserLabel);
            if(account.load(indexSet)){

            }
            session.write(this.builder.create().toJson(new ResponseHeader(session.action(),"load user list",true)).getBytes(),label());
        }
        else if(session.action().equals("onAddUser")){
            OnAccess onAccess = this.builder.create().fromJson(new String(payload),OnAccess.class);
            Account acc = new UserAccount();
            acc.distributionKey(session.systemId());
            account.load(acc);
            if(acc.userCount(0)<maxUserCount){
                AccessIndex query = accessIndexService.set((String)onAccess.property("login"), user.bucket()+Recoverable.PATH_SEPARATOR+SystemUtil.oid());
                if(query!=null){
                    onAccess.owner(session.systemId());
                    onAccess.distributionKey(query.distributionKey());
                    this.context.postOffice().onTag("index/user").send(onAccess.distributionKey(),onAccess);
                    session.write(this.builder.create().toJson(_onMessage("user added")).getBytes(),label());
                }
                else{
                    session.write(this.builder.create().toJson(_onMessage("user already existed")).getBytes(),label());
                }
            }
            else{
                session.write(this.builder.create().toJson(_onMessage("you already have max user count")).getBytes(),label());
            }
        }
        else{
            session.write(payload,label());
        }
        return session.action().equals("onLeave");
    }

    @Override
    public void setup(ApplicationContext context) throws Exception {
        this.context = context;
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(OnAccess.class,new OnAccessDeserializer());
        this.builder.registerTypeAdapter(AdminUserObject.class,new AdminObjectSerializer());
        this.accessIndexService = this.context.serviceProvider(AccessIndexService.NAME);
        this.user = this.context.dataStore(Access.DataStore);
        this.account = this.context.dataStore(Account.DataStore);
        this.maxUserCount = Integer.parseInt(this.context.configuration("setup").property("maxUserCount"));
        this.context.log("Account role module started with max user count ["+maxUserCount+"]", OnLog.INFO);
    }
    @Override
    public String label() {
        return "account-role";
    }
    private AdminUserObject _onMessage(String message){
        return new AdminUserObject(message,label());
    }
    private AdminUserObject _onAccessIndex(AccessIndex accessIndex){
        return new AdminUserObject(accessIndex,label());
    }
}
