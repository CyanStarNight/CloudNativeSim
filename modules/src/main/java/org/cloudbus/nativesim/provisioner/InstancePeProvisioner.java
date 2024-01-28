/*
 * Copyright ©2024. Jingfeng Wu.
 */

package org.cloudbus.nativesim.provisioner;

import lombok.Getter;
import lombok.Setter;
import org.cloudbus.nativesim.entity.Instance;
import org.cloudbus.nativesim.entity.Pod;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public abstract class InstancePeProvisioner {

    private double mips;
    private double availableMips;

    public InstancePeProvisioner(double mips) {
        setMips(mips);
        setAvailableMips(mips);
    }

    public abstract boolean allocateMipsForInstance(Instance instance, double mips);
    public abstract boolean allocateMipsForInstance(String instanceUid, double mips);
    public abstract boolean allocateMipsForInstance(Instance instance, List<Double> mips);

    public abstract List<Double> getAllocatedMipsForInstance(Instance instance);

    public abstract double getTotalAllocatedMipsForInstance(Instance instance);

    public abstract double getAllocatedMipsForInstanceByVirtualPeId(Instance instance, int peId);

    public abstract void deallocateMipsForInstance(Instance instance);

    public void deallocateMipsForAllInstances() {
        setAvailableMips(getMips());
    }

    public double getTotalAllocatedMips() {
        double totalAllocatedMips = getMips() - getAvailableMips();
        if (totalAllocatedMips > 0) {
            return totalAllocatedMips;
        }
        return 0;
    }

    public double getUtilization() {
        return getTotalAllocatedMips() / getMips();
    }

}
