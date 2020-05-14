package com.tarantula.platform.presence;

import com.tarantula.*;
import com.tarantula.platform.*;
import com.tarantula.platform.service.AccessIndexService;
import com.tarantula.platform.service.DeploymentServiceProvider;
import com.tarantula.platform.util.PresenceContextSerializer;
import com.tarantula.platform.util.RingBuffer;
import com.tarantula.platform.util.SystemUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Updated 12/25/2019
 */
public class UserManagementApplication extends TarantulaApplicationHeader{

    private String lobbyId;
    private boolean activated;
    private String role = "player";
    private double initialBalance;
    private AccessIndexService accessIndexService;
    private RingBuffer<Connection> cBuffer;
    private DeploymentServiceProvider deploymentServiceProvider;
    private boolean onApplication;
    @Override
    public void setup(ApplicationContext context) throws Exception {
        super.setup(context);
        this.cBuffer = new RingBuffer<>(new Connection[5]);
        Configuration configuration = this.context.configuration("setup");
        this.lobbyId = configuration.property("lobbyId");
        this.activated = Boolean.parseBoolean(configuration.property("activated"));
        this.initialBalance = Double.parseDouble(configuration.property("initialBalance"));
        this.role = configuration.property("roleName");
        builder.registerTypeAdapter(PresenceContext.class,new PresenceContextSerializer());
        this.accessIndexService = this.context.serviceProvider(AccessIndexService.NAME);
        deploymentServiceProvider = this.context.serviceProvider(DeploymentServiceProvider.NAME);
        this.onApplication = this.deploymentServiceProvider.deploymentMode()== DeploymentServiceProvider.Mode.APPLICATION;
        String root = configuration.property("root");
        String pwd = configuration.property("password");
        OnAccess onAccess = new OnAccessTrack();
        onAccess.property("login",root);
        onAccess.property("password",pwd);
        DataStore ds = this.context.dataStore("user");
        String rootId = ds.bucket()+Recoverable.PATH_SEPARATOR+SystemUtil.oid();
        AccessIndex accessIndex = accessIndexService.set(onAccess.property("login"),rootId);
        if(accessIndex!=null){
            createLogin(onAccess, rootId,"root",false,"password");
        }
        this.context.registerRecoverableListener(new UserPortableRegistry()).addRecoverableFilter(UserPortableRegistry.ON_ACCESS_CID,(a)->{
            createLogin((OnAccess)a,a.distributionKey(),role,false,"password");
        });
        this.context.log("User management application started on tag ["+descriptor.tag()+"] with application mode ["+onApplication+"]",OnLog.INFO);
    }
    @Override
    public void callback(Session session,byte[] payload) throws Exception {
        OnAccess acc = builder.create().fromJson(new String(payload).trim(),OnAccess.class);
        if(session.action().equals("onLogin")){
            OnSession access = this.login(session.systemId(),acc.property("password"),session);
            onSession(access,session);
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
            if(this.context.validator().validateTicket(session.systemId(),acc.stub(),acc.accessKey())){
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
                AccessIndex _query = accessIndexService.set(acc.property("login"),session.systemId());
                if(_query!=null){
                    createLogin(acc,session.systemId(),role,true,acc.name());
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
            AccessIndex _query = accessIndexService.set(acc.property("login"),session.systemId());
            if(_query==null){
                session.write(builder.create().toJson(new ResponseHeader(session.action(),false,0,"login [" + acc.property("login") + "] cannot be registered","error")).getBytes(),this.descriptor.responseLabel());
            }
            else{
                Access access = this.createLogin(acc,session.systemId(),role,false,"password");
                session.systemId(access.distributionKey());
                OnSession _onSession = this.login(session.systemId(),acc.property("password"),session);
                this.onSession(_onSession,session);
            }
        }
        else if(session.action().equals("onDevice")){
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
                    this.createLogin(acc,session.trackId(),role,true,"device");
                    OnSession access = this.login(session.trackId(),acc.property("password"),session);
                    onSession(access,session);
                }
                else{
                    session.write(this.builder.create().toJson(new ResponseHeader("onDevice","wrong device id", false)).getBytes(),this.descriptor.responseLabel());
                }
            }
        }
        else if(session.action().equals("onResetPassword")){
            if(this.deploymentServiceProvider.checkCode(acc.accessKey()).equals(acc.property("login"))){
                Access user = new User();
                user.distributionKey(session.systemId());
                if(this.context.dataStore("user").load(user)){
                    user.password(this.context.validator().hashPassword(acc.property("password")));
                    this.context.dataStore("user").update(user);
                    OnSession onSession = this.login(session.systemId(),acc.property("password"),session);
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
            ptx.connection = cBuffer.pop();
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
        if(this.context.dataStore("user").load(access)){
            access.routingNumber(session.routingNumber());
            _onSession=this.context.validator().validatePassword(access,password);
            _onSession.systemId(systemId);
        }
        return _onSession;
    }
    private Access createLogin(OnAccess payload,String systemId,String roleName,boolean validated,String validator){
        DataStore ds = this.context.dataStore("user");
        //this.context.log("User Create->"+payload.header("login")+"<>"+systemId,OnLog.INFO);
        Access acc = new User(payload.property("login"),validated,validator);
        acc.distributionKey(systemId);
        acc.password(validated?"":this.context.validator().hashPassword(payload.property("password")));
        acc.active(this.activated);//if false do email validation
        acc.role(roleName);
        if(ds.create(acc)){
            PresenceIndex px = new PresenceIndex(initialBalance);
            px.distributionKey(acc.distributionKey());
            this.context.dataStore("presence").create(px);
        }
        return acc;
    }
    @Override
    public void onState(Connection c) {
        if(c.type().equals(Connection.WEB_SOCKET)){
            this.context.log(c.type()+"/"+c.serverId()+"/"+(c.disabled()?"closed":"open")+"/ on user management service application",OnLog.WARN);
            onWebSocket(c);
        }
    }
    private void onWebSocket(Connection c) {
        if(!c.disabled()){
            if(!cBuffer.push(c)){
                cBuffer.reset(((ca,limit)->{
                    Connection[] cn = new Connection[ca.length*2];
                    for(int i=0;i<limit;i++){
                        cn[i]=ca[i];
                    }
                    cn[limit]=c;
                    return cn;
                }));
            }
        }
        else{
            cBuffer.reset((ca,limit)->{
                Connection[] cn = new Connection[ca.length];
                int r=0;
                for(int i=0;i<limit;i++){
                    if(!(ca[i].serverId().equals(c.serverId()))){
                        cn[r++]=ca[i];
                    }
                }
                return cn;
            });
        }
    }
}
