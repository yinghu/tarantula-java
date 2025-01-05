package com.tarantula.platform.service.metrics;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.util.BufferProxy;
import com.icodesoftware.service.ServiceProvider;
import com.icodesoftware.util.RecoverableObject;


public class ServiceViewRequest extends RecoverableObject implements ServiceProvider.Summary{



    private String memberId;
    private final JsonArray metrics;

    private ServiceViewRequest(String memberId){
        this();
        this.memberId = memberId;
    }

    private ServiceViewRequest(){
        this.metrics = new JsonArray();
    }


    @Override
    public void update(String category, int value) {
        _update(category,value);
    }

    @Override
    public void update(String category, long value) {
        _update(category,value);
    }

    @Override
    public void update(String category, double value) {
        _update(category,value);
    }

    @Override
    public void registerCategory(String category) {

    }

    @Override
    public byte[] toBinary() {
        DataBuffer dataBuffer = BufferProxy.buffer(1000,false);
        dataBuffer.writeUTF8(memberId);
        dataBuffer.writeInt(metrics.size());
        metrics.forEach(e->{
            JsonObject m = e.getAsJsonObject();
            dataBuffer.writeUTF8(m.get("category").getAsString());
            dataBuffer.writeDouble(m.get("value").getAsDouble());
        });
        return dataBuffer.array();
    }

    @Override
    public void fromBinary(byte[] payload) {
        DataBuffer dataBuffer = BufferProxy.wrap(payload);
        memberId = dataBuffer.readUTF8();
        int sz = dataBuffer.readInt();
        for(int i=0;i<sz;i++){
            _update(dataBuffer.readUTF8(),dataBuffer.readDouble());
        }
    }

    @Override
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("memberId",memberId);
        jsonObject.add("metrics",metrics);
        return jsonObject;
    }

    private void _update(String category, Object value){
        JsonObject m = new JsonObject();
        m.addProperty("category",category);
        m.addProperty("value",value.toString());
        metrics.add(m);
    }
    public static ServiceViewRequest request(String memberId){
        return new ServiceViewRequest(memberId);
    }
    public static ServiceViewRequest response(byte[] payload){
        ServiceViewRequest response = new ServiceViewRequest();
        response.fromBinary(payload);
        return response;
    }
}
