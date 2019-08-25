package com.tarantula.service.payment;

import com.tarantula.DataStore;
import com.tarantula.Serviceable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yinghu lu on 2/15/2019.
 */
public class SmartPackageGenerator implements Serviceable {

    private final int basePackageSize;
    private final double basePrice; //fee 5% plus 0.05 per micro transaction (less than 10.00 USD)
    private final double baseVirtualCredits;
    private final DataStore dataStore;

    private final ArrayList<VirtualCreditsPack> plist = new ArrayList<>();


    public SmartPackageGenerator(int basePackageSize, double basePrice, double baseVirtualCredits, DataStore dataStore){
        this.basePackageSize = basePackageSize;
        this.basePrice = basePrice;
        this.baseVirtualCredits = baseVirtualCredits;
        this.dataStore = dataStore;
    }
    public List<VirtualCreditsPack> list(double requestingCredits, int size){

        return plist;
    }
    public List<VirtualCreditsPack> list(double requestingCredits){
        return this.list(requestingCredits,basePackageSize);
    }

    @Override
    public void start() throws Exception {
        VirtualCreditsPack base = new VirtualCreditsPack("Base",basePrice,baseVirtualCredits);
        this.dataStore.create(base);
        VirtualCreditsPack base2x = new VirtualCreditsPack("Base2X",basePrice*2*0.8,baseVirtualCredits*2);
        this.dataStore.create(base2x);
        VirtualCreditsPack base5x = new VirtualCreditsPack("Base5X",basePrice*5*0.7,baseVirtualCredits*5);
        this.dataStore.create(base5x);
        VirtualCreditsPack base10x = new VirtualCreditsPack("Base10X",basePrice*10*0.5,baseVirtualCredits*10);
        this.dataStore.create(base10x);
        plist.add(base);
        plist.add(base2x);
        plist.add(base5x);
        plist.add(base10x);
    }

    @Override
    public void shutdown() throws Exception {

    }
}
