package com.tarantula.platform.presence;

import com.google.gson.JsonObject;
import com.icodesoftware.Session;
import com.tarantula.*;
import com.tarantula.platform.*;

import com.tarantula.platform.service.DeploymentServiceProvider;
import com.tarantula.platform.service.OnLobby;
import com.tarantula.platform.service.TokenValidatorProvider;
import com.tarantula.platform.util.*;

import java.util.ArrayList;


/**
 * Developer: YINGHU LU
 * Date: updated 5/20/2020
 */
public class PresenceApplication extends TarantulaApplicationHeader implements OnLobby.Listener{

    private DeploymentServiceProvider deploymentServiceProvider;
    private TokenValidatorProvider tokenValidatorProvider;
    private DataStore userDs;
    private DataStore accountDs;
    private DataStore memberDs;
    private LiveGameContext liveGameContext;
    private Connection connection;
    @Override
    public void setup(ApplicationContext context) throws Exception {
        super.setup(context);
        builder.registerTypeAdapter(PresenceContext.class, new PresenceContextSerializer());
        this.tokenValidatorProvider = this.context.serviceProvider(TokenValidatorProvider.NAME);
        this.deploymentServiceProvider = this.context.serviceProvider(DeploymentServiceProvider.NAME);
        this.deploymentServiceProvider.registerOnLobbyListener(this);
        liveGameContext = new LiveGameContext();
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
        this.deploymentServiceProvider.registerOnConnectionListener(this);
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
            pc.account = account(pc.access.primary()?session.systemId():pc.access.owner());
            pc.subscription = membership(pc.access.primary()?session.systemId():pc.access.owner());
            pc.connection = this.connection;
            session.write(this.builder.create().toJson(pc).getBytes(),this.descriptor.responseLabel());
            this.context.postOffice().onConnection(connection).send(100+"/"+12,"presence".getBytes());
        }
        //public lobby access by page number
        else if(session.action().equals("onLobbyList")){
            OnAccess onAccess = this.builder.create().fromJson(new String(payload).trim(),OnAccess.class);
            int page = ((Number)onAccess.property("page")).intValue();
            LiveGame liveGame = liveGameContext.onIndex(page);
            if(liveGame!=null){
                liveGame.lobbyList = new ArrayList<>();
                liveGame.lobbyList.add(this.context.lobby(liveGame.name+"-lobby"));
                liveGame.lobbyList.add(this.context.lobby(liveGame.name+"-service"));
                liveGame.lobbyList.add(this.context.lobby(liveGame.name+"-data"));
                session.write(liveGame.toJson().toString().getBytes(),this.descriptor.responseLabel());
            }else{
                session.write(toMessage("no lobby data",false).toString().getBytes(),this.descriptor.responseLabel());
            }

        }
        else if(session.action().equals("onAddEmail")){
            OnAccess onAccess = this.builder.create().fromJson(new String(payload).trim(),OnAccess.class);
            User auser = user(session.systemId());
            String email = (String)onAccess.property("emailAddress");
            if(email.contains("@")){
                auser.emailAddress(email);
                userDs.update(auser);
                String code = this.deploymentServiceProvider.resetCode(session.systemId());
                if(this.deploymentServiceProvider.registerPostOffice().onEmail().send(email,code)){
                    session.write(this.builder.create().toJson(new ResponseHeader("","check email for code", true)).getBytes(), descriptor.responseLabel());
                }else {
                    session.write(this.builder.create().toJson(new ResponseHeader("","system issue, try later", false)).getBytes(), descriptor.responseLabel());
                }
            }
            else{
                session.write(this.builder.create().toJson(new ResponseHeader("","wrong email format ["+email+"]",false)).getBytes(),descriptor.responseLabel());
            }
        }
        else if(session.action().equals("onRequestCode")){
            User u = user(session.systemId());
            if(u.activated()){
                session.write(toMessage("Email already has validated",false).toString().getBytes(),descriptor.responseLabel());
            }
            else{
                if(u.emailAddress()!=null&&u.emailAddress.contains("@")){
                    String code = this.deploymentServiceProvider.resetCode(session.systemId());
                    if(this.deploymentServiceProvider.registerPostOffice().onEmail().send(u.emailAddress(),code)){
                        session.write(this.builder.create().toJson(new ResponseHeader("","check email for code", true)).getBytes(), descriptor.responseLabel());
                    }else {
                        session.write(this.builder.create().toJson(new ResponseHeader("","system issue, try later", false)).getBytes(), descriptor.responseLabel());
                    }
                }
                else{
                    session.write(toMessage("No email available",false).toString().getBytes(),descriptor.responseLabel());
                }
            }
        }
        else if(session.action().equals("onValidateEmail")){
            OnAccess onAccess = this.builder.create().fromJson(new String(payload).trim(),OnAccess.class);
            String code = (String) onAccess.property("validationCode");
            if(this.deploymentServiceProvider.checkCode(code).equals(session.systemId())){
                User u = user(session.systemId());
                u.activated(true);
                userDs.update(u);
                session.write(toMessage("validated email",true).toString().getBytes(),descriptor.responseLabel());
            }
            else{
                session.write(toMessage("wrong validation code",true).toString().getBytes(),descriptor.responseLabel());
            }
        }
        else if(session.action().equals("onCheckRole")){
            OnAccess onAccess = this.builder.create().fromJson(new String(payload).trim(),OnAccess.class);
            String role = (String)onAccess.property("role");
            User u = this.user(session.systemId());
            if(tokenValidatorProvider.checkRole(u,role)){
                PresenceContext pc = new PresenceContext(session.action());
                pc.access = u;
                session.write(this.builder.create().toJson(pc).getBytes(),descriptor.responseLabel());
            }
            else{
                session.write(toMessage("invalid role upgrade for ["+role+"] from ["+u.role+"]",false).toString().getBytes(),descriptor.responseLabel());
            }
        }
        else if(session.action().equals("onUpgradeAccountRole")){
            OnAccess onAccess = this.builder.create().fromJson(new String(payload).trim(),OnAccess.class);
            User user = this.user(session.systemId());
            String role = (String)onAccess.property("role");
            boolean suc = this.context.validator().upgradeRole(user,role);
            PermissionContext permissionContext = new PermissionContext(role,suc);
            session.write(permissionContext.toJson().toString().getBytes(),descriptor.responseLabel());
        }
        else if(session.action().equals("onUpgradeAdminRole")){
            OnAccess onAccess = this.builder.create().fromJson(new String(payload).trim(),OnAccess.class);
            User user = this.user(session.systemId());
            String role = (String)onAccess.property("role");
            boolean suc = this.context.validator().upgradeRole(user,role);
            String developerName = (String)onAccess.property("developerName");
            Account acc = this.account(session.systemId());
            acc.owner(developerName);
            accountDs.update(acc);
            PermissionContext permissionContext = new PermissionContext(role,suc);
            session.write(permissionContext.toJson().toString().getBytes(),descriptor.responseLabel());
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
        else{
            session.write(this.builder.create().toJson(new ResponseHeader("onError", "operation not supported", false)).getBytes(),this.descriptor.responseLabel());
        }
    }
    private JsonObject toMessage(String msg, boolean suc){
        JsonObject jms = new JsonObject();
        jms.addProperty("successful",suc);
        jms.addProperty("message",msg);
        return jms;
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
        if(!onLobby.closed()){
            String[] ps = onLobby.typeId().split("-");
            liveGameContext.addGameIndex(ps[0]);
            context.log("Lobby ["+onLobby.typeId()+"] is going to be live",OnLog.WARN);
        }
        else{
            String[] ps = onLobby.typeId().split("-");
            liveGameContext.removeGameIndex(ps[0]);
            context.log("Lobby ["+onLobby.typeId()+"] is going to be offline",OnLog.WARN);
        }
    }
    @Override
    public void onState(Connection c) {
        this.context.log(c.type()+"/"+c.serverId()+"/"+(c.disabled()?"closed":"open")+"/ on lobby ["+descriptor.tag()+"]",OnLog.WARN);
        this.context.log("Server->"+c.server().host(),OnLog.WARN);
        this.connection = c;
        c.registerInboundMessageListener((code,d)->{
            this.context.log("MSG->"+code+"<><><><>"+new String(d).trim(),OnLog.WARN);
        });
    }
}
