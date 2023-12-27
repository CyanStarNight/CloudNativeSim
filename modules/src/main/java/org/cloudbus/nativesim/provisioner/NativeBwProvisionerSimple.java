/*
 * Copyright ©2023. Jingfeng Wu.
 */

package org.cloudbus.nativesim.provisioner;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.cloudbus.nativesim.entity.NativeEntity;
import org.cloudbus.nativesim.entity.Service;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class NativeBwProvisionerSimple extends NativeBwProvisioner{
    private Map<String, Long> bwTable;
    
    public NativeBwProvisionerSimple(long bw) {
        super(bw);
        setBwTable(new HashMap<String, Long>());
    }
    @Override
    public boolean allocateBwForEntity(NativeEntity entity, long bw) {
        deallocateBwForEntity(entity);

        if (getAvailableBw() >= bw) {
            setAvailableBw(getAvailableBw() - bw);
            getBwTable().put(entity.getUid(), bw);
            entity.setCurrentAllocatedBw(getAllocatedBwForEntity(entity));
            return true;
        }

        entity.setCurrentAllocatedBw(getAllocatedBwForEntity(entity));
        return false;
    }
    @Override
    public long getAllocatedBwForEntity(NativeEntity entity) {
        if (getBwTable().containsKey(entity.getUid())) {
            return getBwTable().get(entity.getUid());
        }
        return 0;
    }

    @Override
    public void deallocateBwForEntity(NativeEntity entity) {
        if (getBwTable().containsKey(entity.getUid())) {
            long amountFreed = getBwTable().remove(entity.getUid());
            setAvailableBw(getAvailableBw() + amountFreed);
            entity.setCurrentAllocatedBw(0);
        }
    }

    public void deallocateBwForAllEntities() {
        setAvailableBw(getBw());
        getBwTable().clear();
    }
    @Override
    public boolean isSuitableForEntity(NativeEntity entity, long bw) {
        long allocatedBw = getAllocatedBwForEntity(entity);
        boolean result = allocateBwForEntity(entity, bw);
        deallocateBwForEntity(entity);
        if (allocatedBw > 0) {
            allocateBwForEntity(entity, allocatedBw);
        }
        return result;
    }

}
