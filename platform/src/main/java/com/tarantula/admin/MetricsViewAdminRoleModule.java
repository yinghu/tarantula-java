package com.tarantula.admin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.Module;
import com.icodesoftware.*;
import com.icodesoftware.service.*;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.presence.PermissionContext;
import com.tarantula.platform.service.metrics.*;

import java.util.concurrent.ConcurrentHashMap;


public class MetricsViewAdminRoleModule implements Module {

    private ApplicationContext context;
    private DeploymentServiceProvider deploymentServiceProvider;

    private UserService userService;

    private ConcurrentHashMap<String, ServiceViewSummary> viewMap = new ConcurrentHashMap<>();
    private Configuration chartConfiguration;
    private long timerInterval;
    private int timerLoopCount;

    @Override
    public boolean onRequest(Session session, byte[] payload) throws Exception {
        if(session.action().equals("onCheckPermission")){
            Access acc = userService.loadUser(session.distributionId());
            session.write(new PermissionContext(acc.role(),true).toJson().toString().getBytes());
        }

        else if(session.action().equals("onServiceViewList")){
            ClusterProvider.Summary summary = this.deploymentServiceProvider.clusterSummary();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("successful",true);
            JsonArray list = new JsonArray();
            this.deploymentServiceProvider.listServiceView().forEach(v->list.add(v));
            jsonObject.add("list",list);
            JsonArray nodes = new JsonArray();
            JsonArray chs = ((JsonElement)chartConfiguration.property("charts")).getAsJsonArray();
            int[] i = {0};
            summary.clusterNodes().forEach(n->{
                JsonObject nd = new JsonObject();
                nd.addProperty("nodeName",n.nodeName());
                nd.addProperty("memberId",n.memberId());
                nd.add("chart",chs.get(i[0]).getAsJsonObject());
                nodes.add(nd);
                i[0]++;
            });
            jsonObject.add("cluster",nodes);
            session.write(jsonObject.toString().getBytes());
        }
        else if(session.action().equals("onEnableServiceView")){
            ServiceProvider serviceProvider = context.serviceProvider(session.name());
            if(serviceProvider != null){
                viewMap.computeIfAbsent(session.name(),k->{
                        ServiceViewSummary view = new ServiceViewSummary(session.name(),chartConfiguration,()->{
                        ServiceViewSummary removed = viewMap.remove(session.name());
                        this.context.log("Monitor view removed : "+removed.name(),OnLog.WARN);
                    });
                    ServiceViewScheduler monitor = new ServiceViewScheduler(context,serviceProvider,timerInterval,timerLoopCount,view);
                    context.schedule(monitor);
                    return view;
                });
                ServiceViewSummary view = viewMap.get(session.name());
                session.write(view.toCategoryJson().toString().getBytes());
            }
            else{
                session.write(JsonUtil.toSimpleResponse(false,"service provider ["+session.name()+"] not existed").getBytes());
            }
        }
        else if(session.action().equals("onUpdateServiceView")){
            JsonObject query = JsonUtil.parse(payload);
            JsonArray nodes = query.get("nodes").getAsJsonArray();
            JsonArray categories = query.get("categories").getAsJsonArray();
            ServiceViewSummary view = viewMap.get(session.name());
            if(view!=null && categories.size()>0){
                session.write(view.toMetricsJson(nodes,categories).toString().getBytes());
            }
            else{
                session.write(JsonUtil.toSimpleResponse(false,"service view ["+session.name()+"] not existed").getBytes());
            }
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
        this.userService = this.context.serviceProvider(UserService.NAME);
        this.chartConfiguration = this.deploymentServiceProvider.configuration("metrics-view-settings");
        this.timerInterval = ((Number)this.chartConfiguration.property("timerInterval")).longValue();
        this.timerLoopCount = ((Number)this.chartConfiguration.property("timerLoopCount")).intValue();
        this.context.log("Monitor view admin role module started", OnLog.INFO);
    }


}
