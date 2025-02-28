package com.tarantula.platform.configuration;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.DataStore;
import com.icodesoftware.OnAccess;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.ServiceContext;

import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.item.ConfigurableObject;
import com.tarantula.platform.presence.pvp.PvpErrorCode;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
                season.seasonId = configurationObject.distributionId();
                season.faction1 = Faction.values()[configurationObject.header().get("Faction1").getAsInt()];
                season.faction2 = Faction.values()[configurationObject.header().get("Faction2").getAsInt()];
                season.faction3 = Faction.values()[configurationObject.header().get("Faction3").getAsInt()];
                seasons.add(season);
            }
            else{
                logger.warn("Season ["+id.getAsLong()+"] cannot be loaded");
            }
        });
        return true;
    }

    @Override
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("SeasonId",distributionId);
        JsonArray list = new JsonArray();
        seasons.forEach(season -> list.add(season.toJson()) );
        jsonObject.add("_seasons",list);
        return jsonObject;
    }

    public static class Season extends ConfigurableObject{
        public long seasonId;
        public Faction faction1;
        public Faction faction2;
        public Faction faction3;

        public JsonObject toJson(){
            if(seasonId>0){
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("Successful",true);
                jsonObject.addProperty("SeasonId",seasonId);
                jsonObject.addProperty("EndTime",LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
                jsonObject.addProperty("Faction1",faction1.ordinal());
                jsonObject.addProperty("Faction2",faction2.ordinal());
                jsonObject.addProperty("Faction3",faction3.ordinal());
                return jsonObject;
            }
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("Successful",false);
            jsonObject.addProperty("ErrorCode", PvpErrorCode.NO_SEASON_SCHEDULED);
            jsonObject.addProperty("Message","No season scheduled");
            return jsonObject;
        }

        @Override
        public long distributionId(){
            return seasonId;
        }
    }

}
