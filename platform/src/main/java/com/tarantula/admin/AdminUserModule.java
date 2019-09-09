package com.tarantula.admin;

import com.google.gson.GsonBuilder;
import com.tarantula.*;
import com.tarantula.Module;
import com.tarantula.platform.presence.AccessTrack;
import com.tarantula.platform.service.AccessIndexService;
import com.tarantula.platform.util.OnAccessDeserializer;
import com.tarantula.platform.util.SystemUtil;

public class AdminUserModule implements Module {

    private ApplicationContext context;
    private GsonBuilder builder;
    private AccessIndexService accessIndexService;
    private DataStore user;
    public void onJoin(Session session,OnConnection onConnection) throws Exception{
        session.write(this.builder.create().toJson(_onMessage("joined")).getBytes(),label());
    }

    @Override
    public boolean onRequest(Session session, byte[] payload, OnUpdate update) throws Exception {
        this.context.log(session.action()+"=>"+new String(payload),OnLog.INFO);
        OnAccess onAccess = this.builder.create().fromJson(new String(payload),OnAccess.class);
        if(session.action().equals("findKey")){
            AccessIndex ix = this.accessIndexService.get(onAccess.property("login"));
            if(ix!=null){
                session.write(this.builder.create().toJson(_onAccessIndex(ix)).getBytes(),label());
            }
            else{
                session.write(payload,label());
            }
        }
        else if(session.action().equals("resetPassword")){
            Access acc = new AccessTrack();
            acc.distributionKey(onAccess.systemId());
            String p1 = onAccess.property("password1");
            String p2 = onAccess.property("password2");
            if(p1.equals(p2)&&this.user.load(acc)){
                acc.password(this.context.validator().hashPassword(p1));
                this.user.update(acc);
                session.write(this.builder.create().toJson(_onMessage("password changed")).getBytes(),label());
            }
            else{
                session.write(this.builder.create().toJson(_onMessage("no user found or wrong data input")).getBytes(),label());
            }
        }
        else if(session.action().equals("changeRole")){
            Access acc = new AccessTrack();
            acc.distributionKey(onAccess.systemId());
            String r1 = onAccess.property("role1");
            String r2 = onAccess.property("role2");
            if(r1.equals(r2)&&this.user.load(acc)){
                acc.role(r1);
                this.user.update(acc);
                session.write(this.builder.create().toJson(_onMessage("role changed")).getBytes(),label());
            }
            else{
                session.write(this.builder.create().toJson(_onMessage("no user found or wrong data input")).getBytes(),label());
            }
        }
        else if(session.action().equals("addUser")){
            AccessIndex query = accessIndexService.set(onAccess.property("login"), user.bucket()+Recoverable.PATH_SEPARATOR+SystemUtil.oid());
            if(query!=null){
                onAccess.owner(session.systemId());
                onAccess.distributionKey(query.distributionKey());
                this.context.postOffice().onTag("index/user").send(onAccess.distributionKey(),onAccess);
                session.write(this.builder.create().toJson(_onMessage("user added")).getBytes(),label());
            }
            else{
                session.write(this.builder.create().toJson(_onMessage("user already existed")).getBytes(),label());
            }
        }
        else{
            session.write(payload,label());
        }
        return session.action().equals("onLeave");
    }

    @Override
    public void setup(ApplicationContext context) throws Exception {
        this.context = context;
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(OnAccess.class,new OnAccessDeserializer());
        this.builder.registerTypeAdapter(AdminUserObject.class,new AdminObjectSerializer());
        this.accessIndexService = this.context.serviceProvider(AccessIndexService.NAME);
        this.user = this.context.dataStore("user");
        this.context.log("Admin user module started", OnLog.INFO);
    }
    @Override
    public String label() {
        return "admin-user";
    }
    private AdminUserObject _onMessage(String message){
        return new AdminUserObject(message,label());
    }
    private AdminUserObject _onAccessIndex(AccessIndex accessIndex){
        return new AdminUserObject(accessIndex,label());
    }
}
