package com.tarantula.platform.presence;

import com.tarantula.*;
import com.tarantula.platform.*;

import com.tarantula.platform.service.DeploymentServiceProvider;
import com.tarantula.platform.util.*;

/**
 * Developer: YINGHU LU
 * Date: updated 12/25/2019.
 */
public class PresenceApplication extends TarantulaApplicationHeader {


    private DeploymentServiceProvider deploymentServiceProvider;
    private DataStore userDs;
    private DataStore accountDs;

    @Override
    public void setup(ApplicationContext context) throws Exception {
        super.setup(context);
        Configuration ya = this.context.configuration("yearlyAccess");
        Configuration ma = this.context.configuration("monthlyAccess");
        builder.registerTypeAdapter(PresenceContext.class, new PresenceContextSerializer());
        deploymentServiceProvider = this.context.serviceProvider(DeploymentServiceProvider.NAME);
        userDs = this.context.dataStore(Access.DataStore);
        accountDs = this.context.dataStore(Account.DataStore);
        this.context.registerRecoverableListener(new PresencePortableRegistry()).addRecoverableFilter(PresencePortableRegistry.ON_BALANCE_CID,(t)->{
            Presence presence = this.context.presence(t.owner());
            OnBalance ob = (OnBalance)t;
            if(!(presence!=null&&presence.transact(ob.balance()))){
                ob.redeemed(false);
                //this.context.dataStore("presence").create(ob);
            }
        });
        this.context.log("Presence application started on ["+descriptor.tag()+"]",OnLog.INFO);
    }

    @Override
    public void callback(Session session, byte[] payload) throws Exception {
        if (session.action().equals("onPresence")) {
            Presence presence = this.context.presence(session.systemId());
            PresenceContext pc = new PresenceContext(session.action());
            pc.presence= new OnSessionTrack(session.systemId(),presence.balance());
            pc.access = user(session.systemId());
            pc.account = account(session.systemId());
            session.write(this.builder.create().toJson(pc).getBytes(),this.descriptor.responseLabel());
        }
        else if(session.action().equals("onAddEmail")){
            OnAccess onAccess = this.builder.create().fromJson(new String(payload).trim(),OnAccess.class);
            User auser = user(session.systemId());
            if(auser!=null){
                auser.emailAddress((String)onAccess.property("emailAddress"));
                userDs.update(auser);
                if(!auser.role().equals(AccessControl.player)&&auser.primary()){
                    UserAccount userAccount = new UserAccount();
                    userAccount.distributionKey(session.systemId());
                    if(accountDs.load(userAccount)){
                        userAccount.emailAddress(auser.emailAddress());
                        accountDs.update(userAccount);
                    }
                }
                session.write(this.builder.create().toJson(new ResponseHeader("","successful",true)).getBytes(),descriptor.responseLabel());
            }else{
                session.write(this.builder.create().toJson(new ResponseHeader("","failed",false)).getBytes(),descriptor.responseLabel());
            }
        }
        else if(session.action().equals("onUpgradeRole")){
            OnAccess onAccess = this.builder.create().fromJson(new String(payload).trim(),OnAccess.class);
            User user = this.user(session.systemId());
            boolean suc = this.context.validator().upgradeRole(user,onAccess.name());
            ResponseHeader responseHeader = new ResponseHeader(session.action(),suc?"You have upgraded to ["+onAccess.name()+"]":"Failed to upgrade",suc);
            session.write(this.builder.create().toJson(responseHeader).getBytes(),descriptor.responseLabel());
        }
        else if(session.action().equals("onChangePassword")){
            OnAccess onAccess = this.builder.create().fromJson(new String(payload).trim(),OnAccess.class);
            User user = this.user(session.systemId());
            user.password(this.context.validator().hashPassword((String)onAccess.property("password")));
            boolean suc = userDs.update(user);
            ResponseHeader responseHeader = new ResponseHeader(session.action(),suc?"You have changed password":"Failed to change password",suc);
            session.write(this.builder.create().toJson(responseHeader).getBytes(),descriptor.responseLabel());
        }
        /**
        else if(session.action().equals("onPlay")){
              OnAccess onAccess = this.builder.create().fromJson(new String(payload).trim(),OnAccess.class);
              Presence presence = this.context.presence(session.systemId());
              Response resp = presence.onPlay(session,onAccess,this.context.descriptor(onAccess.applicationId()));
              if (resp != null) {
                  session.write(this.builder.create().toJson(resp).getBytes(),this.descriptor.responseLabel());//failure
              }
        }
        else if (session.action().equals("onBalance")) {
            Presence presence = this.context.presence(session.systemId());
            PresenceContext pc = new PresenceContext(session.action());
            pc.presence= new OnSessionTrack(session.systemId(),presence.balance());
            session.write(this.builder.create().toJson(pc).getBytes(),this.descriptor.responseLabel());
        }**/
        else if (session.action().equals("onAbsence")) {
            this.context.absence(session);
            session.write(this.builder.create().toJson(new ResponseHeader("onAbsence", "off session [" + session.stub() + "]", true)).getBytes(),this.descriptor.responseLabel());
        }
        /**
        Map<String, Object> chargeParams = new HashMap<>();
        chargeParams.put("amount",Double.valueOf(co.price).intValue());//pass penney number as integer
        chargeParams.put("currency", "usd");
        chargeParams.put("description", "Charge for ["+co.distributionKey()+"]");
        chargeParams.put("source",ex.header("orderId")); //orderId from client stripe call
        if(strip.validate(chargeParams)){
            //charge successfully
            OnBalanceTrack onBalanceTrack = new OnBalanceTrack(session.systemId(),co.credits);
            this.context.publish(this.context.routingKey(session.systemId(),"presence"),onBalanceTrack);
            suc = true;
        }**/
        else{
            session.write(this.builder.create().toJson(new ResponseHeader("onError", "operation not supported", false)).getBytes(),this.descriptor.responseLabel());
        }

    }
    private User user(String systemId){
        User user = new User();
        user.distributionKey(systemId);
        if(userDs.load(user)){
            return user;
        }
        return null;
    }
    private Account account(String systemId){
        UserAccount acc = new UserAccount();
        acc.distributionKey(systemId);
        if(accountDs.load(acc)){
            return acc;
        }
        return null;
    }
}
