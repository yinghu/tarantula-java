package com.tarantula.platform.presence;

import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.service.AccessIndexService;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.service.TokenValidatorProvider;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.*;
import com.tarantula.platform.service.Metrics;
import com.tarantula.platform.util.PresenceContextSerializer;
import com.tarantula.platform.util.SystemUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Updated 6/17/2020
 */
public class UserManagementApplication extends TarantulaApplicationHeader{

    private String lobbyId;
    private boolean activated;
    private String role = AccessControl.player.name();
    private double initialBalance;
    private AccessIndexService accessIndexService;
    private DeploymentServiceProvider deploymentServiceProvider;

    private List<Access.Role> roleList;
    private TokenValidatorProvider tokenValidatorProvider;

    private DataStore uDatastore;
    private DataStore pDatastore;
    private DataStore aDatastore;
    private DataStore sDatastore;

    @Override
    public void setup(ApplicationContext context) throws Exception {
        super.setup(context);
        Configuration configuration = this.context.configuration("setup");
        this.lobbyId = configuration.property("lobbyId");
        this.activated = Boolean.parseBoolean(configuration.property("activated"));
        this.initialBalance = Double.parseDouble(configuration.property("initialBalance"));
        this.role = configuration.property("roleName");
        builder.registerTypeAdapter(PresenceContext.class,new PresenceContextSerializer());
        this.accessIndexService = this.context.serviceProvider(AccessIndexService.NAME);
        deploymentServiceProvider = this.context.serviceProvider(DeploymentServiceProvider.NAME);
        this.tokenValidatorProvider = this.context.serviceProvider(TokenValidatorProvider.NAME);
        this.roleList = this.tokenValidatorProvider.list();
        String root = configuration.property("root");
        String pwd = configuration.property("password");
        OnAccess onAccess = new OnAccessTrack();
        onAccess.property("login",root);
        onAccess.property("password",pwd);
        uDatastore = this.context.dataStore(Access.DataStore);
        pDatastore = this.context.dataStore(Presence.DataStore);
        aDatastore = this.context.dataStore(Account.DataStore);
        sDatastore = this.context.dataStore(OnSession.DataStore);
        DataStore mDatastore = this.context.dataStore(Subscription.DataStore);
        accessIndexService.set("serverPush");
        AccessIndex accessIndex = accessIndexService.set(root);
        if(accessIndex!=null){
            Access user = createLogin(onAccess,accessIndex.distributionKey(),AccessControl.root.name(),false,"password",true);
            Account acc = new UserAccount();
            acc.distributionKey(user.distributionKey());
            acc.trial(false);
            acc.subscribed(true);
            LocalDateTime loc = LocalDateTime.now();
            acc.timestamp(TimeUtil.toUTCMilliseconds(loc));
            aDatastore.create(acc);
            Membership membership = new Membership();
            membership.distributionKey(user.distributionKey());
            membership.startTimestamp(TimeUtil.toUTCMilliseconds(loc));
            membership.endTimestamp(TimeUtil.toUTCMilliseconds(loc.plusMonths(12)));
            membership.timestamp(TimeUtil.toUTCMilliseconds(loc));
            mDatastore.create(membership);
        }
        this.context.registerRecoverableListener(new UserPortableRegistry()).addRecoverableFilter(UserPortableRegistry.ON_ACCESS_CID,(a)->{
            //add player user to the account
            OnAccess uadded = (OnAccess)a;
            if(uadded.property("command").equals("onAddUser")){
                Access user = createLogin(uadded,uadded.distributionKey(),role,false,"password",false);
                Account account = new UserAccount();
                account.distributionKey(uadded.owner());
                if(aDatastore.load(account)){
                    account.userCount(1);
                    account.timestamp(TimeUtil.toUTCMilliseconds(LocalDateTime.now()));
                    aDatastore.update(account);
                    IndexSet idx = new IndexSet();
                    idx.distributionKey(account.distributionKey());
                    idx.label(Account.UserLabel);
                    idx.keySet.add(user.distributionKey());
                    if(!aDatastore.createIfAbsent(idx,true)){
                        idx.keySet.add(user.distributionKey());//update on existing
                        aDatastore.update(idx);
                    }
                }
            }
        });
        this.deploymentServiceProvider.registerOnConnectionStateListener(this);
        this.context.log("User management application started on tag ["+descriptor.tag()+"]",OnLog.INFO);
    }
    @Override
    public void callback(Session session,byte[] payload) throws Exception {
        //this.context.log(new String(payload),OnLog.WARN);
        OnAccess acc = builder.create().fromJson(new String(payload).trim(),OnAccess.class);
        if(session.action().equals("onIndex")){
            PresenceContext ic = new PresenceContext("onIndex");
            ic.googleClientId = this.tokenValidatorProvider.authVendor(OnAccess.GOOGLE).clientId();
            ic.stripeClientId = this.tokenValidatorProvider.authVendor(OnAccess.STRIPE).clientId();
            ic.lobbyList = this.context.index();
            ic.roleList = roleList;
            session.write(builder.create().toJson(ic).getBytes(),this.descriptor.responseLabel());
        }
        else if(session.action().equals("onLogin")){
            OnSession access = this.login(session.systemId(),(String) acc.property(OnAccess.PASSWORD),session);
            onSession(access,session);
            this.deploymentServiceProvider.onUpdated(Metrics.PASSWORD_COUNT,1);
        }
        else if(session.action().equals("onToken")){//exchange token
            boolean suc = this.context.validator().validateToken(acc.toMap());
            if(suc){
                OnSession _ox = new OnSessionTrack();
                _ox.distributionKey(session.systemId());
                sDatastore.load(_ox);
                OnSession onSession = this.login(session.systemId(),_ox.token(),session);
                onSession(onSession,session);
            }else{
                session.write(this.builder.create().toJson(new ResponseHeader("onToken", "invalid token", false)).getBytes(),this.descriptor.responseLabel());
            }
        }
        else if(session.action().equals("onTicket")){//validate game server connection
            if(this.context.validator().validateTicket(session.systemId(),acc.stub(),(String)acc.property(OnAccess.ACCESS_KEY))){
                PresenceContext ptx = new PresenceContext();
                ptx.successful(true);
                session.write(this.builder.create().toJson(ptx).getBytes(),this.descriptor.responseLabel()+"?onTicket");
            }
            else{
                session.write(this.builder.create().toJson(new ResponseHeader("onTicket", "invalid ticket", false)).getBytes(),this.descriptor.responseLabel());
            }
        }
        else if(session.action().equals("onTokenRegister")){
            Map<String,Object> params = acc.toMap();
            if(this.context.validator().validateToken(params)){
                AccessIndex _query = accessIndexService.get((String) acc.property("login"));
                if(_query!=null){
                    OnSession _onSession = new OnSessionTrack();
                    _onSession.distributionKey(session.systemId());
                    _onSession.token(SystemUtil.oid());
                    sDatastore.createIfAbsent(_onSession,false);
                    acc.property(OnAccess.PASSWORD,_onSession.token());
                    Access user = createLogin(acc,session.systemId(),role,true,acc.name(),true);
                    user.emailAddress((String) params.get("email"));
                    user.activated(true);
                    uDatastore.update(user);
                    OnSession onSession = login(session.systemId(),_onSession.token(),session);
                    onSession(onSession,session);
                }
                else{
                    session.write(builder.create().toJson(new ResponseHeader(session.action(),false,0,"login [" + acc.property("login") + "] cannot be registered","error")).getBytes(),this.descriptor.responseLabel());
                }
            }else{
                session.write(this.builder.create().toJson(new ResponseHeader("onToken", "invalid token", false)).getBytes(),this.descriptor.responseLabel());
            }
        }
        else if(session.action().equals("onRegister")){
            AccessIndex _query = accessIndexService.get((String) acc.property(OnAccess.LOGIN));
            if(_query==null){//double-check
                session.write(builder.create().toJson(new ResponseHeader(session.action(),false,0,"login [" + acc.property("login") + "] cannot be registered","error")).getBytes(),this.descriptor.responseLabel());
            }
            else{
                Access access = this.createLogin(acc,session.systemId(),role,false,"password",true);
                session.systemId(access.distributionKey());
                OnSession _onSession = this.login(session.systemId(),(String) acc.property(OnAccess.PASSWORD),session);
                this.onSession(_onSession,session);
                this.deploymentServiceProvider.onUpdated(Metrics.PASSWORD_COUNT,1);
            }
        }
        else if(session.action().equals("onDevice")){
            String deviceId = (String) acc.property(OnAccess.DEVICE_ID);
            OnSession access = this.login(session.systemId(),deviceId,session);
            onSession(access,session);
            this.deploymentServiceProvider.onUpdated(Metrics.DEVICE_COUNT,1);
        }
        else if(session.action().equals("onDeviceRegister")){
            String deviceId = (String) acc.property(OnAccess.DEVICE_ID);
            AccessIndex accessIndex = this.accessIndexService.get(deviceId);
            if(accessIndex!=null){
                acc.property("login",deviceId);
                acc.property("password",deviceId);
                this.createLogin(acc,session.systemId(),role,true,"device",true);
                OnSession access = this.login(session.systemId(),(String) acc.property(OnAccess.PASSWORD),session);
                onSession(access,session);
            }
            else{
                session.write(this.builder.create().toJson(new ResponseHeader("onDevice","wrong device id", false)).getBytes(),this.descriptor.responseLabel());
            }
            this.deploymentServiceProvider.onUpdated(Metrics.DEVICE_COUNT,1);
        }
        else if(session.action().equals("onResetCode")){
            String code = this.deploymentServiceProvider.resetCode(session.trackId());
            if(this.deploymentServiceProvider.registerPostOffice().onEmail().send(session.trackId(),code)){
                session.write(toMessage("check email for code",true).toString().getBytes(),descriptor.responseLabel());
            }
            else{
                session.write(toMessage("system error,try later",true).toString().getBytes(),descriptor.responseLabel());
            }
        }
        else if(session.action().equals("onResetPassword")){
            String code = (String)acc.property(OnAccess.ACCESS_KEY);
            Access user = new User();
            user.distributionKey(session.systemId());
            if(!uDatastore.load(user)){
                session.write(toMessage("wrong user name",false).toString().getBytes(),descriptor.responseLabel());
            }
            else{
                if(user.activated()&&this.deploymentServiceProvider.checkCode(code).equals(user.emailAddress())){
                    user.password(this.context.validator().hashPassword((String) acc.property(OnAccess.PASSWORD)));
                    uDatastore.update(user);
                    OnSession onSession = this.login(session.systemId(),(String) acc.property(OnAccess.PASSWORD),session);
                    onSession(onSession,session);
                }
                else{
                    session.write(this.builder.create().toJson(new ResponseHeader("onResetPassword", "Invalid recovery", false)).getBytes(),this.descriptor.responseLabel());
                }
            }
        }
        else{
            throw new UnsupportedOperationException(session.action());
        }
    }
    private void onSession(OnSession access, Session session){
        if(access.successful()){
            PresenceContext ptx = new PresenceContext("onLogin");
            ptx.presence= access;
            List<Lobby> lobbyList = new ArrayList();
            lobbyList.add(this.context.lobby(this.lobbyId));
            ptx.lobbyList=(lobbyList);
            session.write(this.builder.create().toJson(ptx).getBytes(),this.descriptor.responseLabel());
            session.systemId(access.systemId());
            session.stub(access.stub());
            session.ticket(access.ticket());
        }
        else{
            session.write(this.builder.create().toJson(new ResponseHeader("reset","wrong user/password", false)).getBytes(),this.descriptor.responseLabel());
        }
    }
    private OnSession login(String systemId, String password, Session session){
        Access access = new User();
        access.distributionKey(systemId);
        OnSession _onSession = OnSessionTrack.PASSWORD_NOT_MATCHED;
        if(uDatastore.load(access)){
            access.routingNumber(session.routingNumber());
            _onSession=this.context.validator().validatePassword(access,password);
            _onSession.systemId(systemId);
        }
        return _onSession;
    }
    private Access createLogin(OnAccess payload,String systemId,String roleName,boolean validated,String validator,boolean primary){
        Access acc = new User((String) payload.property("login"),validated,validator);
        acc.distributionKey(systemId);
        String pwd = (String)payload.property(OnAccess.PASSWORD);
        acc.password(this.context.validator().hashPassword(pwd));
        acc.activated(this.activated);//if false do email validation
        acc.primary(primary);
        if(!primary){
            acc.owner(payload.owner());
        }
        acc.role(roleName);
        if(uDatastore.create(acc)){
            PresenceIndex px = new PresenceIndex(initialBalance);
            px.distributionKey(acc.distributionKey());
            pDatastore.create(px);
        }
        return acc;
    }
    private JsonObject toMessage(String msg, boolean suc){
        JsonObject jms = new JsonObject();
        jms.addProperty("successful",suc);
        jms.addProperty("message",msg);
        return jms;
    }
}
