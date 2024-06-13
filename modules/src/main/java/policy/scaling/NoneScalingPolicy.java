/*
 * Copyright ©2024. Jingfeng Wu.
 */

package policy.scaling;

import entity.Service;

public class NoneScalingPolicy extends ServiceScalingPolicy{

    @Override
    public boolean needScaling(Service service) {
        return false;
    }

    @Override
    public void scaling(Service service) {

    }
}
