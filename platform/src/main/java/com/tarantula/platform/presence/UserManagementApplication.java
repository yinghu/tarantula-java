package com.tarantula.platform.presence;

import com.icodesoftware.*;
import com.icodesoftware.service.*;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.*;
import com.tarantula.platform.service.metrics.AccessMetrics;
import com.tarantula.platform.util.PresenceContextSerializer;
import com.tarantula.platform.util.SystemUtil;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class UserManagementApplication extends TarantulaApplicationHeader implements Configurable.Listener<OnLobby>{

    //private String lobbyId;
    private boolean activated;
    private int trialDays;

    //private double initialBalance;
    private AccessIndexService accessIndexService;
    private UserService userService;
    private DeploymentServiceProvider deploymentServiceProvider;

    private TokenValidatorProvider tokenValidatorProvider;

    private ConcurrentHashMap<String,OnLobby> onLobbyIndex;



    @Override
    public void setup(ApplicationContext context) throws Exception {
        super.setup(context);
        Configuration configuration = this.context.configuration("account");
        this.activated = (boolean)configuration.property("activated");
        this.trialDays = ((Number)configuration.property("trialDays")).intValue();
        builder.registerTypeAdapter(PresenceContext.class,new PresenceContextSerializer());
        this.accessIndexService = this.context.serviceProvider(AccessIndexService.NAME);
        deploymentServiceProvider = this.context.serviceProvider(DeploymentServiceProvider.NAME);
        userService = this.context.serviceProvider(UserService.NAME);
        this.tokenValidatorProvider = this.context.serviceProvider(TokenValidatorProvider.NAME);
        this.onLobbyIndex = new ConcurrentHashMap<>();
        String root = this.context.node().clusterNameSuffix()+"/"+(String)configuration.property("root");
        String pwd = (String) configuration.property("password");
        OnAccess onAccess = new OnAccessTrack();
        onAccess.property("login",root);
        onAccess.property("password",pwd);
        accessIndexService.set("serverPush",AccessIndex.SYSTEM_INDEX);
        AccessIndex accessIndex = accessIndexService.set(root,AccessIndex.USER_INDEX);

        if(accessIndex!=null){
            Access user = createLogin(onAccess,accessIndex.oid(),AccessControl.root.name(),false,"password",true);
            LocalDateTime loc = LocalDateTime.now();
            Membership membership = new Membership();
            membership.startTimestamp(TimeUtil.toUTCMilliseconds(loc));
            membership.endTimestamp(TimeUtil.toUTCMilliseconds(loc.plusYears(10)));
            membership.timestamp(TimeUtil.toUTCMilliseconds(loc));
            membership.trial(true);
            this.userService.createAccount(user,membership);
        }
        this.context.registerRecoverableListener(new UserPortableRegistry()).addRecoverableFilter(UserPortableRegistry.ON_ACCESS_CID,(a)->{
            //add player user to the account
            OnAccess uadded = (OnAccess)a;
            if(uadded.property("command").equals("onAddUser")){
                createLogin(uadded.owner(),uadded,uadded.distributionKey(),AccessControl.player.name(),false,"password",false);
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
            ic.lobbyList = this.context.index();
            session.write(builder.create().toJson(ic).getBytes());
        }
        else if(session.action().equals("onAvailable")){
            String typeId = session.trackId();
            if(onLobbyIndex.containsKey(typeId)){
                session.write(JsonUtil.toSimpleResponse(true,"").getBytes());
            }
            else{
                session.write(JsonUtil.toSimpleResponse(false,"game ["+typeId+"] not available").getBytes());
            }
        }
        else if(session.action().equals("onLogin")){
            //this.context.log("USER ID->"+session.id(),OnLog.WARN);
            OnSession access = this.login(session.oid(),(String) acc.property(OnAccess.PASSWORD),session);
            userService.onUpdated(AccessMetrics.ACCESS_WEB_LOGIN_COUNT,1);
            onSession(access,session);
        }
        else if(session.action().equals("onToken")){//exchange token
            Map<String,Object> params = acc.toMap();
            params.put(OnAccess.SYSTEM_ID,session.systemId());
            boolean suc = this.context.validator().validateToken(params);
            LoginProvider _ox = userService.loginProvider(session.systemId());
            if(suc && _ox!=null ){
                OnSession onSession = this.login(session.oid(),_ox.password(),session);
                onSession(onSession,session);
            }else{
                session.write(this.builder.create().toJson(new ResponseHeader("onToken", "invalid token", false)).getBytes());
            }
        }
        else if(session.action().equals("onTicket")){//validate game server connection
            session.stub(acc.stub());
            session.ticket((String)acc.property(OnAccess.ACCESS_KEY));
            if(this.context.validator().validateTicket(session)){
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
            params.put(OnAccess.SYSTEM_ID,session.systemId());
            if(this.context.validator().validateToken(params)){
                AccessIndex _query = accessIndexService.get((String) acc.property("login"));
                if(_query!=null){
                    ThirdPartyLogin thirdPartyLogin = new ThirdPartyLogin((String)params.get("provider"),SystemUtil.oid(),"");
                    thirdPartyLogin.distributionKey(session.systemId());
                    userService.createLoginProvider(thirdPartyLogin);
                    acc.property(OnAccess.PASSWORD,thirdPartyLogin.password());
                    createLogin(acc,session.systemId(),AccessControl.player.name(),true,acc.name(),true);
                    OnSession onSession = login(session.oid(),thirdPartyLogin.password(),session);
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
                Access access = this.createLogin(acc,session.systemId(),AccessControl.admin.name(),false,"password",true);
                Membership membership = new Membership();
                LocalDateTime loc = LocalDateTime.now();
                membership.trial(true);
                membership.startTimestamp(TimeUtil.toUTCMilliseconds(loc));
                membership.endTimestamp(TimeUtil.toUTCMilliseconds(loc.plusDays(trialDays)));
                this.userService.createAccount(access,membership);
                session.systemId(access.distributionKey());
                OnSession _onSession = this.login(session.oid(),(String) acc.property(OnAccess.PASSWORD),session);
                this.onSession(_onSession,session);
            }
        }
        else if(session.action().equals("onDevice")){
            String deviceId = (String) acc.property(OnAccess.DEVICE_ID);
            LoginProvider _ox = userService.loginProvider(session.systemId());
            if(_ox!=null && _ox.clientId().equals(deviceId)){
                OnSession access = this.login(session.oid(),_ox.password(),session);
                onSession(access,session);
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
                userService.createLoginProvider(thirdPartyLogin);
                acc.property("login",deviceId);
                acc.property("password",thirdPartyLogin.password());
                this.createLogin(acc,session.systemId(),AccessControl.player.name(),true,"device",true);
                OnSession access = this.login(session.oid(),thirdPartyLogin.password(),session);
                onSession(access,session);
            }
            else{
                session.write(this.builder.create().toJson(new ResponseHeader("onDevice","wrong device id", false)).getBytes());
            }
        }
        else if(session.action().equals("onResetCode")){
            String code = this.deploymentServiceProvider.resetCode(session.trackId());
            if(this.context.postOffice().onEmail(session.trackId()).send(code)){
                session.write(JsonUtil.toSimpleResponse(true,"check email for code").getBytes());
            }
            else{
                session.write(JsonUtil.toSimpleResponse(false,"system error,try later").getBytes());
            }
        }
        else if(session.action().equals("onResetPassword")){
            String code = (String)acc.property(OnAccess.ACCESS_KEY);
            Access user = this.userService.loadUser(session.oid());
            if(user==null){
                session.write(JsonUtil.toSimpleResponse(false,"wrong user name").getBytes());
            }
            else{
                if(user.activated()&&this.deploymentServiceProvider.checkCode(code).equals(user.emailAddress())){
                    user.password(this.context.validator().hashPassword((String) acc.property(OnAccess.PASSWORD)));
                    user.update();
                    OnSession onSession = this.login(session.oid(),(String) acc.property(OnAccess.PASSWORD),session);
                    onSession(onSession,session);
                }
                else{
                    session.write(this.builder.create().toJson(new ResponseHeader("onResetPassword", "Invalid recovery", false)).getBytes());
                }
            }
        }
        else if(session.action().equals("onDeveloper")){
            String deviceId = (String) acc.property(OnAccess.DEVICE_ID);
            LoginProvider _ox = userService.loginProvider(session.systemId());
            if(_ox!=null && _ox.clientId().equals(deviceId)){
                OnSession access = this.login(session.oid(),_ox.password(),session);
                onSession(access,session);
            }
            else{
                session.write(JsonUtil.toSimpleResponse(false,"Developer not registered").getBytes());
            }
            userService.onUpdated(AccessMetrics.ACCESS_DEVELOPER_LOGIN_COUNT,1);
        }
        else if(session.action().equals("onDeveloperRegister")){
            String deviceId = (String) acc.property(OnAccess.DEVICE_ID);
            AccessIndex accessIndex = this.accessIndexService.get(deviceId);
            if(accessIndex!=null){
                //create association with the master account
                GameCluster gameCluster = this.tokenValidatorProvider.validateGameClusterAccessKey(session.trackId());
                String owner = gameCluster.accountId;//(String)gameCluster.property(GameCluster.OWNER);
                ThirdPartyLogin developerLogin = new ThirdPartyLogin("developer",SystemUtil.oid(),deviceId);
                developerLogin.distributionKey(session.systemId());
                acc.property("login",deviceId);
                acc.property("password",developerLogin.password());
                this.createLogin(owner,acc,session.systemId(),AccessControl.admin.name(),true,"key",false);
                userService.createLoginProvider(developerLogin);
                OnSession access = this.login(session.oid(),developerLogin.password(),session);
                onSession(access,session);
            }
            else{
                session.write(this.builder.create().toJson(new ResponseHeader("onDeveloper","wrong device id", false)).getBytes());
            }
            userService.onUpdated(AccessMetrics.ACCESS_DEVELOPER_LOGIN_COUNT,1);
        }
        else{
            throw new UnsupportedOperationException(session.action());
        }
    }
    private boolean onSession(OnSession access, Session session){
        if(access.successful()){
            session.write(access.toJson().toString().getBytes());
            session.systemId(access.systemId());
            session.stub(access.stub());
            session.ticket(access.ticket());
        }
        else{
            session.write(JsonUtil.toSimpleResponse(false,"wrong user/password").getBytes());
        }
        return access.successful();
    }
    private OnSession login(String systemId, String password, Session session){
        Access access = this.userService.loadUser(systemId);
        OnSession _onSession = OnSessionTrack.PASSWORD_NOT_MATCHED;
        if(access!=null){
            access.routingNumber(session.routingNumber());
            _onSession = this.context.validator().validatePassword(access,password);
            _onSession.oid(systemId);
        }
        return _onSession;
    }

    private Access createLogin(String accountId,OnAccess payload,String systemId,String roleName,boolean validated,String validator,boolean primary){
        payload.property(OnAccess.SYSTEM_ID,systemId);
        payload.property(OnAccess.ACCESS_CONTROL,roleName);
        payload.property(OnAccess.VALIDATOR,validator);
        payload.property(OnAccess.VALIDATED,validated);
        payload.property(OnAccess.PRIMARY_USER,primary);
        payload.property(OnAccess.ACTIVATED,activated);
        return userService.createUser(accountId,payload);
    }
    private Access createLogin(OnAccess payload,String systemId,String roleName,boolean validated,String validator,boolean primary){
        payload.property(OnAccess.SYSTEM_ID,systemId);
        payload.property(OnAccess.ACCESS_CONTROL,roleName);
        payload.property(OnAccess.VALIDATOR,validator);
        payload.property(OnAccess.VALIDATED,validated);
        payload.property(OnAccess.PRIMARY_USER,primary);
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
