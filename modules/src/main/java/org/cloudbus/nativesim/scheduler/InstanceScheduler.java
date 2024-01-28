package org.cloudbus.nativesim.scheduler;

import lombok.Getter;
import lombok.Setter;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.lists.PeList;
import org.cloudbus.nativesim.entity.Instance;
import org.cloudbus.nativesim.entity.NativePe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Setter
@Getter
public abstract class InstanceScheduler {
    /** The peList. */
    private List<? extends NativePe> peList;

    /** The map of VMs to PEs. */
    private Map<String, List<NativePe>> peMap;

    /** The MIPS that are currently allocated to the VMs. */
    private Map<String, List<Double>> mipsMap;

    /** The total available mips. */
    private double availableMips;

    /** The VMs migrating in. */
    private List<String> instanceMigratingIn;

    /** The VMs migrating out. */
    private List<String> instanceMigratingOut;


    public InstanceScheduler(List<? extends NativePe> pelist) {
        setPeList(pelist);
        setPeMap(new HashMap<String, List<NativePe>>());
        setMipsMap(new HashMap<String, List<Double>>());
        setAvailableMips(PeList.getTotalMips(getPeList()));
        setInstanceMigratingIn(new ArrayList<String>());
        setInstanceMigratingOut(new ArrayList<String>());

    }

    public abstract boolean allocatePesForInstance(Instance instance, List<Double> mipsShare);

    public abstract void deallocatePesForInstance(Instance instance);

    public void deallocatePesForAllInstances() {
        getMipsMap().clear();
        setAvailableMips(PeList.getTotalMips(getPeList()));
        for (NativePe pe : getPeList()) {
             pe.getInstancePeProvisioner().deallocateMipsForAllInstances();
        }
    }

    public List<NativePe> getPesAllocatedForInstance(Instance instance) {
        return getPeMap().get(instance.getUid());
    }

    public List<Double> getAllocatedMipsForInstance(Instance instance) {
        return getMipsMap().get(instance.getUid());
    }

    public double getTotalAllocatedMipsForInstance(Instance instance) {
        double allocated = 0;
        List<Double> mipsMap = getAllocatedMipsForInstance(instance);
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

}
