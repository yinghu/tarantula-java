package com.tarantula.platform.util;

import com.google.gson.*;
import com.tarantula.OnAccess;
import com.tarantula.platform.OnAccessTrack;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Updated by yinghu on 9/2/2019.
 */
public class OnAccessDeserializer implements JsonDeserializer<OnAccess> {


    public OnAccessDeserializer(){}


    public OnAccess deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        OnAccess   access = new OnAccessTrack();
        JsonObject e = jsonElement.getAsJsonObject();
        e.entrySet().forEach((Map.Entry<String,JsonElement> kv)->{
            String k = kv.getKey();
            JsonElement ve = kv.getValue();
            if(ve.isJsonPrimitive()){
                JsonPrimitive jo = ve.getAsJsonPrimitive();
                if(k.equals("systemId")){
                    access.systemId(jo.getAsString());
                }
                else if(k.equals("stub")){
                    access.stub(jo.getAsInt());
                }
                else if(k.equals("applicationId")){
                    access.applicationId(jo.getAsString());
                }
                else if(k.equals("name")){
                    access.name(jo.getAsString());
                }
                else if(k.equals("accessMode")){
                    access.accessMode(jo.getAsInt());
                }
                else if(k.equals("instanceId")){
                    access.instanceId(jo.getAsString());
                }
                else if(k.equals("accessKey")){
                    access.accessKey(jo.getAsString());
                }
                else if(k.equals("accessId")){
                    access.accessId(jo.getAsString());
                }
                else if(k.equals("typeId")){
                    access.typeId(jo.getAsString());
                }
                else if(k.equals("subtypeId")){
                    access.subtypeId(jo.getAsString());
                }
                else if(k.equals("oid")){
                    access.oid(jo.getAsString());
                }
                else if(k.equals("balance")){
                    access.entryCost(jo.getAsDouble());
                }
                else if(k.equals("timestamp")){
                    access.timestamp(jo.getAsLong());
                }
                else{
                    if(!jo.getAsString().trim().equals("")){
                        access.property(k,jo.getAsString());
                    }
                }
            }
            else if(ve.isJsonArray()){
                JsonArray alist = ve.getAsJsonArray();
                alist.forEach((a)->{
                    JsonObject jo = a.getAsJsonObject();
                    String hk = jo.get("name").getAsString();
                    String hv = jo.get("value").getAsString();
                    access.property(hk,hv);
                    _setProperty(access,hk,hv);
                });
            }
        });
        return access;
    }
    private void _setProperty(OnAccess access,String k,String v){
        if(k.equals("systemId")){
            access.systemId(v);
        }
        else if(k.equals("stub")){
            access.stub(Integer.parseInt(v));
        }
        else if(k.equals("applicationId")){
            access.applicationId(v);
        }
        else if(k.equals("name")){
            access.name(v);
        }
        else if(k.equals("accessMode")){
            access.accessMode(Integer.parseInt(v));
        }
        else if(k.equals("instanceId")){
            access.instanceId(v);
        }
        else if(k.equals("accessKey")){
            access.accessKey(v);
        }
        else if(k.equals("accessId")){
            access.accessId(v);
        }
        else if(k.equals("typeId")){
            access.typeId(v);
        }
        else if(k.equals("subtypeId")){
            access.subtypeId(v);
        }
        else if(k.equals("oid")){
            access.oid(v);
        }
        else if(k.equals("balance")){
            access.entryCost(Double.parseDouble(v));
        }
        else if(k.equals("timestamp")){
            access.timestamp(Long.parseLong(v));
        }
    }
}
