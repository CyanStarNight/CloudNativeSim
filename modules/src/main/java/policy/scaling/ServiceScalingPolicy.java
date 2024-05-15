/*
 * Copyright ©2024. Jingfeng Wu.
 */

package policy.scaling;

import service.Service;

public abstract class ServiceScalingPolicy {

    public boolean needScaling(Service service) {
        return false;
    }

    public void scalingService(Service serviceToScaling) {
    }
}
