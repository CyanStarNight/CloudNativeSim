/*
 * Copyright ©2024. Jingfeng Wu.
 */

package policy.scaling;

import entity.Service;

public abstract class ServiceScalingPolicy {

    public boolean needScaling(Service service) {
        return false;
    }

    public void scalingService(Service serviceToScaling) {
    }
}
