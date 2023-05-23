package com.tarantula.game.module;

import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.MappingObject;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.item.ConfigurableObject;
import com.tarantula.platform.presence.saves.PlayerSaveIndex;


public class KeyValueDataStoreModule implements Module,Configurable.Listener<ConfigurableObject>{

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
                String[] query = session.name().split("#");
                PlayerSaveIndex savedGame = gameServiceProvider.presenceServiceProvider().loadPlayerSaveIndex(session.systemId());
                if(savedGame.addKey(query[1])) savedGame.update();
                MappingObject mo = new MappingObject();
                mo.distributionKey(query[0]);
                mo.label(query[1]);
                mo.value(payload);
                boolean suc = dataStore.update(mo);
                session.write(JsonUtil.toSimpleResponse(suc,suc?"saved":"not saved").getBytes());
            }
        }
        else if(session.action().equals("onGet")){
            String[] query = session.name().split("#");
            PlayerSaveIndex savedGame = gameServiceProvider.presenceServiceProvider().loadPlayerSaveIndex(session.systemId());
            if(savedGame.addKey(query[1])) savedGame.update();
            MappingObject mo = new MappingObject();
            mo.distributionKey(query[0]);
            mo.label(query[1]);
            byte[] v = null;
            if(dataStore.load(mo)){
                v = mo.value();
            }
            session.write(v!=null?v:JsonUtil.toSimpleResponse(false,"no data saved").getBytes());
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
