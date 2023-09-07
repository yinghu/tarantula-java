package com.tarantula.admin;

import com.icodesoftware.Module;
import com.icodesoftware.*;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.item.*;
import com.tarantula.platform.service.ApplicationPreSetup;

import java.util.List;

public class GameApplicationAdminRoleModule implements Module {
    private ApplicationContext context;
    private DeploymentServiceProvider deploymentServiceProvider;
    @Override
    public boolean onRequest(Session session, byte[] payload) throws Exception {
        if(session.action().equals("onStock")){
            String[] query = session.name().split("#");
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(Long.parseLong(query[0]));
            Descriptor app = gameCluster.serviceWithCategory(query[1]);
            ApplicationPreSetup preSetup = gameCluster.applicationPreSetup();
            List<ConfigurableObject> items = preSetup.list(app,new ConfigurableObjectQuery(app.key(),query[2].split("/")[1]));
            session.write(new ItemAdminContext(true,items.size()>0?"Configure store item":"no items configured",items).toJson().toString().getBytes());
        }
        else if(session.action().equals("onLoad")){
            String[] query = session.name().split("#");
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(Long.parseLong(query[0]));
            Descriptor desc = gameCluster.serviceWithCategory(query[2]);
            ApplicationPreSetup preSetup = gameCluster.applicationPreSetup();
            Application app = new Application();
            app.distributionKey(query[1]);
            if(preSetup.load(desc,app)){
                app.setup();
                session.write(new ApplicationSerializer().serialize(app,Application.class,null).toString().getBytes());
            }
            else{
                session.write(JsonUtil.toSimpleResponse(false,query[1]+" not existed").getBytes());
            }
        }
        else if (session.action().equals("onRegister")){

            String[] query = session.name().split("#");
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(Long.parseLong(query[0]));
            PlatformGameServiceProvider gameServiceProvider = this.context.serviceProvider(gameCluster.serviceType());
            ApplicationPreSetup preSetup = gameCluster.applicationPreSetup();
            Application app = new Application();
            app.distributionKey(query[1]);
            Descriptor desc = gameCluster.serviceWithCategory(query[2]);
            if(preSetup.load(desc,app) && app.configureAndValidate()){
                gameServiceProvider.configurationServiceProvider(query[2]).register(app);
                session.write(JsonUtil.toSimpleResponse(true,query[1]).getBytes());
            }
            else{
               session.write(JsonUtil.toSimpleResponse(false,"application items have to have at least one commodity item").getBytes());
            }

        }
        else if (session.action().equals("onRelease")){
            String[] query = session.name().split("#");
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(Long.parseLong(query[0]));
            PlatformGameServiceProvider gameServiceProvider = this.context.serviceProvider(gameCluster.serviceType());
            Application app = new Application();
            app.distributionKey(query[1]);
            Descriptor desc = gameCluster.serviceWithCategory(query[2]);
            if(gameCluster.applicationPreSetup().load(desc,app)){
                gameServiceProvider.configurationServiceProvider(query[2]).release(app);
                session.write(JsonUtil.toSimpleResponse(true,query[1]).getBytes());
            }
            else{
                session.write(JsonUtil.toSimpleResponse(false,"failed to release item").getBytes());
            }
        }
        else {
            throw new UnsupportedOperationException(session.action()+" not supported");
        }
        return false;
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        this.context = applicationContext;
        this.deploymentServiceProvider = context.serviceProvider(DeploymentServiceProvider.NAME);
        this.context.log("Game application admin module started", OnLog.WARN);
    }

}
