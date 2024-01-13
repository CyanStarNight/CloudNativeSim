package org.cloudbus.nativesim.scheduler;

import lombok.Getter;
import lombok.Setter;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.lists.PeList;
import org.cloudbus.nativesim.entity.Container;
import org.cloudbus.nativesim.entity.NativePe;
import org.cloudbus.nativesim.entity.Pod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Setter
@Getter
public abstract class ContainerScheduler {
    /** The peList. */
    private List<? extends NativePe> peList;

    /** The map of VMs to PEs. */
    private Map<String, List<NativePe>> peMap;

    /** The MIPS that are currently allocated to the VMs. */
    private Map<String, List<Double>> mipsMap;

    /** The total available mips. */
    private double availableMips;

    /** The VMs migrating in. */
    private List<String> containersMigratingIn;

    /** The VMs migrating out. */
    private List<String> containersMigratingOut;

    /**
     * Creates a new HostAllocationPolicy.
     *
     * @param pelist the pelist
     * @pre peList != $null
     * @post $none
     */
    public ContainerScheduler(List<? extends NativePe> pelist) {
        setPeList(pelist);
        setPeMap(new HashMap<String, List<NativePe>>());
        setMipsMap(new HashMap<String, List<Double>>());
        setAvailableMips(PeList.getTotalMips(getPeList()));
        setContainersMigratingIn(new ArrayList<String>());
        setContainersMigratingOut(new ArrayList<String>());

    }

    public abstract boolean allocatePesForContainer(Container container, List<Double> mipsShare);

    public abstract void deallocatePesForContainer(Container container);

    public void deallocatePesForAllContainers() {
        getMipsMap().clear();
        setAvailableMips(PeList.getTotalMips(getPeList()));
        for (NativePe pe : getPeList()) {
             pe.getNativePeProvisioner().deallocateMipsForAllContainers();
        }
    }

    public List<NativePe> getPesAllocatedForContainer(Container container) {
        return getPeMap().get(container.getUid());
    }

    public List<Double> getAllocatedMipsForContainer(Container container) {
        return getMipsMap().get(container.getUid());
    }

    public double getTotalAllocatedMipsForContainer(Container container) {
        double allocated = 0;
        List<Double> mipsMap = getAllocatedMipsForContainer(container);
        if (mipsMap != null) {
            for (double mips : mipsMap) {
                allocated += mips;
            }
        }
        return allocated;
    }

    public double getMaxAvailableMips() {
        if (getPeList() == null) {
            Log.printLine("Pe list is empty");
            return 0;
        }

        double max = 0.0;
        for (NativePe pe : getPeList()) {
            double tmp = (pe.getPeProvisioner().getAvailableMips());
            if (tmp > max) {
                max = tmp;
            }
        }

        return max;
    }

    public double getPeCapacity() {
        if (getPeList() == null) {
            Log.printLine("Pe list is empty");
            return 0;
        }
        return getPeList().get(0).getMips();
    }


    public abstract boolean allocatePesForPod(Pod pod, List<Double> currentRequestedMips);
}
