package com.tarantula.game.module;

import com.google.gson.GsonBuilder;
import com.tarantula.*;
import com.tarantula.Module;
import com.tarantula.game.MappingObject;
import com.tarantula.platform.ResponseHeader;
import com.tarantula.platform.service.DeploymentServiceProvider;
import com.tarantula.game.service.GameServiceProvider;
import com.tarantula.platform.util.ResponseSerializer;
import com.tarantula.platform.util.SystemUtil;

public class KeyValueDataStoreModule implements Module {

    private ApplicationContext context;
    private GsonBuilder builder;
    private DataStore dataStore;
    private DeploymentServiceProvider deploymentServiceProvider;
    private GameServiceProvider gameServiceProvider;
    private int maxSizeOnSet;
    @Override
    public boolean onRequest(Session session, byte[] payload, OnUpdate update) throws Exception {
        if(session.action().equals("onSet")){
            if(payload.length>maxSizeOnSet){
                ResponseHeader resp = new ResponseHeader("onSet","payload size ["+payload.length+"] cannot be over ["+maxSizeOnSet+"]",false);
                session.write(builder.create().toJson(resp).getBytes(),label());
            }else{
                String key = session.systemId()+Recoverable.PATH_SEPARATOR+session.name();
                MappingObject mo = new MappingObject();
                mo.distributionKey(session.systemId());
                mo.label(session.name());
                mo.fromMap(SystemUtil.toMap(payload));
                dataStore.create(mo);
                ResponseHeader resp = new ResponseHeader("onSet","Saved on key ["+key+"]",true);
                session.write(builder.create().toJson(resp).getBytes(),label());
            }
        }
        else if(session.action().equals("onGet")){
            //String key = session.systemId()+Recoverable.PATH_SEPARATOR+session.name();
            MappingObject mo = new MappingObject();
            mo.distributionKey(session.systemId());
            mo.label(session.name());
            byte[] v = null;
            if(dataStore.load(mo)){
                v = SystemUtil.toJson(mo.toMap());
            }
            session.write(v!=null?v:"{}".getBytes(),label());
        }
        else{
            throw new UnsupportedOperationException(session.action());
        }
        return false;
    }

    @Override
    public void setup(ApplicationContext context) throws Exception {
        this.context = context;
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(ResponseHeader.class,new ResponseSerializer());
        this.dataStore = this.context.dataStore(this.context.descriptor().typeId().replace("-","_"));//typeId_data
        gameServiceProvider = new GameServiceProvider(this.context.descriptor().typeId().replace("-data","-service"));
        deploymentServiceProvider = this.context.serviceProvider(DeploymentServiceProvider.NAME);
        deploymentServiceProvider.register(gameServiceProvider);
        this.maxSizeOnSet = this.context.descriptor().capacity();
        this.context.log("Data store ["+this.context.descriptor().typeId()+" started with max size on set call ["+maxSizeOnSet+"]", OnLog.WARN);
    }
    @Override
    public String label() {
        return this.context.descriptor().typeId();
    }
    @Override
    public void clear(){
        this.deploymentServiceProvider.release(gameServiceProvider);
    }
}
