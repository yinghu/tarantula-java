package com.tarantula.service.payment;

import com.tarantula.platform.ResponseHeader;

import java.util.List;

/**
 * Updated by yinghu on 5/17/2018
 * .
 */
public class MarketplaceContext  extends ResponseHeader {

    public MarketplaceContext(String command){
        super(command);
    }

    public String paymentClientId ="client id";
    public VirtualCreditsPack onCheckout;
    public List<VirtualCreditsPack> virtualCreditsPackList;
}
