package com.tarantula.platform.service.deployment;


import com.icodesoftware.Access;
import com.icodesoftware.Descriptor;
import com.icodesoftware.Event;
import com.icodesoftware.TarantulaLogger;
import com.tarantula.*;
import com.tarantula.logging.JDKLogger;
import com.tarantula.platform.*;
import com.tarantula.platform.service.ApplicationAllocator;
import com.tarantula.platform.service.Application;
import com.tarantula.platform.service.Instance;


import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Developer: YINGHU LU
 * Updated : 7/29/2020
 */
public class DefaultApplication implements Application {

    private static final TarantulaLogger log = JDKLogger.getLogger(DefaultApplication.class);

    protected final TarantulaContext tarantulaContext;
    protected final DeploymentDescriptor deploymentDescriptor;

    protected ApplicationAllocator applicationRegistry;

    protected ConcurrentHashMap<String,InstanceIndex> onAvailable = new ConcurrentHashMap();

    protected HashMap<String,Configuration> configurations = new HashMap<>();

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
        List<Configuration> clist = this.tarantulaContext.configurations(this.deploymentDescriptor.typeId());
        if(clist!=null){
            clist.forEach((c)->{
                this.configurations.put(c.type(),c);
            });
        }
        //log.warn("Application ["+this.deploymentDescriptor.name()+"/"+this.deploymentDescriptor.distributionKey()+"] started");
    }

    @Override
    public void shutdown() throws Exception {
        onAvailable.forEach((String k,InstanceRegistry ir)->{
            ir.disabled(true);
            this.tarantulaContext.deploymentService().register(ir);
        });
        onAvailable.clear();
        log.warn("Application ["+this.deploymentDescriptor.name()+"/"+this.deploymentDescriptor.distributionKey()+"] shutdown");
    }
    public void unload(String instanceId){
       InstanceRegistry ir = onAvailable.remove(instanceId);
       ir.disabled(true);
       this.tarantulaContext.masterDataStore().update(ir);
       this.tarantulaContext.deploymentService().register(ir);
       log.warn("Instance ["+ir.distributionKey()+"] unloaded");
    }
    private InstanceIndex _loadIndex(String rid){
        return onAvailable.computeIfAbsent(rid,(k)->{
            InstanceIndex ir = new InstanceIndex();
            ir.applicationId(this.deploymentDescriptor.distributionKey());
            ir.distributionKey(rid);
            if(this.tarantulaContext.masterDataStore().load(ir)&&(!ir.disabled())){
                return ir;
            }else{
                //log.warn("Instance not available ->"+rid);
                return null;
            }
        });
    }
    public TarantulaApplicationContext launch(Event event){
        InstanceIndex index = _loadIndex(event.instanceId());
        if(index!=null){
            DeploymentDescriptor dd = this.deploymentDescriptor.deploy(index.distributionKey());
            dd.owner(index.owner());
            TarantulaApplicationContext app = this.launch(dd,index);
            return app;
        }else{
            return null;
        }
    }
    protected TarantulaApplicationContext launch(DeploymentDescriptor dd,InstanceIndex instanceRegistry){ //private instance launched by owner
        try{
            TarantulaApplicationContext app;
            TarantulaApplication _app = (TarantulaApplication)Class.forName(this.deploymentDescriptor.applicationClassName()).getConstructor().newInstance();
            _app.descriptor(dd);
            app = new TarantulaApplicationContext(tarantulaContext,dd,_app,instanceRegistry,this.configurations);
            if(instanceRegistry!=null){
                instanceRegistry.applicationContext = app;
            }
            return app;

        }catch (Exception ex){
            log.error("error on launch",ex);
            throw new RuntimeException(ex);
        }
    }

    @Override
    public  Configuration configuration(String type){
        return this.configurations.get(type);
    }


    public synchronized void load(Instance instance){
        onAvailable.forEach((k,v)->{
           if(v.routingNumber()==instance.partition()){
               v.typeId(this.deploymentDescriptor.typeId());
               v.subtypeId(this.deploymentDescriptor.tag());
               this.tarantulaContext.deploymentService().register(v);
               instance.onPartition(k);
           }
       });
    }
    public boolean launch(Instance instance){
        InstanceRegistry instanceRegistry = this.applicationRegistry.allocate(instance.partition());
        if(instanceRegistry!=null){
            instanceRegistry.typeId(this.deploymentDescriptor.typeId());
            instanceRegistry.subtypeId(this.deploymentDescriptor.tag());
            this.tarantulaContext.deploymentService().register(instanceRegistry);
            instance.onPartition(instanceRegistry.distributionKey());
            return true;
        }
        else{
            return false;
        }
    }
    public void atMidnight(){

    }
}
