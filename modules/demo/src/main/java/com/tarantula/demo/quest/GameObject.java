package com.tarantula.demo.quest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.tarantula.Connection;
import com.tarantula.platform.OnApplicationHeader;
import com.tarantula.platform.util.ConnectionSerializer;

import java.lang.reflect.Type;

/**
 * Created by yinghu 9/30/2019
 */

public class GameObject extends OnApplicationHeader {

    private Connection connection;
    private String ticket;
    private RobotQuest robotQuest;

    public GameObject(Connection connection, String ticket,RobotQuest robotQuest){
        this.successful = true;
        this.command = "onJoin";
        this.connection = connection;
        this.ticket = ticket;
        this.robotQuest = robotQuest;
    }

    public JsonElement setup(Type type, JsonSerializationContext jsonSerializationContext){
        JsonObject jsonObject = new JsonObject();
        if(connection!=null){
            jsonObject.add("connection",new ConnectionSerializer().serialize(connection,type,jsonSerializationContext));
            jsonObject.addProperty("ticket",ticket);
        }
        if(robotQuest!=null){
            jsonObject.add("robotQuest",new RobotQuestSerializer().serialize(robotQuest,type,jsonSerializationContext));
        }
        return jsonObject;
    }
}
