package com.tarantula.platform.service.metrics;


import com.icodesoftware.service.Metrics;
import com.icodesoftware.service.ServiceContext;

public class PaymentMetrics extends AbstractMetrics{

    public final static String GOOGLE_STORE_COUNT = "googleStoreCount";
    public final static String APPLE_STORE_COUNT = "appleStoreCount";
    public final static String STRIPE_COUNT = "stripeCount";

    public final static String GOOGLE_STORE_AMOUNT = "googleStoreAmount";
    public final static String APPLE_STORE_AMOUNT = "appleStoreAmount";
    public final static String STRIPE_AMOUNT = "stripeAmount";


    @Override
    void _setup(ServiceContext serviceContext) {
        this.name = Metrics.PAYMENT;
        this.logger = serviceContext.logger(PaymentMetrics.class);
        this.categories = new String[6];
        this.categories[0]=GOOGLE_STORE_COUNT;
        this.categories[1]=APPLE_STORE_COUNT;
        this.categories[2]=STRIPE_COUNT;
        this.categories[3]=GOOGLE_STORE_AMOUNT;
        this.categories[4]=APPLE_STORE_AMOUNT;
        this.categories[5]=STRIPE_AMOUNT;
        this.dataStore = serviceContext.dataStore("payment_metrics",serviceContext.partitionNumber());
    }
}
