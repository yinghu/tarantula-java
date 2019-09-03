package com.tarantula.service.payment;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.stripe.Stripe;
import com.stripe.model.Charge;
import com.tarantula.*;
import com.tarantula.Module;
import com.tarantula.platform.OnBalanceTrack;
import com.tarantula.platform.util.OnAccessDeserializer;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class StripePaymentService implements Module {

    private ApplicationContext context;

    private String privateKey;
    private String publicKey;

    private SmartPackageGenerator smartPackageGenerator;

    private DataStore dataStore;
    private GsonBuilder builder;
    @Override
    public void onJoin(Session session) throws Exception{
        //session.write(ret,this.label());
    }

    @Override
    public boolean onRequest(Session session, byte[] payload, OnUpdate onUpdate) throws Exception {
        OnAccess ex = this.builder.create().fromJson(new String(payload),OnAccess.class);
        if(session.action().equals("onList")){
            MarketplaceContext mc = new MarketplaceContext(session.action());
            mc.paymentClientId = publicKey;
            mc.virtualCreditsPackList = this.smartPackageGenerator.list(Double.parseDouble(ex.property("requestingCredits")),Integer.parseInt(ex.property("packSize")));
            session.write(this.builder.create().toJson(mc).getBytes(),this.label());
        }
        else if(session.action().equals("onCommit")){ //finish the checkout process with confirmation ticket to verify
            VirtualCreditsPack co = new VirtualCreditsPack();
            co.distributionKey(ex.property("checkoutId"));
            boolean suc = false;
            if(this.dataStore.load(co)){
                Map<String, Object> chargeParams = new HashMap<>();
                chargeParams.put("amount",Double.valueOf(co.price).intValue());//pass penney number as integer
                chargeParams.put("currency", "usd");
                chargeParams.put("description", "Charge for ["+co.distributionKey()+"]");
                chargeParams.put("source",ex.property("orderId"));
                if(validate(chargeParams)){
                    //charge successfully
                    OnBalanceTrack onBalanceTrack = new OnBalanceTrack(session.systemId(),co.credits);
                    this.context.postOffice().onTag(Presence.LOBBY_TAG).send(session.systemId(),onBalanceTrack);
                    suc = true;
                }
            }
            MarketplaceContext mc = new MarketplaceContext(session.action());
            mc.successful(suc);
            session.write(this.builder.create().toJson(mc).getBytes(),this.label());
        }
        return false;
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        this.context = applicationContext;
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(OnAccess.class,new OnAccessDeserializer());
        this.builder.registerTypeAdapter(MarketplaceContext.class,new MarketplaceContextSerializer());
        this.context.resource("stripe-credentials.json",(in)->{
            JsonParser jp = new JsonParser();
            JsonObject jo = jp.parse(new InputStreamReader(in)).getAsJsonObject();
            privateKey = jo.get("private_key").getAsString();
            publicKey = jo.get("public_key").getAsString();
        });
        this.dataStore = this.context.dataStore(this.context.descriptor().typeId());
        double basePrice = 100;
        double baseVirtualCredits = 500000;
        int basePackageSize = 4;
        smartPackageGenerator = new SmartPackageGenerator(basePackageSize,basePrice,baseVirtualCredits,this.dataStore);
        smartPackageGenerator.start();
        this.context.log("Stripe payment service started with data store ["+dataStore.name()+"]", OnLog.INFO);
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
