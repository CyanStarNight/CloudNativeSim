package org.cloudbus.nativesim.scheduler;

import lombok.Getter;
import lombok.Setter;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.nativesim.entity.Pod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Setter
@Getter
public abstract class PodScheduler {
    /** The peList. */
    private List<? extends Pe> peList;

    /** The map of VMs to PEs. */
    private Map<String, List<Pe>> peMap;

    /** The MIPS that are currently allocated to the VMs. */
    private Map<String, List<Double>> mipsMap;

    /** The total available mips. */
    private double availableMips;

    /** The VMs migrating in. */
    private List<String> podsMigratingIn;

    /** The VMs migrating out. */
    private List<String> podsMigratingOut;


    /**
     * Creates a new HostAllocationPolicy.
     *
     * @param pelist the pelist
     * @pre peList != $null
     * @post $none
     */
    public PodScheduler(List<? extends Pe> pelist) {
        setPeList(pelist);
        setPeMap(new HashMap<String, List<Pe>>());
        setMipsMap(new HashMap<String, List<Double>>());
        setAvailableMips(peList.getTotalMips(getPeList()));
        setPodsMigratingIn(new ArrayList<String>());
        setPodsMigratingOut(new ArrayList<String>());

    }

    /**
     * Allocates PEs for a VM.
     *
     * @param pod the pod
     * @param mipsShare the mips share
     * @return $true if this policy allows a new VM in the host, $false otherwise
     * @pre $none
     * @post $none
     */
    public abstract boolean allocatePesForPod(Pod pod, List<Double> mipsShare);

    /**
     * Releases PEs allocated to a VM.
     *
     * @param pod the pod
     * @pre $none
     * @post $none
     */
    public abstract void deallocatePesForPod(Pod pod);

    /**
     * Releases PEs allocated to all the VMs.
     *
     * @pre $none
     * @post $none
     */
    public void deallocatePesForAllPods() {
        getMipsMap().clear();
        setAvailableMips(peList.getTotalMips(getPeList()));
        for (Pe pe : getPeList()) {
             pe.getPeProvisioner().deallocateMipsForAllPods();
        }
    }

    /**
     * Gets the pes allocated for pod.
     *
     * @param pod the pod
     * @return the pes allocated for pod
     */
    public List<Pe> getPesAllocatedForVM(Pod pod) {
        return getPeMap().get(pod.getUid());
    }

    /**
     * Returns the MIPS share of each Pe that is allocated to a given VM.
     *
     * @param pod the pod
     * @return an array containing the amount of MIPS of each pe that is available to the VM
     * @pre $none
     * @post $none
     */
    public List<Double> getAllocatedMipsForPod(Pod pod) {
        return getMipsMap().get(pod.getUid());
    }

    /**
     * Gets the total allocated MIPS for a VM over all the PEs.
     *
     * @param pod the pod
     * @return the allocated mips for pod
     */
    public double getTotalAllocatedMipsForPod(Pod pod) {
        double allocated = 0;
        List<Double> mipsMap = getAllocatedMipsForPod(pod);
        if (mipsMap != null) {
            for (double mips : mipsMap) {
                allocated += mips;
            }
        }
        return allocated;
    }

    /**
     * Returns maximum available MIPS among all the PEs.
     *
     * @return max mips
     */
    public double getMaxAvailableMips() {
        if (getPeList() == null) {
            Log.printLine("Pe list is empty");
            return 0;
        }

        double max = 0.0;
        for (Pe pe : getPeList()) {
            double tmp = (pe.getPeProvisioner().getAvailableMips());
            if (tmp > max) {
                max = tmp;
            }
        }

        return max;
    }

    /**
     * Returns PE capacity in MIPS.
     *
     * @return mips
     */
    public double getPeCapacity() {
        if (getPeList() == null) {
            Log.printLine("Pe list is empty");
            return 0;
        }
        return getPeList().get(0).getMips();
    }

    /**
     * Gets the pod list.
     *
     * @param <T> the generic type
     * @return the pod list
     */
    @SuppressWarnings("unchecked")
    public <T extends Pe> List<T> getPeList() {
        return (List<T>) peList;
    }

    /**
     * Sets the pod list.
     *
     * @param <T> the generic type
     * @param peList the pe list
     */
    protected <T extends Pe> void setPeList(List<T> peList) {
        this.peList = peList;
    }



}
