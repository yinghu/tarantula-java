package com.tarantula.platform.presence;

public class SubscriptionFee {
    public String type;
    public String name;
    public String amount;
    public String currency;

    public SubscriptionFee(String type,String name,String amount,String currency){
        this.type = type;
        this.name = name;
        this.amount = amount;
        this.currency =currency;
    }
}
