/*
 * Copyright ©2023. Jingfeng Wu.
 */

package org.cloudbus.nativesim.provisioner;

import lombok.Getter;
import lombok.Setter;
import org.cloudbus.nativesim.entity.Instance;

@Getter
@Setter
public abstract class InstanceBwProvisioner {
    private long totalBw; //total bw;
    private long availableBw;

    public InstanceBwProvisioner(long totalBw) {
        setTotalBw(totalBw);
        setAvailableBw(totalBw);
    }

    public abstract boolean allocateBwForInstance(Instance instance, long bw);

    public abstract long getAllocatedBwForInstance(Instance instance);

    public abstract void deallocateBwForInstance(Instance instance);

    public void deallocateBwForAllInstances() {
        setAvailableBw(getTotalBw());
    }
    public abstract boolean isSuitableForInstance(Instance instance, long bw);

    public long getUsedBw() {
        return totalBw - availableBw;
    }

}
