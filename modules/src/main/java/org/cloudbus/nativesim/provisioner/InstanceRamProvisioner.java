/*
 * Copyright ©2023. Jingfeng Wu.
 */

package org.cloudbus.nativesim.provisioner;

import lombok.Getter;
import lombok.Setter;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;
import org.cloudbus.nativesim.entity.Container;
import org.cloudbus.nativesim.entity.Instance;
import org.cloudbus.nativesim.entity.Pod;

@Getter
@Setter
public abstract class InstanceRamProvisioner {
    private int ram;
    private int availableRam;


    public InstanceRamProvisioner(int ram) {
        setRam(ram);
        setAvailableRam(ram);
    }

    public abstract boolean allocateRamForInstance(Instance instance, int ram);

    public abstract int getAllocatedRamForInstance(Instance instance);

    public abstract void deallocateRamForInstance(Instance instance);

    public void deallocateRamForAllInstances() {
        setAvailableRam(getRam());
    }
    public abstract boolean isSuitableForInstance(Instance instance, int ram);

    public int getUsedRam() {
        return ram - availableRam;
    }
}
