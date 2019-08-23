package com.tarantula.platform.marketplace;

import com.tarantula.*;
import com.tarantula.platform.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Developer: YINGHU LU
 * Date updated: 2/16/2019
 * Time: 6:32 PM
 */
public class MarketplaceApplication extends TarantulaApplicationHeader {


    private SmartPackageGenerator smartPackageGenerator;
    private TokenValidator.OAuthVendor strip;
    private DataStore dataStore;
    @Override
    public void setup(ApplicationContext context) throws Exception {
        super.setup(context);
        this.builder.registerTypeAdapter(MarketplaceContext.class,new MarketplaceContextSerializer());
        this.dataStore = context.dataStore("marketplace");
        Configuration cfg = context.configuration("setup");
        double basePrice = Double.parseDouble(cfg.property("basePrice"));
        double baseVirtualCredits = Double.parseDouble(cfg.property("baseVirtualCredits"));
        int basePackageSize = Integer.parseInt(cfg.property("basePackageSize"));
        smartPackageGenerator = new SmartPackageGenerator(basePackageSize,basePrice,baseVirtualCredits,this.dataStore);
        smartPackageGenerator.start();
        strip = this.context.validator().vendor("stripe");
        this.context.log("Marketplace application started on tag ["+descriptor.tag()+"]",OnLog.INFO);
    }


    @Override
    public void callback(Session session,byte[] payload) throws Exception{
        OnAccess ex = this.builder.create().fromJson(new String(payload),OnAccess.class);
        if(session.action().equals("onList")){
            MarketplaceContext mc = new MarketplaceContext(session.action());
            mc.virtualCreditsPackList = this.smartPackageGenerator.list(Double.parseDouble(ex.header("requestingCredits")),Integer.parseInt(ex.header("packSize")));
            session.write(this.builder.create().toJson(mc).getBytes(),this.descriptor.responseLabel());
        }
        else if(session.action().equals("onCommit")){ //finish the checkout process with confirmation ticket to verify
            VirtualCreditsPack co = new VirtualCreditsPack();
            co.distributionKey(ex.header("checkoutId"));
            boolean suc = false;
            if(this.dataStore.load(co)){
                Map<String, Object> chargeParams = new HashMap<>();
                chargeParams.put("amount",Double.valueOf(co.price).intValue());//pass penney number as integer
                chargeParams.put("currency", "usd");
                chargeParams.put("description", "Charge for ["+co.distributionKey()+"]");
                chargeParams.put("source",ex.header("orderId"));
                if(strip.validate(chargeParams)){
                    //charge successfully
                    OnBalanceTrack onBalanceTrack = new OnBalanceTrack(session.systemId(),co.credits);
                    this.context.postOffice().onTag(Presence.LOBBY_TAG).send(session.systemId(),onBalanceTrack);
                    suc = true;
                }
            }
            MarketplaceContext mc = new MarketplaceContext(session.action());
            mc.successful(suc);
            session.write(this.builder.create().toJson(mc).getBytes(),this.descriptor.responseLabel());
        }
    }


}
