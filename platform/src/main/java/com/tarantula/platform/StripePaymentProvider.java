package com.tarantula.platform;
//import com.stripe.Stripe;
//import com.stripe.model.Charge;
import java.util.Map;

/**
 * Created by yinghu lu on 3/21/2019.
 */
public class StripePaymentProvider extends OAuthObject {


    public StripePaymentProvider(String clientId, String secureKey) {
        super("stripe", clientId, secureKey, null, null, null, null);
    }
    @Override
    public boolean validate(Map<String,Object> params){
        return false;
    }
    /**
    @Override
    public boolean validate(Map<String,Object> params){
        try {
            Stripe.apiKey = secureKey();
            Charge c = Charge.create(params);
            params.put("stripe_paid",c.getPaid());
            return true;
        }catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
    }**/
}
