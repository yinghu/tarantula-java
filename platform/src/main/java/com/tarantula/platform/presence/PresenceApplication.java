package com.tarantula.platform.presence;

import com.tarantula.*;
import com.tarantula.Response;
import com.tarantula.platform.*;
import com.tarantula.DeploymentServiceProvider;
import com.tarantula.platform.util.*;

import java.util.ArrayList;
import java.util.List;


/**
 * Developer: YINGHU LU
 * Date: updated 6/16/2019.
 */
public class PresenceApplication extends TarantulaApplicationHeader implements OnLobby.Listener,Configuration.Listener{

    private String[] lobbyIdList;
    private DeploymentServiceProvider deploymentServiceProvider;
    private RingBuffer<Configuration> cBuffer;

    @Override
    public void setup(ApplicationContext context) throws Exception {
        super.setup(context);
        Configuration pc = this.context.configuration("setup");
        this.lobbyIdList = pc.property("lobbyList").split(",");
        boolean lobbyListenerEnabled = Boolean.parseBoolean(pc.property("lobbyListenerEnabled"));
        this.cBuffer = new RingBuffer<>(new Configuration[5]);
        builder.registerTypeAdapter(PresenceContext.class, new PresenceContextSerializer());
        deploymentServiceProvider = this.context.serviceProvider(DeploymentServiceProvider.NAME);
        if(lobbyListenerEnabled){
            deploymentServiceProvider.registerOnLobbyListener(this);
        }
        deploymentServiceProvider.registerConfigurationListener(this);
        this.context.registerRecoverableListener(new PresencePortableRegistry()).addRecoverableFilter(PresencePortableRegistry.ON_BALANCE_CID,(t)->{
            this.context.log(t.toString(),OnLog.INFO);
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
                List<Lobby> lobbyList = new ArrayList();
                for (String s : this.lobbyIdList) {
                    Lobby lx = this.context.lobby(s);
                    if (lx != null) {
                        lobbyList.add(lx);
                    } else {
                        this.context.log("Lobby [" + s + "] is not existed. Please check lobby list on presence",OnLog.WARN);
                    }
                }
                pc.lobbyList=(lobbyList);
                pc.connection = cBuffer.pop();
                pc.presence= new OnSessionTrack(session.systemId(),presence.balance());
                session.write(this.builder.create().toJson(pc).getBytes(),this.descriptor.responseLabel());
            }
            else if(session.action().equals("onTicket")){
                //request new ticket
                PresenceContext ptc = new PresenceContext(session.action());
                ptc.presence = new OnSessionTrack(session.systemId(),session.stub());
                ptc.presence.ticket(this.context.validator().ticket(session.systemId(),session.stub(),20));
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
            else if (session.action().equals("onTransfer")) {
                Presence presence = this.context.presence(session.systemId());
                OnAccess ex = this.builder.create().fromJson(new String(payload).trim(), OnAccess.class);
                PresenceContext pb = new PresenceContext(session.action());
                Descriptor desc = this.context.descriptor(ex.applicationId());
                if ((!desc.tournamentEnabled())&&presence.transact(ex.entryCost()* (-1))) {
                    OnBalance onBalance = new OnBalanceTrack(session.systemId(),ex.entryCost());
                    onBalance.applicationId(ex.applicationId());
                    onBalance.instanceId(ex.instanceId());
                    this.context.publish(this.context.instanceRoutingKey(ex.applicationId(),ex.instanceId()),onBalance);
                }else {
                    pb.successful(false);
                    pb.code(desc.tournamentEnabled()?Presence.IN_TOURNAMENT_MODE:Presence.NOT_ENOUGH_BALANCE);
                    pb.message(desc.tournamentEnabled()?"in tournament mode":"not enough balance");
                }
                pb.presence= new OnSessionTrack(session.systemId(),presence.balance());
                session.write(this.builder.create().toJson(pb).getBytes(),this.descriptor.responseLabel());
            }
            else if (session.action().equals("onAbsence")) {
                this.context.absence(session);
                session.write(this.builder.create().toJson(new ResponseHeader("onAbsence", "off session [" + session.stub() + "]", true)).getBytes(),this.descriptor.responseLabel());
                //steam.write(payload,"presence/lobby");
            }
            else{
                session.write(this.builder.create().toJson(new ResponseHeader("onError", "operation not supported", false)).getBytes(),this.descriptor.responseLabel());
            }

    }

    public void onLobby(OnLobby onLobby) {
        //optional with a config flag
        Configuration cx = this.context.configuration("setup");
        String list = cx.property("lobbyList");
        if(list!=null&&!cx.property("lobbyList").contains(onLobby.typeId())) {
            cx.configure("lobbyList", cx.property("lobbyList") + "," +onLobby.typeId());
        }
        else{
            cx.configure("lobbyList",onLobby.typeId());
        }
        this.lobbyIdList = cx.property("lobbyList").split(",");
    }

    @Override
    public void onConfiguration(Configuration c) {
        this.context.log(c.type()+"/"+c.property("serverId")+"/"+c.disabled(),OnLog.INFO);
        if(!c.disabled()){
            if(!cBuffer.push(c)){
                cBuffer.reset(((ca,limit)->{
                    Configuration[] cn = new Configuration[ca.length*2];
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
                Configuration[] cn = new Configuration[ca.length];
                int r=0;
                for(int i=0;i<limit;i++){
                    if(!(ca[i].property("serverId").equals(c.property("serverId")))){
                        cn[r++]=ca[i];
                    }
                }
                return cn;
            });
        }
    }
}
