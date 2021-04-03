package com.tarantula.platform.service;
import com.stripe.Stripe;
import com.stripe.model.Charge;
import java.util.Map;

public class StripePaymentProvider extends AuthObject {


    public StripePaymentProvider(String clientId, String secureKey) {
        super("stripe", clientId, secureKey, null, null, null, null);
    }

    @Override
    public boolean validate(Map<String,Object> params){
        try {
            Stripe.apiKey = secureKey();
            Charge c = Charge.create(params);
            params.put("stripe_paid",c.getPaid());
            metricsListener.onUpdated(Metrics.STRIPE_COUNT,1);
            return true;
        }catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
    }
}
