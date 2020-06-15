package com.tarantula.platform.presence;

import com.tarantula.*;
import com.tarantula.platform.*;
import com.tarantula.platform.service.AccessIndexService;
import com.tarantula.platform.service.DeploymentServiceProvider;
import com.tarantula.platform.service.Metrics;
import com.tarantula.platform.service.TokenValidatorProvider;
import com.tarantula.platform.util.PresenceContextSerializer;
import com.tarantula.platform.util.SystemUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Updated 5/14/2020
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

    private boolean onApplication;
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
        this.onApplication = this.deploymentServiceProvider.deploymentMode()== DeploymentServiceProvider.Mode.APPLICATION;
        String root = configuration.property("root");
        String pwd = configuration.property("password");
        OnAccess onAccess = new OnAccessTrack();
        onAccess.property("login",root);
        onAccess.property("password",pwd);
        uDatastore = this.context.dataStore(Access.DataStore);
        pDatastore = this.context.dataStore(Presence.DataStore);
        aDatastore = this.context.dataStore(Account.DataStore);
        DataStore mDatastore = this.context.dataStore(Subscription.DataStore);
        String rootId = uDatastore.bucket()+Recoverable.PATH_SEPARATOR+SystemUtil.oid();
        AccessIndex accessIndex = accessIndexService.set((String) onAccess.property("login"),rootId);
        if(accessIndex!=null){
            Access user = createLogin(onAccess, rootId,AccessControl.root.name(),false,"password",true);
            Account acc = new UserAccount();
            acc.distributionKey(user.distributionKey());
            acc.trial(false);
            acc.subscribed(true);
            LocalDateTime loc = LocalDateTime.now();
            acc.timestamp(SystemUtil.toUTCMilliseconds(loc));
            aDatastore.create(acc);
            Membership membership = new Membership();
            membership.distributionKey(user.distributionKey());
            membership.startTimestamp(SystemUtil.toUTCMilliseconds(loc));
            membership.endTimestamp(SystemUtil.toUTCMilliseconds(loc.plusMonths(12)));
            membership.timestamp(SystemUtil.toUTCMilliseconds(loc));
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
                    account.timestamp(SystemUtil.toUTCMilliseconds(LocalDateTime.now()));
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
        this.context.log("User management application started on tag ["+descriptor.tag()+"] with application mode ["+onApplication+"]",OnLog.INFO);
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
                OnSession onSession = this.login(session.systemId(),"",session);
                onSession(onSession,session);
            }else{
                session.write(this.builder.create().toJson(new ResponseHeader("onToken", "invalid token", false)).getBytes(),this.descriptor.responseLabel());
            }
        }
        else if(session.action().equals("onTicket")){//validate client web socket connection
            if(this.context.validator().validateTicket(session.systemId(),acc.stub(),(String)acc.property(OnAccess.ACCESS_KEY))){
                OnSession onSession = this.context.validator().token(session.systemId(),acc.stub());//web socket request
                onSession.successful(true);
                PresenceContext ptx = new PresenceContext();
                ptx.successful(true);
                ptx.presence = onSession;
                session.write(this.builder.create().toJson(ptx).getBytes(),this.descriptor.responseLabel()+"?onTicket");
            }
            else{
                session.write(this.builder.create().toJson(new ResponseHeader("onTicket", "invalid ticket", false)).getBytes(),this.descriptor.responseLabel());
            }
        }
        else if(session.action().equals("onTokenRegister")){
            if(this.context.validator().validateToken(acc.toMap())){
                AccessIndex _query = accessIndexService.set((String) acc.property("login"),session.systemId());
                if(_query!=null){
                    createLogin(acc,session.systemId(),role,true,acc.name(),true);
                    OnSession onSession = login(session.systemId(),"",session);
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
            AccessIndex _query = accessIndexService.set((String) acc.property("login"),session.systemId());
            if(_query==null){
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
            String deviceId = (String) acc.property("deviceId");
            if(session.systemId()!=null){//registered
                OnSession access = this.login(session.systemId(),deviceId,session);
                onSession(access,session);
            }
            else{
                AccessIndex accessIndex = this.accessIndexService.set(deviceId,session.trackId());
                if(accessIndex!=null){
                    acc.property("login",deviceId);
                    acc.property("password",deviceId);
                    this.createLogin(acc,session.trackId(),role,true,"device",true);
                    OnSession access = this.login(session.trackId(),(String) acc.property(OnAccess.PASSWORD),session);
                    onSession(access,session);
                }
                else{
                    session.write(this.builder.create().toJson(new ResponseHeader("onDevice","wrong device id", false)).getBytes(),this.descriptor.responseLabel());
                }
            }
            this.deploymentServiceProvider.onUpdated(Metrics.DEVICE_COUNT,1);
        }
        else if(session.action().equals("onResetPassword")){
            if(this.deploymentServiceProvider.checkCode((String)acc.property(OnAccess.ACCESS_KEY)).equals(acc.property("login"))){
                Access user = new User();
                user.distributionKey(session.systemId());
                if(uDatastore.load(user)){
                    user.password(this.context.validator().hashPassword((String) acc.property(OnAccess.PASSWORD)));
                    uDatastore.update(user);
                    OnSession onSession = this.login(session.systemId(),(String) acc.property(OnAccess.PASSWORD),session);
                    onSession(onSession,session);
                }
                else{
                    session.write(this.builder.create().toJson(new ResponseHeader("onResetPassword", "invalid user name", false)).getBytes(),this.descriptor.responseLabel());
                }
            }
            else{
                session.write(this.builder.create().toJson(new ResponseHeader("onResetPassword", "invalid token", false)).getBytes(),this.descriptor.responseLabel());
            }
        }
        else{
            throw new UnsupportedOperationException(session.action());
        }
    }
    private void onSession(OnSession access,Session session){
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
    private OnSession login(String systemId,String password,Session session){
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
        acc.password(validated?"":this.context.validator().hashPassword((String) payload.property(OnAccess.PASSWORD)));
        acc.active(this.activated);//if false do email validation
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
}
