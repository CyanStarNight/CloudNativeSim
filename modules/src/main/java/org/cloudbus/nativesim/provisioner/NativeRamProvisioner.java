/*
 * Copyright ©2023. Jingfeng Wu.
 */

package org.cloudbus.nativesim.provisioner;

import lombok.Getter;
import lombok.Setter;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;
import org.cloudbus.nativesim.entity.NativeEntity;

@Getter
@Setter
public abstract class NativeRamProvisioner extends RamProvisioner{
    private int ram;

    private int availableRam;

    public NativeRamProvisioner(int ram) {
        super(ram);
        setRam(ram);
        setAvailableRam(ram);
    }

    public abstract boolean allocateRamForEntity(NativeEntity entity, int ram);

    public abstract int getAllocatedRamForEntity(NativeEntity entity);

    public abstract void deallocateRamForEntity(NativeEntity entity);

    public void deallocateRamForAllEntities() {
        setAvailableRam(getRam());
    }
    public abstract boolean isSuitableForEntity(NativeEntity entity, int ram);

    public int getUsedRam() {
        return ram - availableRam;
    }
}
