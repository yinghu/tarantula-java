package com.tarantula.game.casino;

import com.tarantula.*;
import com.tarantula.game.GameApplication;
import com.tarantula.game.casino.sicbo.SicBo;
import com.tarantula.game.casino.sicbo.SicBoSerializer;
import com.tarantula.platform.DeploymentDescriptor;
import com.tarantula.platform.TarantulaContext;
import com.tarantula.platform.util.SystemUtil;

public class SicBoApplication extends GameApplication {

    private SicBo sicBo;
    TarantulaContext tcx;
    @Override
    public void initialize(Session session) throws Exception {
        session.joined(true);
        this.sicBo.join(session.systemId());
        session.write(this.builder.create().toJson(sicBo.setup()).getBytes(),this.descriptor.responseLabel());
    }
    @Override
    public void callback(Session session, byte[] payload) throws Exception {
        if(session.action().equals("onLeave")){
            this.onTimeout(session);
            session.write(payload,"sicbo");
        }
        else if(session.action().equals("onStream")){
            this.onStream(session);
        }
        else if(session.action().equals("onQuery")){
            //this.sicBo.reset();
            session.write(payload,"sicbo");
        }
        else if(session.action().equals("onBackup")){
            this.tcx.dataStoreProvider().backup(Distributable.DATA_SCOPE);
            this.tcx.dataStoreProvider().backup(Distributable.INTEGRATION_SCOPE);
            session.write(payload,"sicbo");
        }
        else if(session.action().equals("addLobby")){
            //this.context.log(new String(payload),OnLog.WARN);
            DeploymentServiceProvider dps = this.context.serviceProvider(DeploymentServiceProvider.NAME);
            DeploymentDescriptor desc = new DeploymentDescriptor();
            desc.fromMap(SystemUtil.toMap(payload));
            session.write(dps.createLobby(desc).getBytes(),"sicbo");
        }
        else if(session.action().equals("addApplication")){
            DeploymentServiceProvider dps = this.context.serviceProvider(DeploymentServiceProvider.NAME);
            DeploymentDescriptor desc = new DeploymentDescriptor();
            desc.fromMap(SystemUtil.toMap(payload));
            session.write(dps.createApplication(desc).getBytes(),"sicbo");
        }
        else if(session.action().equals("onLaunch")){
            DeploymentServiceProvider dps = this.context.serviceProvider(DeploymentServiceProvider.NAME);
            dps.launch("demo-sync");
            session.write(payload,"sicbo");
        }
        else if(session.action().equals("enableApplication")){
            DeploymentServiceProvider dps = this.context.serviceProvider(DeploymentServiceProvider.NAME);
            OnAccess acc = this.builder.create().fromJson(new String(payload),OnAccess.class);
            session.write(dps.enableApplication(acc.accessId(),true).getBytes(),"sicbo");
        }
        else if(session.action().equals("disableApplication")){
            DeploymentServiceProvider dps = this.context.serviceProvider(DeploymentServiceProvider.NAME);
            OnAccess acc = this.builder.create().fromJson(new String(payload),OnAccess.class);
            session.write(dps.enableApplication(acc.accessId(),false).getBytes(),"sicbo");
        }
        else if(session.action().equals("onShutdown")){
            DeploymentServiceProvider dps = this.context.serviceProvider(DeploymentServiceProvider.NAME);
            dps.shutdown("demo-sync");
            session.write(payload,"sicbo");
        }
        else if(session.action().equals("onReset")){
            DeploymentServiceProvider dps = this.context.serviceProvider(DeploymentServiceProvider.NAME);
            DeploymentDescriptor desc = new DeploymentDescriptor();
            desc.typeId("demo-sync");
            desc.subtypeId("demo-sync-a");
            desc.codebase("file:///development/boost/target");
            desc.moduleArtifact("tarantula-boost");
            desc.moduleVersion("1.1");
            //desc.moduleName("com.tarantula.boost.Demo");
            dps.reset(desc);
            session.write(payload,"sicbo");
        }
        else if(session.action().equals("onPing")){
            session.write(payload,"sicbo");
        }
    }
    @Override
    public void setup(ApplicationContext context) throws Exception {
        super.setup(context);
        this.builder.registerTypeAdapter(SicBo.class,new SicBoSerializer());
        this.sicBo = new SicBo();
        //this.sicBo.pendingQueue = this.uQueue;
        //this.sicBo.instanceId(this.context.onRegistry().distributionKey());
        //this.sicBo.name(this.descriptor.name());
        //this.sicBo.entryCost(this.descriptor.entryCost());
        //this.sicBo.context = this.context;
        //this.sicBo.currentCheckPoint = this.sicBo;
        //this.sicBo.tQueue.offer(this.sicBo);
        //this.context.schedule(sicBo);
        this.context.schedule(this);
        this.context.log("SicBo application started ["+descriptor.name()+"]", OnLog.INFO);
        tcx = TarantulaContext.getInstance();
    }

}
