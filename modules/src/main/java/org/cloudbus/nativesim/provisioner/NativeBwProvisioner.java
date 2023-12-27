/*
 * Copyright ©2023. Jingfeng Wu.
 */

package org.cloudbus.nativesim.provisioner;

import lombok.Getter;
import lombok.Setter;
import org.cloudbus.nativesim.entity.NativeEntity;

@Getter
@Setter
public abstract class NativeBwProvisioner {
    private long bw;

    private long availableBw;

    public NativeBwProvisioner(long bw) {
        setBw(bw);
        setAvailableBw(bw);
    }

    public abstract boolean allocateBwForEntity(NativeEntity entity, long bw);

    public abstract long getAllocatedBwForEntity(NativeEntity entity);

    public abstract void deallocateBwForEntity(NativeEntity entity);

    public void deallocateBwForAllEntities() {
        setAvailableBw(getBw());
    }
    public abstract boolean isSuitableForEntity(NativeEntity entity, long bw);

    public long getUsedBw() {
        return bw - availableBw;
    }

}
