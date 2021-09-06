package com.tarantula.platform.service.deployment;


import com.icodesoftware.*;
import com.icodesoftware.logging.JDKLogger;
import com.tarantula.platform.*;
import com.tarantula.platform.service.Application;
import java.util.HashMap;
import java.util.List;

public class DefaultApplication implements Application {

    private static final TarantulaLogger log = JDKLogger.getLogger(DefaultApplication.class);

    protected final TarantulaContext tarantulaContext;
    protected final DeploymentDescriptor deploymentDescriptor;


    //protected HashMap<String, Configuration> configurations = new HashMap<>();

    public DefaultApplication(final TarantulaContext tarantulaContext, final DeploymentDescriptor deploymentDescriptor){
        this.tarantulaContext = tarantulaContext;
        this.deploymentDescriptor = deploymentDescriptor;
    }

    public boolean checkAccessControl(Event event){
        if(this.deploymentDescriptor.accessMode()== Access.PUBLIC_ACCESS_MODE){
            return true;
        }
        return this.tarantulaContext.tokenValidatorProvider().tokenValidator().validateTicket(event.systemId(),event.stub(),event.ticket());
    }
    @Override
    public Descriptor descriptor() {
        return this.deploymentDescriptor;
    }

    @Override
    public void start() throws Exception{
        //List<Configuration> clist = this.tarantulaContext.configurations(this.deploymentDescriptor.typeId());
        //if(clist!=null){
            //clist.forEach((c)->{
                //this.configurations.put(c.configurationName(),c);
        //});
        //}
        //log.warn("Application ["+this.deploymentDescriptor.name()+"/"+this.deploymentDescriptor.distributionKey()+"] started");
    }

    @Override
    public void shutdown() throws Exception {
        //onAvailable.forEach((String k,InstanceRegistry ir)->{
            //ir.disabled(true);
            //this.tarantulaContext.deploymentService().register(ir);
        //});
        //onAvailable.clear();
        //log.warn("Application ["+this.deploymentDescriptor.name()+"/"+this.deploymentDescriptor.distributionKey()+"] shutdown");
    }
    protected TarantulaApplicationContext launch(DeploymentDescriptor dd){ //private instance launched by owner
        try{
            TarantulaApplicationContext app;
            TarantulaApplication _app = (TarantulaApplication)Class.forName(this.deploymentDescriptor.applicationClassName()).getConstructor().newInstance();
            _app.descriptor(dd);
            app = new TarantulaApplicationContext(tarantulaContext,dd,_app);
            return app;

        }catch (Exception ex){
            log.error("error on launch",ex);
            throw new RuntimeException(ex);
        }
    }

    public void atMidnight(){

    }
}
