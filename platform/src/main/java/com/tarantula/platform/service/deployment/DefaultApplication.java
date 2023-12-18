package com.tarantula.platform.service.deployment;


import com.icodesoftware.*;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.Metrics;
import com.tarantula.platform.*;
import com.tarantula.platform.service.ApplicationProvider;


public class DefaultApplication implements ApplicationProvider {

    private static final TarantulaLogger log = JDKLogger.getLogger(DefaultApplication.class);

    protected final TarantulaContext tarantulaContext;
    protected final DeploymentDescriptor deploymentDescriptor;

    public DefaultApplication(final TarantulaContext tarantulaContext, final DeploymentDescriptor deploymentDescriptor){
        this.tarantulaContext = tarantulaContext;
        this.deploymentDescriptor = deploymentDescriptor;
    }

    public boolean checkAccessControl(Event event){
        if(this.deploymentDescriptor.accessMode() == Access.PUBLIC_ACCESS_MODE){
            return true;
        }
        return this.tarantulaContext.tokenValidatorProvider().tokenValidator().validateTicket(event);
    }
    //@Override
    public Descriptor descriptor() {
        return this.deploymentDescriptor;
    }

    //@Override
    public void start() throws Exception{
    }

    //@Override
    public void shutdown() throws Exception {
    }
    protected TarantulaApplicationContext launch(DeploymentDescriptor dd){ //private instance launched by owner
        try{
            TarantulaApplicationContext app;
            TarantulaApplication _app = (TarantulaApplication)Class.forName(this.deploymentDescriptor.applicationClassName()).getConstructor().newInstance();
            _app.descriptor(dd);
            app = new TarantulaApplicationContext(tarantulaContext,dd,_app);
            app.registerMetricsListener(tarantulaContext.metrics(Metrics.APPLICATION));
            return app;

        }catch (Exception ex){
            log.error("error on launch",ex);
            throw new RuntimeException(ex);
        }
    }

    public void atMidnight(){

    }
}
