/*
 * Copyright ©2023. Jingfeng Wu.
 */

package org.cloudbus.nativesim.provisioner;

import lombok.Getter;
import lombok.Setter;
import org.cloudbus.cloudsim.provisioners.PeProvisioner;
import org.cloudbus.nativesim.entity.NativeEntity;
import java.util.List;

@Getter
@Setter
public abstract class NativePeProvisioner extends PeProvisioner {
    private double mips;

    private double availableMips;

    public NativePeProvisioner(double mips) {
        super(mips);
        setMips(mips);
        setAvailableMips(mips);
    }

    public abstract boolean allocateMipsForEntity(NativeEntity entity, double mips);
    public abstract boolean allocateMipsForEntity(String entityUid, double mips);
    public abstract boolean allocateMipsForEntity(NativeEntity entity, List<Double> mips);

    public abstract List<Double> getAllocatedMipsForEntity(NativeEntity entity);

    public abstract double getTotalAllocatedMipsForEntity(NativeEntity entity);

    public abstract double getAllocatedMipsForEntityByVirtualPeId(NativeEntity entity, int peId);

    public abstract void deallocateMipsForEntity(NativeEntity entity);

    public void deallocateMipsForAllEntities() {
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
