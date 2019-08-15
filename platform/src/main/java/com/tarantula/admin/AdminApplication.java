package com.tarantula.admin;

import com.tarantula.*;
import com.tarantula.platform.DeploymentDescriptor;
import com.tarantula.platform.TarantulaApplicationHeader;
import com.tarantula.platform.TarantulaContext;
import com.tarantula.platform.util.SystemUtil;

public class AdminApplication extends TarantulaApplicationHeader {

    private DBObject dbObject;
    TarantulaContext tcx;
    @Override
    public void initialize(Session session) throws Exception {
        //session.joined(true);
        //this.dbObject.join(session.systemId());
        //session.write(this.builder.create().toJson(dbObject.setup()).getBytes(),this.descriptor.responseLabel());
    }
    @Override
    public void callback(Session session, byte[] payload) throws Exception {
        if(session.action().equals("onLeave")){
            //this.onTimeout(session);
            session.write(payload,this.descriptor.responseLabel());
        }
        else if(session.action().equals("onStream")){
            //this.onStream(session);
        }
        else if(session.action().equals("onQuery")){
            //this.dbObject.reset();
            //session.write(this.builder.create().toJson(this.dbObject.setup()).getBytes(),this.descriptor.responseLabel());
        }
        else if(session.action().equals("onBackup")){
            this.tcx.dataStoreProvider().backup(Distributable.DATA_SCOPE);
            this.tcx.dataStoreProvider().backup(Distributable.INTEGRATION_SCOPE);
            session.write(payload,this.descriptor.responseLabel());
        }
        else if(session.action().equals("addLobby")){
            this.context.log(new String(payload),OnLog.WARN);
            DeploymentServiceProvider dps = this.context.serviceProvider(DeploymentServiceProvider.NAME);
            DeploymentDescriptor desc = new DeploymentDescriptor();
            desc.fromMap(SystemUtil.toMap(payload));
            desc.type("lobby");
            desc.category("game");
            desc.accessMode(Session.PROTECT_ACCESS_MODE);
            desc.deployCode(1);
            session.write(dps.createLobby(desc).getBytes(),this.descriptor.responseLabel());
        }
        else if(session.action().equals("addApplication")){
            this.context.log(new String(payload),OnLog.WARN);
            DeploymentServiceProvider dps = this.context.serviceProvider(DeploymentServiceProvider.NAME);
            DeploymentDescriptor desc = new DeploymentDescriptor();
            desc.fromMap(SystemUtil.toMap(payload));
            desc.type("application");
            desc.category("demo");
            desc.deployPriority(10);
            desc.maxIdlesOnInstance(3);
            desc.maxInstancesPerPartition(100);
            desc.instancesOnStartupPerPartition(1);
            session.write(dps.createApplication(desc).getBytes(),this.descriptor.responseLabel());
        }
        else if(session.action().equals("onLaunch")){
            DeploymentServiceProvider dps = this.context.serviceProvider(DeploymentServiceProvider.NAME);
            dps.launch("demo-sync");
            session.write(payload,this.descriptor.responseLabel());
        }
        else if(session.action().equals("enableApplication")){
            DeploymentServiceProvider dps = this.context.serviceProvider(DeploymentServiceProvider.NAME);
            OnAccess acc = this.builder.create().fromJson(new String(payload),OnAccess.class);
            session.write(dps.enableApplication(acc.accessId(),true).getBytes(),this.descriptor.responseLabel());
        }
        else if(session.action().equals("disableApplication")){
            DeploymentServiceProvider dps = this.context.serviceProvider(DeploymentServiceProvider.NAME);
            OnAccess acc = this.builder.create().fromJson(new String(payload),OnAccess.class);
            session.write(dps.enableApplication(acc.accessId(),false).getBytes(),this.descriptor.responseLabel());
        }
        else if(session.action().equals("onShutdown")){
            DeploymentServiceProvider dps = this.context.serviceProvider(DeploymentServiceProvider.NAME);
            dps.shutdown("demo-sync");
            session.write(payload,this.descriptor.responseLabel());
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
            session.write(payload,this.descriptor.responseLabel());
        }
        else if(session.action().equals("onPing")){
            session.write(payload,this.descriptor.responseLabel());
        }
    }
    @Override
    public void setup(ApplicationContext context) throws Exception {
        super.setup(context);
        this.builder.registerTypeAdapter(DBObject.class,new DBObjectSerializer());
        this.dbObject = new DBObject();
        this.dbObject.successful(true);
        //this.dbObject.instanceId(this.context.onRegistry().distributionKey());
        this.dbObject.name(this.descriptor.name());
        this.dbObject.entryCost(this.descriptor.entryCost());
        this.dbObject.context = this.context;
        this.context.log("DBObject application started ["+descriptor.name()+"]", OnLog.INFO);
        tcx = TarantulaContext.getInstance();
    }

    @Override
    public boolean onEvent(Event event) {
        this.context.log("join event",OnLog.INFO);
        event.write(this.builder.create().toJson(dbObject).getBytes(),this.descriptor.label());
        return false;
    }
}
