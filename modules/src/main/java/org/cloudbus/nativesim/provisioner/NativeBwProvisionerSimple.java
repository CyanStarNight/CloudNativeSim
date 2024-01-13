/*
 * Copyright ©2023. Jingfeng Wu.
 */

package org.cloudbus.nativesim.provisioner;

import lombok.Getter;
import lombok.Setter;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.nativesim.entity.Container;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class NativeBwProvisionerSimple extends NativeBwProvisioner {
    private Map<String, Long> bwTable;
    
    public NativeBwProvisionerSimple(long bw) {
        super(bw);
        setBwTable(new HashMap<String, Long>());
    }
    @Override
    public boolean allocateBwForContainer(Container container, long bw) {
        deallocateBwForContainer(container);

        if (getAvailableBw() >= bw) {
            setAvailableBw(getAvailableBw() - bw);
            getBwTable().put(container.getUid(), bw);
            container.setCurrentAllocatedBw(getAllocatedBwForContainer(container));
            return true;
        }

        container.setCurrentAllocatedBw(getAllocatedBwForContainer(container));
        return false;
    }
    @Override
    public long getAllocatedBwForContainer(Container container) {
        if (getBwTable().containsKey(container.getUid())) {
            return getBwTable().get(container.getUid());
        }
        return 0;
    }

    @Override
    public void deallocateBwForContainer(Container container) {
        if (getBwTable().containsKey(container.getUid())) {
            long amountFreed = getBwTable().remove(container.getUid());
            setAvailableBw(getAvailableBw() + amountFreed);
            container.setCurrentAllocatedBw(0);
        }
    }

    public void deallocateBwForAllContainers() {
        setAvailableBw(getBw());
        getBwTable().clear();
    }
    @Override
    public boolean isSuitableForContainer(Container container, long bw) {
        long allocatedBw = getAllocatedBwForContainer(container);
        boolean result = allocateBwForContainer(container, bw);
        deallocateBwForContainer(container);
        if (allocatedBw > 0) {
            allocateBwForContainer(container, allocatedBw);
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
