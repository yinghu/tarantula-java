package com.tarantula.platform.service.deployment;

import com.tarantula.platform.DeploymentDescriptor;

import java.util.Comparator;

public class DeploymentDescriptorComparator implements Comparator<DeploymentDescriptor> {

    public int compare(DeploymentDescriptor o1, DeploymentDescriptor o2) {
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
