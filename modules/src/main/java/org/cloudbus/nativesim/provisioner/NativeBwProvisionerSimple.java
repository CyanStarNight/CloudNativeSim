/*
 * Copyright ©2023. Jingfeng Wu.
 */

package org.cloudbus.nativesim.provisioner;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.cloudbus.cloudsim.Vm;
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

    @Override
    public boolean allocateBwForVm(Vm vm, long bw) {
        deallocateBwForVm(vm);

        if (getAvailableBw() >= bw) {
            setAvailableBw(getAvailableBw() - bw);
            getBwTable().put(vm.getUid(), bw);
            vm.setCurrentAllocatedBw(getAllocatedBwForVm(vm));
            return true;
        }

        vm.setCurrentAllocatedBw(getAllocatedBwForVm(vm));
        return false;
    }

    /*
     * (non-Javadoc)
     * @see cloudsim.provisioners.BwProvisioner#getAllocatedBwForVm(cloudsim.Vm)
     */
    @Override
    public long getAllocatedBwForVm(Vm vm) {
        if (getBwTable().containsKey(vm.getUid())) {
            return getBwTable().get(vm.getUid());
        }
        return 0;
    }

    /*
     * (non-Javadoc)
     * @see cloudsim.provisioners.BwProvisioner#deallocateBwForVm(cloudsim.Vm)
     */
    @Override
    public void deallocateBwForVm(Vm vm) {
        if (getBwTable().containsKey(vm.getUid())) {
            long amountFreed = getBwTable().remove(vm.getUid());
            setAvailableBw(getAvailableBw() + amountFreed);
            vm.setCurrentAllocatedBw(0);
        }
    }

    /*
     * (non-Javadoc)
     * @see cloudsim.provisioners.BwProvisioner#deallocateBwForVm(cloudsim.Vm)
     */
    @Override
    public void deallocateBwForAllVms() {
        super.deallocateBwForAllVms();
        getBwTable().clear();
    }

    /*
     * (non-Javadoc)
     * @see
     * gridsim.virtualization.power.provisioners.BWProvisioner#isSuitableForVm(gridsim.virtualization
     * .power.VM, long)
     */
    @Override
    public boolean isSuitableForVm(Vm vm, long bw) {
        long allocatedBw = getAllocatedBwForVm(vm);
        boolean result = allocateBwForVm(vm, bw);
        deallocateBwForVm(vm);
        if (allocatedBw > 0) {
            allocateBwForVm(vm, allocatedBw);
        }
        return result;
    }

    /**
     * Gets the bw table.
     *
     * @return the bw table
     */
    protected Map<String, Long> getBwTable() {
        return bwTable;
    }

    /**
     * Sets the bw table.
     *
     * @param bwTable the bw table
     */
    protected void setBwTable(Map<String, Long> bwTable) {
        this.bwTable = bwTable;
    }


}
