package com.tarantula.platform.presence;

import com.tarantula.*;
import com.tarantula.platform.*;
import com.tarantula.platform.service.AccessIndexService;
import com.tarantula.platform.util.PresenceContextSerializer;
import com.tarantula.platform.util.ResponseSerializer;
import com.tarantula.platform.util.SystemUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Updated 8/27/2019
 */
public class UserManagementApplication extends TarantulaApplicationHeader{

    private String lobbyId;
    private boolean activated;
    private String role = "player";
    private double initialBalance;
    private AccessIndexService accessIndexService;
    private PostOffice postOffice;

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
        postOffice = this.context.postOffice();
        String root = configuration.property("root");
        String pwd = configuration.property("password");
        OnAccess onAccess = new OnAccessTrack();
        onAccess.property("login",root);
        onAccess.property("password",pwd);
        onAccess.property("nickname","super user");
        DataStore ds = this.context.dataStore("user");
        String rootId = ds.bucket()+Recoverable.PATH_SEPARATOR+SystemUtil.oid();
        AccessIndex accessIndex = accessIndexService.set(onAccess.property("login"),rootId);
        if(accessIndex!=null){
            createLogin(onAccess, rootId,"root");
        }
        this.context.registerRecoverableListener(new UserPortableRegistry()).addRecoverableFilter(UserPortableRegistry.ON_ACCESS_CID,(a)->{
            //this.context.log(a.distributionKey(),OnLog.INFO);
            //this.context.log(a.toString(),OnLog.INFO);
            createLogin((OnAccess)a,a.distributionKey(),role);
        });
        this.context.log("User management application started on tag ["+descriptor.tag()+"]",OnLog.INFO);
    }
    @Override
    public void callback(Session session,byte[] payload) throws Exception {
        OnAccess acc = builder.create().fromJson(new String(payload).trim(),OnAccess.class);
        if(session.action().equals("onLogin")){
            OnSession access = this.login(session.systemId(),acc.property("password"),session);
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
                OnStatistics delta = this.context.statistics().value("Login",1);
                delta.xpDelta(1);
                delta.owner(session.systemId());
                delta.onEntry("LoginCount",1);
                this.postOffice.onTag(Level.LEVEL_TAG).send(delta.owner(),delta);
            }
            else{
                session.write(this.builder.create().toJson(new ResponseHeader("login", access.message(), false)).getBytes(),this.descriptor.responseLabel());
            }
        }
        else if(session.action().equals("onTicket")){
            if(this.context.validator().validateTicket(session.systemId(),acc.stub(),acc.accessKey())){
                OnSession onSession = this.context.validator().token(session.systemId(),acc.stub());//web socket request
                onSession.successful(true);
                PresenceContext ptx = new PresenceContext();
                ptx.successful(true);
                ptx.presence = onSession;
                session.write(this.builder.create().toJson(ptx).getBytes(),this.descriptor.responseLabel());
            }
            else{
                session.write(this.builder.create().toJson(new ResponseHeader("onTicket", "invalid ticket", false)).getBytes(),this.descriptor.responseLabel());
            }
        }
        else if(session.action().equals("onRegister")){
            AccessIndex _query = accessIndexService.set(acc.property("login"),session.systemId());
            if(_query==null){
                session.write(builder.create().toJson(new ResponseHeader(session.action(),false,0,"login [" + acc.property("login") + "] cannot be registered","error")).getBytes(),this.descriptor.responseLabel());
            }
            else{
                Access access = this.createLogin(acc,session.systemId(),role);
                session.systemId(access.distributionKey());
                ResponseHeader resp = new ResponseHeader(session.action(),"User [" + access.login() + "] registered",true);
                session.write(builder.create().toJson(resp).getBytes(),this.descriptor.responseLabel());
            }
        }
        else if(session.action().equals("onReset")){
            String deviceId = acc.property("deviceId");
            if(session.systemId()!=null){//registered
                OnSession access = this.login(session.systemId(),"password",session);
                onSession(access,session);
            }
            else{
                AccessIndex accessIndex = this.accessIndexService.set(deviceId,session.trackId());
                if(accessIndex!=null){
                    acc.property("login",deviceId);
                    acc.property("password","password");
                    acc.property("nickname","Player");
                    this.createLogin(acc,session.trackId(),role);
                    OnSession access = this.login(session.trackId(),acc.property("password"),session);
                    onSession(access,session);
                }
                else{
                    session.write(this.builder.create().toJson(new ResponseHeader("reset","wrong device id", false)).getBytes(),this.descriptor.responseLabel());
                }
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
            OnStatistics delta = this.context.statistics().value("Login",1);
            delta.xpDelta(1);
            delta.owner(session.systemId());
            delta.onEntry("LoginCount",1);
            this.postOffice.onTag(Level.LEVEL_TAG).send(delta.owner(),delta);
        }
        else{
            session.write(this.builder.create().toJson(new ResponseHeader("reset","wrong user/password", false)).getBytes(),this.descriptor.responseLabel());
        }
    }
    private OnSession login(String systemId,String password,Session session){
        Access access = new AccessTrack();
        access.distributionKey(systemId);
        OnSession _onSession = OnSessionTrack.PASSWORD_NOT_MATCHED;
        if(this.context.dataStore("user").load(access)){
            access.routingNumber(session.routingNumber());
            _onSession=this.context.validator().validatePassword(access,password);
            _onSession.systemId(systemId);
            ResponseHeader resp = new ResponseHeader(session.action(), "User [" + access.login() + "] signed in",true);
            //postOffice.onTopic().send("presence/notice?login",this.builder.create().toJson(resp).getBytes());
        }
        return _onSession;
    }
    private Access createLogin(OnAccess payload,String systemId,String roleName){
        DataStore ds = this.context.dataStore("user");
        //this.context.log("User Create->"+payload.header("login")+"<>"+systemId,OnLog.INFO);
        Access acc = new AccessTrack(payload.property("login"));
        acc.distributionKey(systemId);
        acc.password(this.context.validator().hashPassword(payload.property("password")));
        acc.active(this.activated);//if false do email validation
        acc.role(roleName);
        if(ds.create(acc)){
            PresenceIndex px = new PresenceIndex(initialBalance);
            px.distributionKey(acc.distributionKey());
            this.context.dataStore("presence").create(px);
            ProfileTrack _p = new ProfileTrack(acc.bucket(),acc.oid());
            _p.nickname(payload.property("nickname")!=null?payload.property("nickname"):acc.login());
            _p.emailAddress("n/a");
            _p.avatar("content/avatar/"+acc.distributionKey());
            this.context.dataStore("profile").create(_p);
        }
        return acc;
    }
}
