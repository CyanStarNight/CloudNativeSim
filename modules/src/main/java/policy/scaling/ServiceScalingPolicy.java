/*
 * Copyright ©2024. Jingfeng Wu.
 */

package policy.scaling;

import entity.Service;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public abstract class ServiceScalingPolicy {

    private int schedulingInterval = 10;

    public boolean needScaling(Service service) {
        return false;
    }

    public void scalingService(Service serviceToScaling) {
    }
}
