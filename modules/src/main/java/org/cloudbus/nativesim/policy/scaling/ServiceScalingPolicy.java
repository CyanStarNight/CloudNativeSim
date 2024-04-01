package org.cloudbus.nativesim.policy.scaling;

import org.cloudbus.nativesim.service.Service;

public abstract class ServiceScalingPolicy {

    public boolean needScaling(Service service) {
        return false;
    }

    public void scalingService(Service serviceToScaling) {
    }
}
