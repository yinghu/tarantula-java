package com.tarantula.admin;

import com.google.gson.GsonBuilder;

import com.icodesoftware.Module;
import com.icodesoftware.*;
import com.icodesoftware.service.*;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.DeploymentDescriptor;
import com.tarantula.platform.OnViewTrack;
import com.tarantula.platform.presence.PermissionContext;
import com.tarantula.platform.util.OnAccessDeserializer;


public class ModuleSudoRoleModule implements Module {

    private ApplicationContext context;
    private DeploymentServiceProvider deploymentServiceProvider;
    private TokenValidatorProvider tokenValidatorProvider;
    private AccessIndexService accessIndexService;

    private UserService userService;
    private GsonBuilder builder;


    @Override
    public boolean onRequest(Session session, byte[] payload) throws Exception {
        if(session.action().equals("onCheckPermission")){
            Access acc = userService.loadUser(session.id());
            session.write(new PermissionContext(acc.role(),true).toJson().toString().getBytes());
        }
        else if(session.action().equals("onExportModule")){
            OnAccess acc = this.builder.create().fromJson(new String(payload),OnAccess.class);
            DeploymentDescriptor desc = new DeploymentDescriptor();
            desc.codebase(acc.property(OnAccess.MODULE_CODE_BASE).toString());
            desc.moduleArtifact(acc.property(OnAccess.MODULE_ARTIFACT).toString());
            desc.moduleVersion(acc.property(OnAccess.MODULE_VERSION).toString());
            Response resp = this.deploymentServiceProvider.exportModule(desc);
            session.write(this.toMessage(resp.message(),resp.successful()).getBytes());
        }
        else if(session.action().equals("onAddModule")){
            OnAccess acc = this.builder.create().fromJson(new String(payload),OnAccess.class);
            DeploymentDescriptor desc = new DeploymentDescriptor();
            desc.codebase(acc.property(OnAccess.MODULE_CODE_BASE).toString());
            desc.moduleArtifact(acc.property(OnAccess.MODULE_ARTIFACT).toString());
            desc.moduleVersion(acc.property(OnAccess.MODULE_VERSION).toString());
            Response resp = this.deploymentServiceProvider.createModule(desc);
            session.write(this.toMessage(resp.message(),resp.successful()).getBytes());
        }
        else if(session.action().equals("onLaunchModule")){//typeId
            OnAccess access = this.builder.create().fromJson(new String(payload),OnAccess.class);
            boolean suc = this.deploymentServiceProvider.launchModule(access.typeId());
            session.write(this.toMessage(suc?"module launched":"module not launched",suc).getBytes());
        }
        else if(session.action().equals("onResetModule")){//typeId or moduleId
            OnAccess access = this.builder.create().fromJson(new String(payload),OnAccess.class);
            Descriptor desc = new DeploymentDescriptor();
            desc.typeId(access.typeId());
            desc.moduleArtifact((String) access.property(OnAccess.MODULE_ARTIFACT));
            desc.moduleVersion((String)access.property(OnAccess.MODULE_VERSION));
            desc.codebase((String)access.property(OnAccess.MODULE_CODE_BASE));
            AccessIndex index = this.accessIndexService.get(access.typeId());
            if(index!=null){
                desc.index(index.distributionKey());
            }
            boolean suc  = this.deploymentServiceProvider.resetModule(desc);
            session.write(this.toMessage(suc?"module rest":"module not reset",suc).getBytes());
        }
        else if(session.action().equals("onShutdownModule")){//typeId
            OnAccess access = this.builder.create().fromJson(new String(payload),OnAccess.class);
            boolean suc = this.deploymentServiceProvider.shutdownModule(access.typeId());
            session.write(this.toMessage(suc?"module shutdown":"module not shutdown",suc).getBytes());
        }
        else if(session.action().equals("onDeployView")){
            OnAccess onAccess = this.builder.create().fromJson(new String(payload),OnAccess.class);
            OnView onView = new OnViewTrack();
            onView.owner(onAccess.typeId());//associated with a lobby type Id
            onView.viewId((String)onAccess.property("viewId"));
            String moduleContext = onAccess.property("deployUrl")!=null?(String) onAccess.property("deployUrl"):"root";
            if(moduleContext.startsWith("root")){
                int tix = moduleContext.lastIndexOf('/');
                if(tix<0){
                    onView.moduleResourceFile((String) onAccess.property("resourceName"));
                }
                else{
                    onView.moduleResourceFile(moduleContext.substring(tix+1)+"/"+onAccess.property("resourceName"));
                }
            }else{
                String rname = onAccess.property("deployUrl")+"/"+onAccess.property("resourceName");
                onView.moduleResourceFile(rname);
            }
            onView.moduleContext(moduleContext);
            Response suc = this.deploymentServiceProvider.createView(onView);
            session.write(toMessage(suc.message(),suc.successful()).getBytes());
        }
        else if(session.action().equals("onDeployResource")){
            OnAccess onAccess = this.builder.create().fromJson(new String(payload),OnAccess.class);
            Response suc = this.deploymentServiceProvider.deployResource((String)onAccess.property("deployUrl"),(String)onAccess.property("resourceName"));
            session.write(toMessage(suc.message(),suc.successful()).getBytes());
        }
        else if(session.action().equals("onDeployModule")){
            OnAccess onAccess = this.builder.create().fromJson(new String(payload),OnAccess.class);
            Response suc = this.deploymentServiceProvider.deployModule((String)onAccess.property("deployUrl"),(String)onAccess.property("resourceName"));
            session.write(toMessage(suc.message(),suc.successful()).getBytes());
        }
        else{
           throw new UnsupportedOperationException("operation ["+session.action()+"] not supported");
        }
        return false;
    }

    @Override
    public void setup(ApplicationContext context) throws Exception {
        this.context = context;
        this.deploymentServiceProvider = this.context.serviceProvider(DeploymentServiceProvider.NAME);
        this.tokenValidatorProvider = this.context.serviceProvider(TokenValidatorProvider.NAME);
        this.accessIndexService = this.context.serviceProvider(AccessIndexService.NAME);
        this.userService = this.context.serviceProvider(UserService.NAME);
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(OnAccess.class,new OnAccessDeserializer());
        this.context.log("Sudo module setup module started", OnLog.INFO);
    }

    private String toMessage(String msg,boolean suc){
        return JsonUtil.toSimpleResponse(suc,msg);
    }
}
