package com.tarantula.platform.service.metrics;


import com.icodesoftware.Distributable;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.Metrics;
import com.icodesoftware.service.ServiceContext;

public class PaymentMetrics extends AbstractMetrics{

    public final static String PAYMENT_GOOGLE_STORE_COUNT = "googleStoreCount";
    public final static String PAYMENT_APPLE_STORE_COUNT = "appleStoreCount";
    public final static String PAYMENT_STRIPE_COUNT = "stripeCount";
    public final static String PAYMENT_GOOGLE_STORE_AMOUNT = "googleStoreAmount";
    public final static String PAYMENT_APPLE_STORE_AMOUNT = "appleStoreAmount";
    public final static String PAYMENT_STRIPE_AMOUNT = "stripeAmount";

    @Override
    void _setup(ServiceContext serviceContext) {
        this.name = Metrics.PAYMENT;
        this.logger = JDKLogger.getLogger(PaymentMetrics.class);
        this.dataStore = serviceContext.dataStore(Distributable.LOCAL_SCOPE,"tarantula_payment_metrics");
    }
}
