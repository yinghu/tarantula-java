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

public class PresenceApplication extends TarantulaApplicationHeader implements Configurable.Listener<OnLobby>{

    private DeploymentServiceProvider deploymentServiceProvider;
    private TokenValidatorProvider tokenValidatorProvider;

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
        this.context.log("Presence application started on ["+descriptor.tag()+"]",OnLog.INFO);
    }

    @Override
    public void callback(Session session, byte[] payload) throws Exception {
        if (session.action().equals("onSession")) {
            Presence presence = this.context.presence(session);
            PresenceContext pc = new PresenceContext(session.action());
            pc.presence = new OnSessionTrack(session.systemId(),presence.balance());
            pc.presence.stub(presence.count(0));
            session.write(this.builder.create().toJson(pc).getBytes());
        }
        else if (session.action().equals("onPresence")) {
            Presence presence = this.context.presence(session);
            PresenceContext pc = new PresenceContext(session.action());
            pc.presence= new OnSessionTrack(session.systemId(),presence.balance());
            pc.presence.stub(presence.count(0));
            pc.access = user(session.systemId());
            pc.account = account(pc.access);
            pc.subscription = membership(pc.access.primary()?session.systemId():pc.access.owner());
            pc.stripeClientId = this.tokenValidatorProvider.authVendor(OnAccess.STRIPE).clientId();
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
                if(this.deploymentServiceProvider.registerPostOffice().onEmail(email).send(code)){
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
            Access u = user(session.systemId());
            if(u.activated()){
                session.write(JsonUtil.toSimpleResponse(false,"Email already has validated").getBytes());
            }
            else{
                if(u.emailAddress()!=null&&u.emailAddress().contains("@")){
                    String code = this.deploymentServiceProvider.resetCode(session.systemId());
                    if(this.deploymentServiceProvider.registerPostOffice().onEmail(u.emailAddress()).send(code)){
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
                Access u = user(session.systemId());
                u.activated(true);
                u.update();
                session.write(JsonUtil.toSimpleResponse(true,"validated email").getBytes());
            }
            else{
                session.write(JsonUtil.toSimpleResponse(false,"wrong validation code").getBytes());
            }
        }
        else if(session.action().equals("onCheckRole")){
            OnAccess onAccess = this.builder.create().fromJson(new String(payload).trim(),OnAccess.class);
            String role = (String)onAccess.property("role");
            Access u = this.user(session.systemId());
            if(tokenValidatorProvider.checkRole(u,role)){
                PresenceContext pc = new PresenceContext(session.action());
                pc.access = u;
                session.write(this.builder.create().toJson(pc).getBytes());
            }
            else{
                session.write(JsonUtil.toSimpleResponse(false,"invalid role upgrade for ["+role+"] from ["+u.role()+"]").getBytes());
            }
        }
        else if(session.action().equals("onUpgradeAccountRole")){
            OnAccess onAccess = this.builder.create().fromJson(new String(payload).trim(),OnAccess.class);
            Access user = this.user(session.systemId());
            String role = (String)onAccess.property("role");
            boolean suc = this.context.validator().upgradeRole(user,role);
            PermissionContext permissionContext = new PermissionContext(role,suc);
            session.write(permissionContext.toJson().toString().getBytes());
        }
        else if(session.action().equals("onUpgradeAdminRole")){
            OnAccess onAccess = this.builder.create().fromJson(new String(payload).trim(),OnAccess.class);
            Access user = this.user(session.systemId());
            String role = (String)onAccess.property("role");
            boolean suc = this.context.validator().upgradeRole(user,role);
            String developerName = (String)onAccess.property("developerName");
            Account acc = this.account(user);
            acc.owner(developerName);
            acc.update();
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
    private Access user(String systemId){
        return this.userService.loadUser(systemId);
    }
    private Account account(Access access){
        if(access == null) return null;
        return this.userService.loadAccount(access);
    }
    private Subscription membership(String systemId){
        Access access = userService.loadUser(systemId);
        return userService.loadSubscription(access);
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
