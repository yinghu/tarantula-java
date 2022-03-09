package com.tarantula.platform.presence;

import com.icodesoftware.*;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.service.OnLobby;
import com.icodesoftware.service.TokenValidatorProvider;
import com.icodesoftware.service.UserService;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.*;
import com.tarantula.platform.util.*;

import java.util.ArrayList;
import java.util.Base64;

public class PresenceApplication extends TarantulaApplicationHeader implements Configurable.Listener<OnLobby>{

    private DeploymentServiceProvider deploymentServiceProvider;
    private TokenValidatorProvider tokenValidatorProvider;
    private DataStore userDs;
    private DataStore accountDs;
    private DataStore memberDs;
    private LiveGameContext liveGameContext;
    private UserService userService;

    @Override
    public void setup(ApplicationContext context) throws Exception {
        super.setup(context);
        builder.registerTypeAdapter(PresenceContext.class, new PresenceContextSerializer());
        this.tokenValidatorProvider = this.context.serviceProvider(TokenValidatorProvider.NAME);
        this.deploymentServiceProvider = this.context.serviceProvider(DeploymentServiceProvider.NAME);
        this.deploymentServiceProvider.registerConfigurableListener(OnLobby.TYPE,this);
        this.userService = this.context.serviceProvider(UserService.NAME);
        liveGameContext = new LiveGameContext();
        userDs = this.context.dataStore(Access.DataStore);
        accountDs = this.context.dataStore(Account.DataStore);
        memberDs = this.context.dataStore(Subscription.DataStore);
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
            session.write(this.builder.create().toJson(pc).getBytes());
        }
        //public lobby access by page number
        else if(session.action().equals("onLobbyList")){
            OnAccess onAccess = this.builder.create().fromJson(new String(payload).trim(),OnAccess.class);
            int page = Integer.parseInt(onAccess.property("page").toString());
            LiveGame liveGame = liveGameContext.onIndex(page);
            if(liveGame!=null){
                liveGame.lobbyList = new ArrayList<>();
                liveGame.lobbyList.add(this.context.lobby(liveGame.name+"-lobby"));
                liveGame.lobbyList.add(this.context.lobby(liveGame.name+"-service"));
                liveGame.lobbyList.add(this.context.lobby(liveGame.name+"-data"));
                session.write(liveGame.toJson().toString().getBytes());
            }else{
                session.write(JsonUtil.toSimpleResponse(false,"no lobby data").getBytes());
            }
        }
        else if(session.action().equals("onLobby")){
            if(liveGameContext.onIndex(session.name())){
                LiveGame liveGame = new LiveGame(0,session.name());
                liveGame.lobbyList = new ArrayList<>();
                liveGame.lobbyList.add(this.context.lobby(liveGame.name+"-lobby"));
                liveGame.lobbyList.add(this.context.lobby(liveGame.name+"-service"));
                liveGame.lobbyList.add(this.context.lobby(liveGame.name+"-data"));
                session.write(liveGame.toJson().toString().getBytes());
            }
            else{
                session.write(JsonUtil.toSimpleResponse(false,session.name()+" not available").getBytes());
            }
        }
        else if(session.action().equals("onAddEmail")){
            OnAccess onAccess = this.builder.create().fromJson(new String(payload).trim(),OnAccess.class);
            onAccess.property(OnAccess.SYSTEM_ID,session.systemId());
            String email = (String) onAccess.property(OnAccess.EMAIL_ADDRESS);
            if(userService.updateEmail(onAccess)){
                String code = this.deploymentServiceProvider.resetCode(session.systemId());
                if(this.deploymentServiceProvider.registerPostOffice().onEmail().send(email,code)){
                    session.write(this.builder.create().toJson(new ResponseHeader("","check email for code", true)).getBytes());
                }else {
                    session.write(this.builder.create().toJson(new ResponseHeader("","system issue, try later", false)).getBytes());
                }
            }
            else{
                session.write(this.builder.create().toJson(new ResponseHeader("","wrong email format ["+email+"]",false)).getBytes());
            }
        }
        else if(session.action().equals("onRequestCode")){
            User u = user(session.systemId());
            if(u.activated()){
                session.write(JsonUtil.toSimpleResponse(false,"Email already has validated").getBytes());
            }
            else{
                if(u.emailAddress()!=null&&u.emailAddress.contains("@")){
                    String code = this.deploymentServiceProvider.resetCode(session.systemId());
                    if(this.deploymentServiceProvider.registerPostOffice().onEmail().send(u.emailAddress(),code)){
                        session.write(this.builder.create().toJson(new ResponseHeader("","check email for code", true)).getBytes());
                    }else {
                        session.write(this.builder.create().toJson(new ResponseHeader("","system issue, try later", false)).getBytes());
                    }
                }
                else{
                    session.write(JsonUtil.toSimpleResponse(false,"No email available").getBytes());
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
                session.write(JsonUtil.toSimpleResponse(true,"validated email").getBytes());
            }
            else{
                session.write(JsonUtil.toSimpleResponse(false,"wrong validation code").getBytes());
            }
        }
        else if(session.action().equals("onCheckRole")){
            OnAccess onAccess = this.builder.create().fromJson(new String(payload).trim(),OnAccess.class);
            String role = (String)onAccess.property("role");
            User u = this.user(session.systemId());
            if(tokenValidatorProvider.checkRole(u,role)){
                PresenceContext pc = new PresenceContext(session.action());
                pc.access = u;
                session.write(this.builder.create().toJson(pc).getBytes());
            }
            else{
                session.write(JsonUtil.toSimpleResponse(false,"invalid role upgrade for ["+role+"] from ["+u.role+"]").getBytes());
            }
        }
        else if(session.action().equals("onUpgradeAccountRole")){
            OnAccess onAccess = this.builder.create().fromJson(new String(payload).trim(),OnAccess.class);
            User user = this.user(session.systemId());
            String role = (String)onAccess.property("role");
            boolean suc = this.context.validator().upgradeRole(user,role);
            PermissionContext permissionContext = new PermissionContext(role,suc);
            session.write(permissionContext.toJson().toString().getBytes());
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
            session.write(permissionContext.toJson().toString().getBytes());
        }
        else if(session.action().equals("onChangePassword")){
            OnAccess onAccess = this.builder.create().fromJson(new String(payload).trim(),OnAccess.class);
            onAccess.property(OnAccess.SYSTEM_ID,session.systemId());
            boolean suc = this.userService.changePassword(onAccess);
            ResponseHeader responseHeader = new ResponseHeader(session.action(),suc?"You have changed password":"Failed to change password",suc);
            session.write(this.builder.create().toJson(responseHeader).getBytes());
        }
        else if (session.action().equals("onAbsence")) {
            this.context.absence(session);
            session.write(this.builder.create().toJson(new ResponseHeader("onAbsence", "off session [" + session.stub() + "]", true)).getBytes());
        }
        else{
            session.write(this.builder.create().toJson(new ResponseHeader("onError", "operation not supported", false)).getBytes());
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
    public void onUpdated(OnLobby onLobby) {
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
    public boolean onEvent(Event event){
        this.context.log("remote event",OnLog.WARN);
        return true;
    }
}
