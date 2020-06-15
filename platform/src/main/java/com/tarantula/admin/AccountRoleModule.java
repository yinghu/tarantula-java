package com.tarantula.admin;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.tarantula.*;
import com.tarantula.Module;
import com.tarantula.platform.AccessControl;
import com.tarantula.platform.IndexSet;
import com.tarantula.platform.presence.User;
import com.tarantula.platform.presence.UserAccount;
import com.tarantula.platform.service.AccessIndexService;
import com.tarantula.platform.util.OnAccessDeserializer;
import com.tarantula.platform.util.SystemUtil;

import java.util.ArrayList;

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
            User access = _user(session.systemId());
            IndexSet indexSet = new IndexSet();
            indexSet.distributionKey(access.primary()?session.systemId():access.owner());
            indexSet.label(Account.UserLabel);
            AccessContext atc = new AccessContext();
            atc.userList = new ArrayList<>();
            if(account.load(indexSet)){
                //atc.userList.add()
                indexSet.keySet.forEach((k)->{
                    User u = new User();
                    u.distributionKey(k);
                    if(user.load(u)){
                        atc.userList.add(u);
                    }
                });
            }
            session.write(atc.toJson().toString().getBytes(),label());
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
                    if(u.role().equals(owner.role())){
                        u.role(AccessControl.player.name());
                    }else{
                        u.role(owner.role());
                    }
                    user.update(u);
                }
                session.write(toMessage("role granted ["+u.role()+"]",false).toString().getBytes(),label());
            }else{
                session.write(toMessage("only primary can update role",false).toString().getBytes(),label());
            }
        }
        else if(session.action().equals("onAddUser")){
            OnAccess onAccess = this.builder.create().fromJson(new String(payload),OnAccess.class);
            User ua = _user(session.systemId());
            Account acc = new UserAccount();
            acc.distributionKey(ua.primary()?session.systemId():ua.owner());
            if(account.load(acc)){
                if(acc.userCount(0)<maxUserCount){
                    AccessIndex query = accessIndexService.set((String)onAccess.property("login"), user.bucket()+Recoverable.PATH_SEPARATOR+SystemUtil.oid());
                    if(query!=null){
                        onAccess.owner(acc.distributionKey());//make sure acc id as the owner
                        onAccess.distributionKey(query.distributionKey());
                        this.context.postOffice().onTag("index/user").send(onAccess.distributionKey(),onAccess);
                        session.write(this.toMessage("user added",true).toString().getBytes(),label());
                    }
                    else{
                        session.write(this.toMessage("user already existed",false).toString().getBytes(),label());
                    }
                }
                else{
                    session.write(this.toMessage("you already have max user count",false).toString().getBytes(),label());
                }
            }
            else{
                session.write(this.toMessage("no permission to add user",false).toString().getBytes(),label());
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
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(OnAccess.class,new OnAccessDeserializer());
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
}
