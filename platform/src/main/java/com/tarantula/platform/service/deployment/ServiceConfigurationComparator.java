package com.tarantula.platform.service.deployment;
import java.util.Comparator;

/**
 * Updated by yinghu lu on 10/9/2018.
 */
public class ServiceConfigurationComparator implements Comparator<ServiceConfiguration> {

    public int compare(ServiceConfiguration o1, ServiceConfiguration o2) {
        int diff = o1.priority-o2.priority;
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
