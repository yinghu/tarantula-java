package com.tarantula.service.payment;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.stripe.Stripe;
import com.stripe.model.Charge;
import com.tarantula.*;
import com.tarantula.Module;
import com.tarantula.platform.util.OnAccessDeserializer;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class StripePaymentService implements Module {

    private ApplicationContext context;

    private String privateKey;
    private String publicKey;

    private GsonBuilder builder;
    @Override
    public void onJoin(Session session) throws Exception{
        //session.write(ret,this.label());
    }

    @Override
    public boolean onRequest(Session session, byte[] payload, OnUpdate onUpdate) throws Exception {
        session.write(payload,label());
        OnAccess ex = this.builder.create().fromJson(new String(payload),OnAccess.class);
        Map<String, Object> chargeParams = new HashMap<>();
        chargeParams.put("amount",3000);//pass penney number as integer
        chargeParams.put("currency", "usd");
        chargeParams.put("description", "Charge for ["+"sample"+"]");
        chargeParams.put("source",ex.header("orderId"));
        if(validate(chargeParams)){
            //
        }
        return false;
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        this.context = applicationContext;
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(OnAccess.class,new OnAccessDeserializer());
        this.context.resource("stripe-credentials.json",(in)->{
            JsonParser jp = new JsonParser();
            JsonObject jo = jp.parse(new InputStreamReader(in)).getAsJsonObject();
            privateKey = jo.get("private_key").getAsString();
            publicKey = jo.get("public_key").getAsString();
        });
        this.context.log("Stripe payment service started", OnLog.INFO);
    }

    @Override
    public String label() {
        return "stripe";
    }
    @Override
    public void clear(){

    }
    private boolean validate(Map<String,Object> params){
        try {
            Stripe.apiKey = privateKey;
            Charge c = Charge.create(params);
            params.put("stripe_paid",c.getPaid());
            return true;
        }catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
    }
}
