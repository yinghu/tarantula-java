package com.tarantula.platform.presence;

import com.tarantula.*;
import com.tarantula.Response;
import com.tarantula.platform.*;

import com.tarantula.platform.service.DeploymentServiceProvider;
import com.tarantula.platform.util.*;
/**
 * Developer: YINGHU LU
 * Date: updated 12/25/2019.
 */
public class PresenceApplication extends TarantulaApplicationHeader {

    private RingBuffer<Connection> cBuffer;
    private DeploymentServiceProvider deploymentServiceProvider;
    private DataStore userDs;
    @Override
    public void setup(ApplicationContext context) throws Exception {
        super.setup(context);
        builder.registerTypeAdapter(PresenceContext.class, new PresenceContextSerializer());
        this.cBuffer = new RingBuffer<>(new Connection[5]);
        deploymentServiceProvider = this.context.serviceProvider(DeploymentServiceProvider.NAME);
        userDs = this.context.dataStore("user");
        //deploymentServiceProvider.registerOnConnectionListener(this);

        this.context.registerRecoverableListener(new PresencePortableRegistry()).addRecoverableFilter(PresencePortableRegistry.ON_BALANCE_CID,(t)->{
            Presence presence = this.context.presence(t.owner());
            OnBalance ob = (OnBalance)t;
            if(!(presence!=null&&presence.transact(ob.balance()))){
                ob.redeemed(false);
                //this.context.dataStore("presence").create(ob);
            }
        });
        this.context.log("Presence application started on ["+descriptor.tag()+"]",OnLog.INFO);
    }

    @Override
    public void callback(Session session, byte[] payload) throws Exception {
        if (session.action().equals("onPresence")) {
            Presence presence = this.context.presence(session.systemId());
            PresenceContext pc = new PresenceContext(session.action());
            pc.presence= new OnSessionTrack(session.systemId(),presence.balance());
            User auser = new User();
            auser.distributionKey(session.systemId());
            if(userDs.load(auser)){
                pc.access = auser;
            }
            session.write(this.builder.create().toJson(pc).getBytes(),this.descriptor.responseLabel());
        }
        else if(session.action().equals("onConnection")){//get web socket connection with a join ticket
            //request new ticket and connection
            PresenceContext ptc = new PresenceContext(session.action());
            ptc.presence = new OnSessionTrack(session.systemId(),session.stub());
            ptc.presence.ticket(this.context.validator().ticket(session.systemId(),session.stub()));
            ptc.connection = cBuffer.pop();
            session.write(this.builder.create().toJson(ptc).getBytes(),this.descriptor.responseLabel());
        }
        else if(session.action().equals("onPlay")){
              OnAccess onAccess = this.builder.create().fromJson(new String(payload).trim(),OnAccess.class);
              Presence presence = this.context.presence(session.systemId());
              Response resp = presence.onPlay(session,onAccess,this.context.descriptor(onAccess.applicationId()));
              if (resp != null) {
                  session.write(this.builder.create().toJson(resp).getBytes(),this.descriptor.responseLabel());//failure
              }
        }
        else if (session.action().equals("onBalance")) {
            Presence presence = this.context.presence(session.systemId());
            PresenceContext pc = new PresenceContext(session.action());
            pc.presence= new OnSessionTrack(session.systemId(),presence.balance());
            session.write(this.builder.create().toJson(pc).getBytes(),this.descriptor.responseLabel());
        }
        else if (session.action().equals("onAbsence")) {
            this.context.absence(session);
            session.write(this.builder.create().toJson(new ResponseHeader("onAbsence", "off session [" + session.stub() + "]", true)).getBytes(),this.descriptor.responseLabel());
        }
        else{
            session.write(this.builder.create().toJson(new ResponseHeader("onError", "operation not supported", false)).getBytes(),this.descriptor.responseLabel());
        }

    }
    @Override
    public void onState(Connection c) {
        if(c.type().equals(Connection.WEB_SOCKET)){
            this.context.log(c.type()+"/"+c.serverId()+"/"+(c.disabled()?"closed":"open")+"/ on presence service application",OnLog.WARN);
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
