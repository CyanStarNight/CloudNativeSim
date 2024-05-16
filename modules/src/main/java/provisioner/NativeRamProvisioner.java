/*
 * Copyright ©2024. Jingfeng Wu.
 */

package provisioner;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import entity.Instance;

@Getter
@Setter
@NoArgsConstructor
public abstract class NativeRamProvisioner {

    private int ram;

    private int availableRam;

    public void init(int ram){
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
