/*
 * Copyright ©2023. Jingfeng Wu.
 */

package org.cloudbus.nativesim.provisioner;

import lombok.Getter;
import lombok.Setter;
import org.cloudbus.nativesim.entity.NativeEntity;

import java.util.HashMap;
import java.util.Map;
@Getter
@Setter
public class NativeRamProvisionerSimple extends NativeRamProvisioner{
    private Map<String, Integer> ramTable;

    public NativeRamProvisionerSimple(int ram) {
        super(ram);
        setRamTable(new HashMap<String, Integer>());
    }
    @Override
    public boolean allocateRamForEntity(NativeEntity entity, int ram) {
        int maxRam = entity.getRam();
                /* If the requested amount of RAM to be allocated to the ENTITY is greater than
                the amount of ENTITY is in fact requiring, allocate only the
                amount defined in the Entity requirements.*/
        if (ram >= maxRam) {
            ram = maxRam;
        }

        deallocateRamForEntity(entity);

        if (getAvailableRam() >= ram) {
            setAvailableRam(getAvailableRam() - ram);
            getRamTable().put(entity.getUid(), ram);
            entity.setCurrentAllocatedRam(getAllocatedRamForEntity(entity));
            return true;
        }

        entity.setCurrentAllocatedRam(getAllocatedRamForEntity(entity));

        return false;
    }
    @Override
    public int getAllocatedRamForEntity(NativeEntity entity) {
        if (getRamTable().containsKey(entity.getUid())) {
            return getRamTable().get(entity.getUid());
        }
        return 0;
    }

    @Override
    public void deallocateRamForEntity(NativeEntity entity) {
        if (getRamTable().containsKey(entity.getUid())) {
            int amountFreed = getRamTable().remove(entity.getUid());
            setAvailableRam(getAvailableRam() + amountFreed);
            entity.setCurrentAllocatedRam(0);
        }
    }

    public void deallocateRamForAllEntities() {
        setAvailableRam(getRam());
        getRamTable().clear();
    }
    @Override
    public boolean isSuitableForEntity(NativeEntity entity, int ram) {
        int allocatedRam = getAllocatedRamForEntity(entity);
        boolean result = allocateRamForEntity(entity, ram);
        deallocateRamForEntity(entity);
        if (allocatedRam > 0) {
            allocateRamForEntity(entity, allocatedRam);
        }
        return result;
    }
}
