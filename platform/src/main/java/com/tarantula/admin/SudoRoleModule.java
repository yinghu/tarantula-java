package com.tarantula.admin;

import com.google.gson.*;
import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.icodesoftware.service.*;
import com.icodesoftware.util.JsonUtil;

import com.tarantula.platform.*;
import com.tarantula.platform.presence.PermissionContext;
import com.tarantula.platform.service.persistence.ClusterNode;
import com.tarantula.platform.util.OnAccessDeserializer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

public class SudoRoleModule implements Module {

    private ApplicationContext context;
    private DeploymentServiceProvider deploymentServiceProvider;
    private TokenValidatorProvider tokenValidatorProvider;
    private AccessIndexService accessIndexService;

    private UserService userService;
    private GsonBuilder builder;


    @Override
    public boolean onRequest(Session session, byte[] payload) throws Exception {
        if(session.action().equals("onCheckPermission")){
            Access acc = userService.loadUser(session.distributionId());
            session.write(new PermissionContext(acc.role(),true).toJson().toString().getBytes());
        }

        else if(session.action().equals("onResetClusterKey")){
            boolean suc = this.tokenValidatorProvider.resetClusterKey();
            if(suc){
                this.context.clusterProvider().deployService().onResetClusterKey();
            }
            session.write(JsonUtil.toSimpleResponse(suc,suc?"Cluster reset":"failed to reset key").getBytes());
        }

        else if(session.action().equals("onCreateLabeledKey")){
            String key = tokenValidatorProvider.createAccessKey(session.name());
            PermissionContext pc = new PermissionContext(key);
            session.write(pc.toJson().toString().getBytes());
        }
        else if(session.action().equals("onLabeledKeyList")){
            List<OnAccess> keys = tokenValidatorProvider.accessKeyList();
            session.write(new LabeledAccessKeyContext(keys).toJson().toString().getBytes());
        }
        else if(session.action().equals("onRevokeLabeledKey")){
            //revoke access key
            tokenValidatorProvider.revokeAccessKey(session.name());
            List<OnAccess> keys = tokenValidatorProvider.accessKeyList();
            session.write(new LabeledAccessKeyContext(keys).toJson().toString().getBytes());
        }
       
        else if(session.action().equals("onStopAccessIndex")){
            accessIndexService.onDisable();
            session.write(toMessage(session.action(),true).getBytes());
        }
        else if(session.action().equals("onStartAccessIndex")){
            accessIndexService.onEnable();
            session.write(toMessage(session.action(),true).getBytes());
        }
        else if(session.action().equals("onFindUser")){
            AccessIndex accessIndex = accessIndexService.get(session.name());
            if(accessIndex!=null){
                session.write(accessIndex.toJson().toString().getBytes());
            }else{
                session.write(toMessage("["+session.name()+"] not found",false).getBytes());
            }
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
        else if(session.action().equals("onClusterList")){
            ClusterProvider.Summary summary = this.deploymentServiceProvider.clusterSummary();
            session.write(summary.toJson().toString().getBytes());
        }
        else if(session.action().equals("onClusterShutdown")){
            Access acc = userService.loadUser(session.distributionId());
            session.write(JsonUtil.toSimpleResponse(true,"shutdown : "+session.name()).getBytes());
            DeploymentServiceProvider.NodeShutdownOperator shutdownOperator = deploymentServiceProvider.nodeShutdownOperator(acc);
            ClusterNode node = new ClusterNode();
            node.memberId(session.name());
            shutdownOperator.shutdown(node);
        }
        else if(session.action().equals("onClusterRestart")){
            Access acc = userService.loadUser(session.distributionId());
            session.write(JsonUtil.toSimpleResponse(true,"restart : "+session.name()).getBytes());
            DeploymentServiceProvider.NodeShutdownOperator shutdownOperator = deploymentServiceProvider.nodeShutdownOperator(acc);
            ClusterNode node = new ClusterNode();
            node.memberId(session.name());
            shutdownOperator.restart(node);
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
        this.context.log("Sudo setup module started", OnLog.INFO);
    }

    private String toMessage(String msg,boolean suc){
       return JsonUtil.toSimpleResponse(suc,msg);
    }


}
