package com.tarantula.platform.presence;

import com.tarantula.*;
import com.tarantula.platform.*;

import com.tarantula.platform.service.DeploymentServiceProvider;
import com.tarantula.platform.service.OnLobby;
import com.tarantula.platform.service.TokenValidatorProvider;
import com.tarantula.platform.util.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Developer: YINGHU LU
 * Date: updated 5/20/2020
 */
public class PresenceApplication extends TarantulaApplicationHeader implements OnLobby.Listener {


    private CopyOnWriteArraySet<String> _lobbyList = new CopyOnWriteArraySet<>();

    private DeploymentServiceProvider deploymentServiceProvider;
    private DataStore userDs;
    private DataStore accountDs;
    private DataStore memberDs;
    private SubscriptionFee monthly;
    private SubscriptionFee yearly;
    @Override
    public void setup(ApplicationContext context) throws Exception {
        super.setup(context);
        Configuration ya = this.context.configuration("yearlyAccess");
        Configuration ma = this.context.configuration("monthlyAccess");
        monthly = new SubscriptionFee("monthlyAccess",ma.property("description"),ma.property("price"),ma.property("currency"));
        yearly = new SubscriptionFee("yearlyAccess",ya.property("description"),ya.property("price"),ya.property("currency"));
        builder.registerTypeAdapter(PresenceContext.class, new PresenceContextSerializer());
        this.deploymentServiceProvider = this.context.serviceProvider(DeploymentServiceProvider.NAME);
        this.deploymentServiceProvider.registerOnLobbyListener(this);
        userDs = this.context.dataStore(Access.DataStore);
        accountDs = this.context.dataStore(Account.DataStore);
        memberDs = this.context.dataStore(Subscription.DataStore);
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
            pc.presence.version(presence.count(0));
            pc.access = user(session.systemId());
            pc.account = account(session.systemId());
            pc.subscription = membership(session.systemId());
            session.write(this.builder.create().toJson(pc).getBytes(),this.descriptor.responseLabel());
        }
        else if(session.action().equals("onLobbyList")){
            PresenceContext ic = new PresenceContext("onLobbyList");
            ic.lobbyList = new ArrayList<>();
            _lobbyList.forEach((a)->{
                ic.lobbyList.add(this.context.lobby(a));
            });
            session.write(this.builder.create().toJson(ic).getBytes(),this.descriptor.responseLabel());
        }
        else if(session.action().equals("onShoppingList")){
            session.write(new ShoppingContext(monthly,yearly).toJson().toString().getBytes(),this.descriptor.responseLabel());
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
        else if (session.action().equals("onAbsence")) {
            this.context.absence(session);
            session.write(this.builder.create().toJson(new ResponseHeader("onAbsence", "off session [" + session.stub() + "]", true)).getBytes(),this.descriptor.responseLabel());
        }
        else if(session.action().equals("onCommit")){
            OnAccess onAccess = this.builder.create().fromJson(new String(payload),OnAccess.class);
            String cid = (String)onAccess.property("checkoutId");
            SubscriptionFee fee = cid.equals("monthlyAccess")?monthly:yearly;
            Map<String, Object> chargeParams = new HashMap<>();
            chargeParams.put("amount",Double.valueOf(fee.amount).intValue()*100);//pass penney number as integer
            chargeParams.put("currency", "usd");
            chargeParams.put("description", "Charge for ["+fee.name+"]");
            chargeParams.put("source",onAccess.property("orderId")); //orderId from client stripe call
            TokenValidatorProvider tp = this.context.serviceProvider(TokenValidatorProvider.NAME);
            if(tp.authVendor("stripe").validate(chargeParams)){
                
                //charge successfully
                //OnBalanceTrack onBalanceTrack = new OnBalanceTrack(session.systemId(),co.credits);
                //this.context.publish(this.context.routingKey(session.systemId(),"presence"),onBalanceTrack);
                //suc = true;
                session.write(this.builder.create().toJson(new ResponseHeader("onCommit", "your purchase is successful", true)).getBytes(),this.descriptor.responseLabel());
            }
            else{
                session.write(this.builder.create().toJson(new ResponseHeader("onCommit", "failed to commit your purchase", false)).getBytes(),this.descriptor.responseLabel());
            }
        }
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
    private Subscription membership(String systemId){
        Membership acc = new Membership();
        acc.distributionKey(systemId);
        if(memberDs.load(acc)){
            return acc;
        }
        return null;
    }
    @Override
    public void onLobby(OnLobby onLobby) {
        context.log("Lobby Updated--->>"+onLobby.toString(),OnLog.WARN);
        if(!onLobby.closed()){
            this._lobbyList.add(onLobby.typeId());
        }
        else{
            this._lobbyList.remove(onLobby.typeId());
        }
    }
}
