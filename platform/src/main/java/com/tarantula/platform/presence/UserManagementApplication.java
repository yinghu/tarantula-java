package com.tarantula.platform.presence;

import com.icodesoftware.*;
import com.icodesoftware.service.*;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.*;
import com.tarantula.platform.service.Metrics;
import com.tarantula.platform.util.PresenceContextSerializer;
import com.tarantula.platform.util.SystemUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class UserManagementApplication extends TarantulaApplicationHeader implements Configurable.Listener<OnLobby>{

    private String lobbyId;
    private boolean activated;
    private String role = AccessControl.player.name();
    private double initialBalance;
    private AccessIndexService accessIndexService;
    private UserService userService;
    private DeploymentServiceProvider deploymentServiceProvider;

    private List<Access.Role> roleList;
    private TokenValidatorProvider tokenValidatorProvider;
    //private List<String> gameList;
    private ConcurrentHashMap<String,OnLobby> onLobbyIndex;

    private DataStore userDatastore;
    private DataStore thirdPartyLoginDatastore;
    private DataStore accountDatastore;
    private DataStore accountIndex;

    @Override
    public void setup(ApplicationContext context) throws Exception {
        super.setup(context);
        Configuration configuration = this.context.configuration("account");
        this.lobbyId = (String)configuration.property("lobbyId");
        this.activated = (boolean)configuration.property("activated");
        this.initialBalance = ((Number)configuration.property("initialBalance")).doubleValue();
        this.role = (String)configuration.property("roleName");
        builder.registerTypeAdapter(PresenceContext.class,new PresenceContextSerializer());
        this.accessIndexService = this.context.serviceProvider(AccessIndexService.NAME);
        deploymentServiceProvider = this.context.serviceProvider(DeploymentServiceProvider.NAME);
        userService = this.context.serviceProvider(UserService.NAME);
        this.tokenValidatorProvider = this.context.serviceProvider(TokenValidatorProvider.NAME);
        this.roleList = this.tokenValidatorProvider.list();
        this.onLobbyIndex = new ConcurrentHashMap<>();
        //this.gameList = new CopyOnWriteArrayList<>();
        String root = (String)configuration.property("root");
        String pwd = (String) configuration.property("password");
        OnAccess onAccess = new OnAccessTrack();
        onAccess.property("login",root);
        onAccess.property("password",pwd);
        userDatastore = this.context.dataStore(Access.DataStore);
        thirdPartyLoginDatastore = this.context.dataStore(ThirdPartyLogin.DataStore);
        accountDatastore = this.context.dataStore(Account.DataStore);
        //sDatastore = this.context.dataStore(OnSession.DataStore);
        accountIndex = this.context.dataStore(Account.IndexDataStore);
        DataStore mDatastore = this.context.dataStore(Subscription.DataStore);
        accessIndexService.set("serverPush",0);
        AccessIndex accessIndex = accessIndexService.set(root,0);
        if(accessIndex!=null){
            Access user = createLogin(onAccess,accessIndex.distributionKey(),AccessControl.root.name(),false,"password",true);
            Account acc = new UserAccount();
            acc.distributionKey(user.distributionKey());
            acc.trial(false);
            acc.subscribed(true);
            LocalDateTime loc = LocalDateTime.now();
            acc.timestamp(TimeUtil.toUTCMilliseconds(loc));
            accountDatastore.create(acc);
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
                if(accountDatastore.load(account)){
                    account.userCount(1);
                    account.timestamp(TimeUtil.toUTCMilliseconds(LocalDateTime.now()));
                    accountDatastore.update(account);
                    IndexSet idx = new IndexSet();
                    idx.distributionKey(account.distributionKey());
                    idx.label(Account.UserLabel);
                    idx.addKey(user.distributionKey());
                    if(!accountIndex.createIfAbsent(idx,true)){
                        idx.addKey(user.distributionKey());//update on existing
                        accountIndex.update(idx);
                    }
                }
            }
        });
        this.deploymentServiceProvider.registerConfigurableListener(OnLobby.TYPE,this);
        this.context.log("User management application started on tag ["+descriptor.tag()+"]",OnLog.INFO);
    }
    @Override
    public void callback(Session session,byte[] payload) throws Exception {
        //this.context.log(new String(payload),OnLog.WARN);
        OnAccess acc = builder.create().fromJson(new String(payload).trim(),OnAccess.class);
        if(session.action().equals("onIndex")){
            PresenceContext ic = new PresenceContext("onIndex");
            ic.googleClientId = this.tokenValidatorProvider.authVendor(OnAccess.GOOGLE).clientId();
            ic.lobbyList = this.context.index();
            session.write(builder.create().toJson(ic).getBytes());
        }
        else if(session.action().equals("onAvailable")){
            PresenceContext ic = new PresenceContext("onAvailable");
            String typeId = session.trackId();
            if(onLobbyIndex.containsKey(typeId)){
                ic.googleClientId = this.tokenValidatorProvider.authVendor(OnAccess.GOOGLE).clientId(typeId);
                session.write(builder.create().toJson(ic).getBytes());
            }
            else{
                session.write(JsonUtil.toSimpleResponse(false,"game ["+typeId+"] not available").getBytes());
            }
        }
        else if(session.action().equals("onLogin")){
            OnSession access = this.login(session.systemId(),(String) acc.property(OnAccess.PASSWORD),session);
            onSession(access,session);
            this.deploymentServiceProvider.onUpdated(Metrics.PASSWORD_COUNT,1);
        }
        else if(session.action().equals("onToken")){//exchange token
            boolean suc = this.context.validator().validateToken(acc.toMap());
            if(suc){
                ThirdPartyLogin _ox = new ThirdPartyLogin();
                _ox.distributionKey(session.systemId());
                thirdPartyLoginDatastore.load(_ox);
                OnSession onSession = this.login(session.systemId(),_ox.password(),session);
                onSession(onSession,session);
            }else{
                session.write(this.builder.create().toJson(new ResponseHeader("onToken", "invalid token", false)).getBytes());
            }
        }
        else if(session.action().equals("onTicket")){//validate game server connection
            if(this.context.validator().validateTicket(session.systemId(),acc.stub(),(String)acc.property(OnAccess.ACCESS_KEY))){
                PresenceContext ptx = new PresenceContext();
                ptx.successful(true);
                session.write(this.builder.create().toJson(ptx).getBytes());
            }
            else{
                session.write(this.builder.create().toJson(new ResponseHeader("onTicket", "invalid ticket", false)).getBytes());
            }
        }
        else if(session.action().equals("onTokenRegister")){
            Map<String,Object> params = acc.toMap();
            if(this.context.validator().validateToken(params)){
                AccessIndex _query = accessIndexService.get((String) acc.property("login"));
                if(_query!=null){
                    ThirdPartyLogin thirdPartyLogin = new ThirdPartyLogin((String)params.get("provider"),SystemUtil.oid(),"");
                    thirdPartyLogin.distributionKey(session.systemId());
                    thirdPartyLoginDatastore.createIfAbsent(thirdPartyLogin,false);
                    acc.property(OnAccess.PASSWORD,thirdPartyLogin.password());
                    Access user = createLogin(acc,session.systemId(),role,true,acc.name(),true);
                    user.emailAddress((String) params.get("email"));
                    user.activated(true);
                    userDatastore.update(user);
                    OnSession onSession = login(session.systemId(),thirdPartyLogin.password(),session);
                    onSession(onSession,session);
                }
                else{
                    session.write(builder.create().toJson(new ResponseHeader(session.action(),false,0,"login [" + acc.property("login") + "] cannot be registered","error")).getBytes());
                }
            }else{
                session.write(this.builder.create().toJson(new ResponseHeader("onToken", "invalid token", false)).getBytes());
            }
        }
        else if(session.action().equals("onRegister")){
            AccessIndex _query = accessIndexService.get((String) acc.property(OnAccess.LOGIN));
            if(_query==null){//double-check
                session.write(builder.create().toJson(new ResponseHeader(session.action(),false,0,"login [" + acc.property("login") + "] cannot be registered","error")).getBytes());
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
            ThirdPartyLogin _ox = new ThirdPartyLogin();
            _ox.distributionKey(session.systemId());
            if(thirdPartyLoginDatastore.load(_ox)&&_ox.deviceId().equals(deviceId)){
                OnSession access = this.login(session.systemId(),_ox.password(),session);
                onSession(access,session);
                this.deploymentServiceProvider.onUpdated(Metrics.DEVICE_COUNT,1);
            }
            else{
                session.write(JsonUtil.toSimpleResponse(false,"Device not registered").getBytes());
            }
        }
        else if(session.action().equals("onDeviceRegister")){
            String deviceId = (String) acc.property(OnAccess.DEVICE_ID);
            AccessIndex accessIndex = this.accessIndexService.get(deviceId);
            if(accessIndex!=null){
                ThirdPartyLogin thirdPartyLogin = new ThirdPartyLogin("device",SystemUtil.oid(),deviceId);
                thirdPartyLogin.distributionKey(session.systemId());
                thirdPartyLoginDatastore.createIfAbsent(thirdPartyLogin,false);
                acc.property("login",deviceId);
                acc.property("password",thirdPartyLogin.password());
                this.createLogin(acc,session.systemId(),role,true,"device",true);
                OnSession access = this.login(session.systemId(),thirdPartyLogin.password(),session);
                onSession(access,session);
            }
            else{
                session.write(this.builder.create().toJson(new ResponseHeader("onDevice","wrong device id", false)).getBytes());
            }
            this.deploymentServiceProvider.onUpdated(Metrics.DEVICE_COUNT,1);
        }
        else if(session.action().equals("onResetCode")){
            String code = this.deploymentServiceProvider.resetCode(session.trackId());
            if(this.deploymentServiceProvider.registerPostOffice().onEmail().send(session.trackId(),code)){
                session.write(JsonUtil.toSimpleResponse(true,"check email for code").getBytes());
            }
            else{
                session.write(JsonUtil.toSimpleResponse(false,"system error,try later").getBytes());
            }
        }
        else if(session.action().equals("onResetPassword")){
            String code = (String)acc.property(OnAccess.ACCESS_KEY);
            Access user = new User();
            user.distributionKey(session.systemId());
            if(!userDatastore.load(user)){
                session.write(JsonUtil.toSimpleResponse(false,"wrong user name").getBytes());
            }
            else{
                if(user.activated()&&this.deploymentServiceProvider.checkCode(code).equals(user.emailAddress())){
                    user.password(this.context.validator().hashPassword((String) acc.property(OnAccess.PASSWORD)));
                    userDatastore.update(user);
                    OnSession onSession = this.login(session.systemId(),(String) acc.property(OnAccess.PASSWORD),session);
                    onSession(onSession,session);
                }
                else{
                    session.write(this.builder.create().toJson(new ResponseHeader("onResetPassword", "Invalid recovery", false)).getBytes());
                }
            }
        }
        else if(session.action().equals("onDeveloper")){
            session.write(JsonUtil.toSimpleResponse(true,session.trackId()).getBytes());
        }
        else{
            throw new UnsupportedOperationException(session.action());
        }
    }
    private void onSession(OnSession access, Session session){
        if(access.successful()){
            PresenceContext ptx = new PresenceContext("onLogin");
            ptx.presence= access;
            List<Lobby> lobbyList = new ArrayList<>();
            lobbyList.add(this.context.lobby(this.lobbyId));
            ptx.lobbyList=(lobbyList);
            session.write(this.builder.create().toJson(ptx).getBytes());
            session.systemId(access.systemId());
            session.stub(access.stub());
            session.ticket(access.ticket());
        }
        else{
            session.write(this.builder.create().toJson(new ResponseHeader("reset","wrong user/password", false)).getBytes());
        }
    }
    private OnSession login(String systemId, String password, Session session){
        Access access = new User();
        access.distributionKey(systemId);
        OnSession _onSession = OnSessionTrack.PASSWORD_NOT_MATCHED;
        if(userDatastore.load(access)){
            access.routingNumber(session.routingNumber());
            _onSession=this.context.validator().validatePassword(access,password);
            _onSession.systemId(systemId);
        }
        return _onSession;
    }
    private Access createLogin(OnAccess payload,String systemId,String roleName,boolean validated,String validator,boolean primary){
        payload.property(OnAccess.SYSTEM_ID,systemId);
        payload.property(OnAccess.ACCESS_CONTROL,roleName);
        payload.property(OnAccess.VALIDATOR,validator);
        payload.property(OnAccess.VALIDATED,validated);
        payload.property(OnAccess.PRIMARY_USER,primary);
        payload.property(OnAccess.BALANCE,initialBalance);
        payload.property(OnAccess.ACTIVATED,activated);
        return userService.createUser(payload);
    }

    @Override
    public void onUpdated(OnLobby onLobby) {
        if(!onLobby.closed()){
            String[] ps = onLobby.typeId().split("-");
            onLobbyIndex.put(ps[0],onLobby);
            context.log("Lobby ["+onLobby.typeId()+"] is going to be live",OnLog.WARN);
        }
        else{
            String[] ps = onLobby.typeId().split("-");
            onLobbyIndex.remove(ps[0]);
            context.log("Lobby ["+onLobby.typeId()+"] is going to be offline",OnLog.WARN);
        }
    }
}
