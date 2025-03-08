package com.icodesoftware.util;

import com.icodesoftware.Descriptor;

import java.util.Comparator;

public class DeploymentDescriptorComparator implements Comparator<Descriptor> {

    public int compare(Descriptor o1, Descriptor o2) {
        int diff = o1.deployPriority()-o2.deployPriority();
        if(diff>0){
            return -1;
        }
        else if(diff<0){
            return 1;
        }
        else{
            return 0;
        }
    }
}
