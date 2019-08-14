package com.tarantula.platform.presence;

import com.tarantula.*;
import com.tarantula.platform.*;
import com.tarantula.platform.util.PresenceContextSerializer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Developer: YINGHU LU
 * Updated 6/14/2019
 */
public class UserManagementApplication extends TarantulaApplicationHeader{

    private String lobbyId;
    private boolean activated;
    private double initialBalance;
    private AccessIndexService accessIndexService;
    //private DeploymentServiceProvider deploymentServiceProvider;
    private PostOffice postOffice;

    @Override
    public void setup(ApplicationContext context) throws Exception {
        super.setup(context);
        Configuration configuration = this.context.configuration("setup");
        this.lobbyId = configuration.property("lobbyId");
        this.activated = Boolean.parseBoolean(configuration.property("activated"));
        this.initialBalance = Double.parseDouble(configuration.property("initialBalance"));
        builder.registerTypeAdapter(PresenceContext.class,new PresenceContextSerializer());
        this.accessIndexService = this.context.serviceProvider(AccessIndexService.NAME);
        //this.deploymentServiceProvider = this.context.serviceProvider(DeploymentServiceProvider.NAME);
        postOffice = this.context.postOffice();
        this.context.log("User management application started",OnLog.INFO);
    }
    @Override
    public void callback(Session session,byte[] payload) throws Exception {
        OnAccess acc = builder.create().fromJson(new String(payload).trim(),OnAccess.class);
        if(session.action().equals("onLogin")){
            OnSession access = this.login(session.systemId(),acc.header("password"),session);
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
                OnStatistics delta = new OnStatisticsTrack(this.context.onStatistics().leaderBoardHeader(),access.systemId());
                delta.xpDelta(1);
                delta.entryList(new Statistics.Entry[]{new StatisticsEntry("LoginCount",1)});
                this.context.publish(this.context.routingKey(delta.owner(),"level"),delta);
                this.context.onStatistics().value("Login",1);
            }
            else{
                session.write(this.builder.create().toJson(new ResponseHeader("login", access.message(), false)).getBytes(),this.descriptor.responseLabel());
            }
        }
        else if(session.action().equals("onTicket")){
            if(this.context.validator().validTicket(session.systemId(),acc.stub(),acc.accessKey())){
                OnSession onSession = this.context.validator().token(session.systemId(),acc.stub(),20);
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
        /**
        else if(session.action().equals("onToken")){
            OnAccess tcc = builder.create().fromJson(new String(payload).trim(),OnAccess.class);
            TokenValidator.OAuthVendor auth = this.context.validator().vendor(tcc.name());
            Map<String,Object> auser = new HashMap<>();
            auser.put("token",tcc.accessKey());
            if(auth.validate(auser)){
                //AccessIndex aix = accessIndexService.get(auser.get("email").toString());
                OnSession oss;
                if(session.systemId()!=null){
                    //String sysId = aix.distributionKey();
                    oss = this.login(session.systemId(),"password",session);
                }
                else{
                    oss = this.createAndLogin(auser.get("email").toString(),auser.get("fullName").toString(),session);
                }
                if(oss.successful()){
                    PresenceContext ptx = new PresenceContext("onLogin");
                    ptx.presence= oss;
                    List<Lobby> lobbyList = new ArrayList();
                    lobbyList.add(this.context.lobby(this.lobbyId));
                    ptx.lobbyList=(lobbyList);
                    session.write(this.builder.create().toJson(ptx).getBytes(),this.descriptor.responseLabel());
                }
                else{
                    //never happen
                    session.write(this.builder.create().toJson(new ResponseHeader("onToken", "invalid token", false)).getBytes(),this.descriptor.responseLabel());
                }
            }
            else{
                session.write(this.builder.create().toJson(new ResponseHeader("onToken", "invalid token", false)).getBytes(),this.descriptor.responseLabel());
            }
        }**/
        else if(session.action().equals("onRegister")){
            this.register(session,acc);
        }
        else if(session.action().equals("onReset")){
            session.write(payload,this.descriptor.responseLabel());
        }
        else{
            throw new UnsupportedOperationException(session.action());
        }
    }
    private OnSession createAndLogin(String email,String nickname,Session session) throws Exception{
        DataStore ds = this.context.dataStore("user");
        AccessIndex _query = accessIndexService.set(email,session.trackId());
        if(_query!=null){
            Access acc = new AccessTrack(_query.owner());
            acc.oid(_query.oid());
            acc.bucket(_query.bucket());
            acc.password(this.context.validator().hashPassword("password"));
            acc.active(this.activated);//if false do email validation
            if(ds.create(acc)){
                PresenceIndex px = new PresenceIndex(initialBalance);
                px.distributionKey(acc.distributionKey());
                this.context.dataStore("presence").create(px);
                ProfileTrack _p = new ProfileTrack(acc.bucket(),acc.oid());
                _p.nickname(nickname);
                _p.emailAddress(email);
                _p.avatar("content/avatar/"+acc.distributionKey());
                _p.video("content/video/"+acc.distributionKey());
                this.context.dataStore("profile").create(_p);
                OnSession onSession = new OnSessionTrack();
                onSession.distributionKey(acc.distributionKey());
                this.context.dataStore("session").create(onSession);
                session.systemId(acc.distributionKey());
            }
        }
        return login(_query.distributionKey(),"password",session);
    }
    private OnSession login(String systemId,String password,Session session){
        Access access = new AccessTrack();
        access.distributionKey(systemId);
        OnSession _onSession = OnSessionTrack.PASSWORD_NOT_MATCHED;
        if(this.context.dataStore("user").load(access)){
            access.routingNumber(session.routingNumber());
            _onSession=this.context.validator().validPassword(access,password,session.clientId());
            _onSession.systemId(systemId);
            ResponseHeader resp = new ResponseHeader(session.action(), "User [" + access.login() + "] signed in",true);
            postOffice.onNotification(this.builder.create().toJson(resp).getBytes(),"presence/notice");
        }
        return _onSession;
    }
    private void register(Session session,OnAccess payload) throws Exception{
        DataStore ds = this.context.dataStore("user");
        AccessIndex _query = accessIndexService.set(payload.header("login"),session.systemId());
        if(_query!=null){
            Access acc = new AccessTrack(_query.owner());
            acc.bucket(_query.bucket());
            acc.oid(_query.oid());
            acc.password(this.context.validator().hashPassword(payload.header("password")));
            acc.active(this.activated);//if false do email validation
            if(ds.create(acc)){
                PresenceIndex px = new PresenceIndex(initialBalance);
                px.distributionKey(acc.distributionKey());
                this.context.dataStore("presence").create(px);
                ProfileTrack _p = new ProfileTrack(acc.bucket(),acc.oid());
                _p.nickname(payload.header("nickname")!=null?payload.header("nickname"):acc.login());
                _p.emailAddress("n/a");
                _p.avatar("content/avatar/"+acc.distributionKey());
                _p.video("content/video/"+acc.distributionKey());
                this.context.dataStore("profile").create(_p);
                OnSession onSession = new OnSessionTrack();
                onSession.oid(acc.oid());
                onSession.bucket(acc.bucket());
                this.context.dataStore("session").create(onSession);
                session.systemId(acc.distributionKey());
                ResponseHeader resp = new ResponseHeader(session.action(),"User [" + acc.login() + "] registered",true);
                session.write(builder.create().toJson(resp).getBytes(),this.descriptor.responseLabel());

            }else{
                session.write(builder.create().toJson(new ResponseHeader(session.action(),false,0,"login [" + acc.login() + "] cannot be registered","error")).getBytes(),this.descriptor.responseLabel());
            }
        }
        else{
            session.write(builder.create().toJson(new ResponseHeader(session.action(),false,0,"login [" + payload.header("login") + "] cannot be registered","error")).getBytes(),this.descriptor.responseLabel());
        }
    }
}
