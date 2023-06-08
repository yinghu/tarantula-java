package com.tarantula.game.module;

import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.item.ConfigurableObject;
import com.tarantula.platform.presence.PersonalDataIndex;
import com.tarantula.platform.presence.PersonalDataObject;


public class PersonalDataStoreModule implements Module,Configurable.Listener<ConfigurableObject>{

    private ApplicationContext context;
    private DataStore dataStore;
    private PlatformGameServiceProvider gameServiceProvider;
    private int maxSizeOnSet;
    @Override
    public boolean onRequest(Session session, byte[] payload) throws Exception {
        if(session.action().equals("onSet")){
            if(payload.length>maxSizeOnSet){
                session.write(JsonUtil.toSimpleResponse(false,"data size must be less than ["+maxSizeOnSet+"]").getBytes());
            }else{
                PersonalDataIndex savedIndex = gameServiceProvider.presenceServiceProvider().loadPersonalDataIndex(session.systemId());
                String key = savedIndex.dataKey(session.name());
                PersonalDataObject po = new PersonalDataObject();
                if(key !=null) po.distributionKey(key.split("#")[1]);
                po.value(payload);
                boolean suc;
                if(key==null) {
                    suc = dataStore.create(po);
                    if(suc && savedIndex.addKey(session.name()+"#"+po.distributionKey())){
                        savedIndex.update();
                    }
                }
                else{
                    dataStore.load(po);
                    po.value(payload);
                    suc = dataStore.update(po);
                }
                session.write(JsonUtil.toSimpleResponse(suc,suc?"saved":"not saved").getBytes());
            }
        }
        else if(session.action().equals("onGet")){
            PersonalDataIndex savedIndex = gameServiceProvider.presenceServiceProvider().loadPersonalDataIndex(session.systemId());
            String key = savedIndex.dataKey(session.name());
            if(key == null) {
                session.write(JsonUtil.toSimpleResponse(false,session.name()+ " not existed").getBytes());
            }
            else{
                PersonalDataObject po = new PersonalDataObject();
                po.distributionKey(key.split("#")[1]);
                byte[] v = null;
                if(dataStore.load(po)){
                    v = po.value();
                }
                session.write(v!=null?v:JsonUtil.toSimpleResponse(false,"no data saved").getBytes());
            }
        }
        else if(session.action().equals("onDelete")){
            PersonalDataIndex savedIndex = gameServiceProvider.presenceServiceProvider().loadPersonalDataIndex(session.systemId());
            String key = savedIndex.dataKey(session.name());
            if(key == null) {
                session.write(JsonUtil.toSimpleResponse(false,session.name()+ " not existed").getBytes());
            }
            else{
                if(dataStore.delete(key.split("#")[1].getBytes()) && savedIndex.removeKey(key)){
                    savedIndex.update();
                }
                session.write(JsonUtil.toSimpleResponse(true,key+" deleted").getBytes());
            }
        }
        else{
            throw new UnsupportedOperationException(session.action());
        }
        return false;
    }

    @Override
    public void setup(ApplicationContext context) throws Exception {
        this.context = context;
        this.gameServiceProvider = this.context.serviceProvider(this.context.descriptor().typeId().replace("-data","-service"));
        this.dataStore = this.context.dataStore("player");
        this.maxSizeOnSet = this.gameServiceProvider.gameCluster().maxDataSizeCount();
        this.gameServiceProvider.configurationServiceProvider().registerConfigurableListener(this.context.descriptor(),this);
        this.gameServiceProvider.exportServiceModule(this.context.descriptor().tag(),this);
        this.context.log("Data store module ["+this.context.descriptor().typeId()+" started with max size on set call ["+maxSizeOnSet+"]", OnLog.WARN);
    }

    @Override
    public void clear(){

    }
}
