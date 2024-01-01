/*
 * Copyright ©2023. Jingfeng Wu.
 */

package org.cloudbus.nativesim.provisioner;

import lombok.Getter;
import lombok.Setter;
import org.cloudbus.cloudsim.Vm;
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

    /*
     * (non-Javadoc)
     * @see cloudsim.provisioners.RamProvisioner#allocateRamForVm(cloudsim.Vm, int)
     */
    @Override
    public boolean allocateRamForVm(Vm vm, int ram) {
        int maxRam = vm.getRam();

        if (ram >= maxRam) {
            ram = maxRam;
        }

        deallocateRamForVm(vm);

        if (getAvailableRam() >= ram) {
            setAvailableRam(getAvailableRam() - ram);
            getRamTable().put(vm.getUid(), ram);
            vm.setCurrentAllocatedRam(getAllocatedRamForVm(vm));
            return true;
        }

        vm.setCurrentAllocatedRam(getAllocatedRamForVm(vm));

        return false;
    }

    /*
     * (non-Javadoc)
     * @see cloudsim.provisioners.RamProvisioner#getAllocatedRamForVm(cloudsim.Vm)
     */
    @Override
    public int getAllocatedRamForVm(Vm vm) {
        if (getRamTable().containsKey(vm.getUid())) {
            return getRamTable().get(vm.getUid());
        }
        return 0;
    }

    /*
     * (non-Javadoc)
     * @see cloudsim.provisioners.RamProvisioner#deallocateRamForVm(cloudsim.Vm)
     */
    @Override
    public void deallocateRamForVm(Vm vm) {
        if (getRamTable().containsKey(vm.getUid())) {
            int amountFreed = getRamTable().remove(vm.getUid());
            setAvailableRam(getAvailableRam() + amountFreed);
            vm.setCurrentAllocatedRam(0);
        }
    }

    /*
     * (non-Javadoc)
     * @see cloudsim.provisioners.RamProvisioner#deallocateRamForVm(cloudsim.Vm)
     */
    @Override
    public void deallocateRamForAllVms() {
        super.deallocateRamForAllVms();
        getRamTable().clear();
    }

    /*
     * (non-Javadoc)
     * @see cloudsim.provisioners.RamProvisioner#isSuitableForVm(cloudsim.Vm, int)
     */
    @Override
    public boolean isSuitableForVm(Vm vm, int ram) {
        int allocatedRam = getAllocatedRamForVm(vm);
        boolean result = allocateRamForVm(vm, ram);
        deallocateRamForVm(vm);
        if (allocatedRam > 0) {
            allocateRamForVm(vm, allocatedRam);
        }
        return result;
    }

    /**
     * Gets the ram table.
     *
     * @return the ram table
     */
    protected Map<String, Integer> getRamTable() {
        return ramTable;
    }

    /**
     * Sets the ram table.
     *
     * @param ramTable the ram table
     */
    protected void setRamTable(Map<String, Integer> ramTable) {
        this.ramTable = ramTable;
    }

}
