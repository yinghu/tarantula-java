package com.tarantula.platform.presence;

import com.tarantula.*;
import com.tarantula.Response;
import com.tarantula.platform.*;

import com.tarantula.platform.util.*;
/**
 * Developer: YINGHU LU
 * Date: updated 9/8/2019.
 */
public class PresenceApplication extends TarantulaApplicationHeader {


    @Override
    public void setup(ApplicationContext context) throws Exception {
        super.setup(context);

        builder.registerTypeAdapter(PresenceContext.class, new PresenceContextSerializer());
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
                session.write(this.builder.create().toJson(pc).getBytes(),this.descriptor.responseLabel());
         }
        else if(session.action().equals("onTicket")){
            //request new ticket
            PresenceContext ptc = new PresenceContext(session.action());
            ptc.presence = new OnSessionTrack(session.systemId(),session.stub());
            ptc.presence.ticket(this.context.validator().ticket(session.systemId(),session.stub()));
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
                this.context.postOffice().onApplication(ex.applicationId()).send(ex.instanceId(),onBalance);
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
        }
        else{
            session.write(this.builder.create().toJson(new ResponseHeader("onError", "operation not supported", false)).getBytes(),this.descriptor.responseLabel());
        }

    }
}
