package com.tarantula.admin;

import com.google.gson.GsonBuilder;
import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.icodesoftware.service.AccessIndexService;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.service.TokenValidatorProvider;
import com.icodesoftware.service.UserService;

import com.tarantula.platform.util.OnAccessDeserializer;
import com.tarantula.platform.util.SystemUtil;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class AccountRoleModule implements Module, AccessIndexService.Listener {

    private ApplicationContext context;
    private GsonBuilder builder;
    private AccessIndexService accessIndexService;

    private int trialMaxUserCount;
    private int subscribedMaxUserCount;

    private TokenValidatorProvider tokenValidatorProvider;
    private UserService userService;
    private AtomicBoolean accessIndexEnabled;
    private ConcurrentHashMap<String,SubscriptionItem> _items = new ConcurrentHashMap<>();
    @Override
    public boolean onRequest(Session session, byte[] payload) throws Exception {
        //this.context.log(session.action()+"=>"+new String(payload),OnLog.INFO);
        if(session.action().equals("onUserList")){
            Access access = userService.loadUser(session.systemId());
            AccessContext atc = new AccessContext();
            atc.userList = userService.loadUsers(access);
            session.write(atc.toJson().toString().getBytes());
        }
        else if(session.action().equals("onUpgradeUser")){
            OnAccess onAccess = this.builder.create().fromJson(new String(payload),OnAccess.class);
            String uid = (String)onAccess.property(OnAccess.ACCESS_ID);
            Access owner = userService.loadUser(session.systemId());
            if(owner.primary()){//only primary
                Access u = userService.loadUser(uid);
                if(u!=null){
                    if(!u.role().equals(owner.role())){
                        session.write(toMessage("upgraded",this.tokenValidatorProvider.grantAccess(u,owner)).getBytes());
                    }else{
                        session.write(toMessage("revoked",this.tokenValidatorProvider.revokeAccess(u)).getBytes());
                    }
                }else{
                    session.write(toMessage("user not existed ["+uid+"]",false).getBytes());
                }
            }else{
                session.write(toMessage("only primary can update role",false).getBytes());
            }
        }
        else if(session.action().equals("onAddUser")){
            if(accessIndexEnabled.get()){
                OnAccess onAccess = this.builder.create().fromJson(new String(payload),OnAccess.class);
                Access ua =  userService.loadUser(session.systemId());
                AccessIndex query = accessIndexService.set((String)onAccess.property("login"),0);
                if(query!=null){
                    onAccess.owner(ua.primary()?session.systemId():ua.owner());//make sure acc id as the owner
                    onAccess.distributionKey(query.distributionKey());
                    this.context.postOffice().onTag("index/user").send(onAccess.distributionKey(),onAccess);
                    session.write(this.toMessage("add user event send",true).getBytes());
                }
                else {
                    session.write(this.toMessage("user already existed", false).getBytes());
                }
            }
            else{
                session.write(this.toMessage("add user service not available",false).getBytes());
            }
        }
        else if(session.action().equals("onSubscription")){
            AccessContext accessContext = new AccessContext();
            accessContext.subscriptionList = new ArrayList<>();
            _items.forEach((k,v)->accessContext.subscriptionList.add(v));
            session.write(accessContext.toJson().toString().getBytes());
        }
        else if(session.action().equals("onCommit")){
            OnAccess acc = builder.create().fromJson(new String(session.payload()).trim(),OnAccess.class);
            Map<String,Object> chargeParams = acc.toMap();
            SubscriptionItem item = _items.get(acc.property("checkoutId"));
            chargeParams.put("amount",Double.valueOf(item.price*100).intValue());//pass penney number as integer
            chargeParams.put("currency", "usd");
            chargeParams.put("description",item.description);
            if(this.context.validator().validateToken(chargeParams)){
                Subscription subscription = userService.subscribe(session.systemId(),12);
                session.write(toMessage( "on commit",true).getBytes());
            }
            else {
                session.write(toMessage("on commit",false).getBytes());
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
        this.userService = this.context.serviceProvider(UserService.NAME);
        this.tokenValidatorProvider = this.context.serviceProvider(TokenValidatorProvider.NAME);
        this.trialMaxUserCount = ((Number)this.context.configuration("user").property("trialMaxUserCount")).intValue();
        this.subscribedMaxUserCount = ((Number)this.context.configuration("user").property("subscribedMaxUserCount")).intValue();
        DeploymentServiceProvider deploymentServiceProvider = this.context.serviceProvider(DeploymentServiceProvider.NAME);
        deploymentServiceProvider.registerAccessIndexListener(this);
        SubscriptionItem item1 = new SubscriptionItem(SystemUtil.oid(),"Monthly","one month subscription",1.99,true);
        SubscriptionItem item2 = new SubscriptionItem(SystemUtil.oid(),"Yearly","one year subscription",19.99,true);
        SubscriptionItem item3 = new SubscriptionItem(SystemUtil.oid(),"2-Month","two month subscription",2.99,true);
        SubscriptionItem item4 = new SubscriptionItem(SystemUtil.oid(),"2-Year","two year subscription",29.99,true);
        _items.put(item1.oid(),item1);
        _items.put(item2.oid(),item2);
        _items.put(item3.oid(),item3);
        _items.put(item4.oid(),item4);
        this.context.log("Account role module started with max user count ["+trialMaxUserCount+","+subscribedMaxUserCount+"]", OnLog.INFO);
    }

    private String toMessage(String msg, boolean suc){
        return SystemUtil.toJsonMessage(msg,suc);
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
