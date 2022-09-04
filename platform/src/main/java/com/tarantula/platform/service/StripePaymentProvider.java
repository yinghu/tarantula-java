package com.tarantula.platform.service;
import com.icodesoftware.OnAccess;
import com.icodesoftware.service.TokenValidatorProvider;
import com.stripe.Stripe;
import com.stripe.model.Charge;
import com.tarantula.platform.service.metrics.PaymentMetrics;

import java.util.HashMap;
import java.util.Map;

public class StripePaymentProvider extends AuthObject implements AuthVendorRegistry {

    private String secureKey;
    public StripePaymentProvider(String clientId, String secureKey) {
        super(OnAccess.STRIPE, clientId);
        this.secureKey = secureKey;
    }

    @Override
    public boolean validate(Map<String,Object> params){
        try {
            Map<String,Object> requestParams = new HashMap<>();
            requestParams.put("source",params.get("source"));
            requestParams.put("amount",params.get("amount"));
            requestParams.put("currency",params.get("currency"));
            requestParams.put("description",params.get("description"));
            Stripe.apiKey = secureKey;
            Charge c = Charge.create(requestParams);
            boolean paid = c.getPaid();
            if(paid) {
                metricsListener.onUpdated(PaymentMetrics.PAYMENT_STRIPE_COUNT,1);
                metricsListener.onUpdated(PaymentMetrics.PAYMENT_STRIPE_AMOUNT,(int)params.get("amount"));
            }
            return  paid;
        }catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public void registerAuthVendor(TokenValidatorProvider.AuthVendor authVendor) {

    }

    @Override
    public void releaseAuthVendor(TokenValidatorProvider.AuthVendor authVendor) {

    }
}
