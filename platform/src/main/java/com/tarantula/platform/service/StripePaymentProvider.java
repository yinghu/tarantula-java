package com.tarantula.platform.service;
import com.stripe.Stripe;
import com.stripe.model.Charge;

import java.util.HashMap;
import java.util.Map;

public class StripePaymentProvider extends AuthObject {


    public StripePaymentProvider(String clientId, String secureKey) {
        super("stripe", clientId, secureKey, null, null, null, null);
    }

    @Override
    public boolean validate(Map<String,Object> params){
        try {
            Map<String,Object> requestParams = new HashMap<>();
            requestParams.put("source",params.get("source"));
            requestParams.put("amount",params.get("amount"));
            requestParams.put("currency",params.get("currency"));
            requestParams.put("description",params.get("description"));
            Stripe.apiKey = secureKey();
            Charge c = Charge.create(requestParams);
            boolean paid = c.getPaid();
            if(paid) metricsListener.onUpdated(Metrics.STRIPE_COUNT,1);
            return  paid;
        }catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
    }
}
