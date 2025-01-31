package com.tarantula.platform.configuration;


import com.google.gson.JsonObject;
import com.icodesoftware.DataStore;
import com.icodesoftware.OnAccess;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.ServiceContext;

import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.item.ConfigurableObject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SeasonCredentialConfiguration extends CredentialConfiguration {

    private static TarantulaLogger logger = JDKLogger.getLogger(SeasonCredentialConfiguration.class);

    public enum Faction { Human, Mech, Pteran, Felian, Crearen, Macropoxian, Skellis};
    private final List<Season> seasons = new ArrayList<>();
    public SeasonCredentialConfiguration(String typeId, ConfigurableObject configurableObject){
        super(typeId,OnAccess.SEASON,configurableObject);
    }

    public LocalDateTime startTime(){
        return TimeUtil.fromString("yyyy-MM-dd'T'HH:mm",header.get("StartTime").getAsString());
    }

    public List<Season> list(){
        return seasons;
    }

    public boolean setup(ServiceContext serviceContext, DataStore dataStore){
        logger.warn(name());
        reference.forEach(id->{
            ConfigurableObject configurationObject = new ConfigurableObject();
            configurationObject.distributionId(id.getAsLong());
            if(this.dataStore.load(configurationObject)){
                Season season = new Season();
                season.faction1 = Faction.values()[configurationObject.header().get("Faction1").getAsInt()];
                season.faction2 = Faction.values()[configurationObject.header().get("Faction2").getAsInt()];
                season.faction3 = Faction.values()[configurationObject.header().get("Faction3").getAsInt()];
            }
            else{
                logger.warn("Season ["+id.getAsLong()+"] cannot be loaded");
            }
        });
        return true;
    }

    public static class Season{
        public Faction faction1;
        public Faction faction2;
        public Faction faction3;
    }

}
