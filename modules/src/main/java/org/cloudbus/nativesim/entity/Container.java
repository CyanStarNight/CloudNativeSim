package org.cloudbus.nativesim.entity;

import lombok.*;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmStateHistoryEntry;
import org.cloudbus.cloudsim.util.MathUtil;
import org.cloudbus.nativesim.policy.cloudletScheduler.NativeCloudletScheduler;

import javax.lang.model.element.PackageElement;
import java.util.*;

/**
 * @author JingFeng Wu
 * the memeber of containers will be specified in file inputs.
 */

//TODO: 2023/12/17 overbooking等属性要考虑下怎么运用，能不能简化

@AllArgsConstructor
@Data
public class Container extends NativeEntity{//Attention: 继承的目的是为了双向映射pods和communications
    private long size;
    private double mips;
    private double workloadMips;
    private int numberOfPes;
    private float ram;
    private long bw;

    private String containerManager;

    private NativeCloudletScheduler cloudletScheduler;
    
    Pod pod;
    Vm vm;

    private boolean inMigration;

    private long currentAllocatedSize;

    private float currentAllocatedRam;

    private long currentAllocatedBw;

    private List<Double> currentAllocatedMips;

    private boolean beingInstantiated;

    private final List<VmStateHistoryEntry> stateHistory = new LinkedList<VmStateHistoryEntry>();

    public static final int HISTORY_LENGTH = 30;

    private final List<Double> utilizationHistory = new LinkedList<Double>();

    private double previousTime;

    private double schedulingInterval; //TODO: 2023/12/17 interval暂时没用上，待定
    private List<? extends Pe> peList;

    /** The map of VMs to PEs. */
    private Map<String, List<Pe>> peMap;

    /** The MIPS that are currently allocated to the VMs. */
    private Map<String, List<Double>> mipsMap;

    /** The total available mips. */
    private double availableMips;

    /** The VMs migrating in. */
    private List<String> containersMigratingIn;

    /** The VMs migrating out. */
    private List<String> containersMigratingOut;

    public Container(
            int userId,
            double mips,
            int numberOfPes,
            int ram,
            long bw,
            long size) {
        setWorkloadMips(mips);
        setUserId(userId);
        setUid();
        setMips(mips);
        setNumberOfPes(numberOfPes);
        setRam(ram);
        setBw(bw);
        setSize(size);
        setInMigration(false);
        setBeingInstantiated(true);
        setCurrentAllocatedBw(0);
        setCurrentAllocatedMips(null);
        setCurrentAllocatedRam(0);
        setCurrentAllocatedSize(0);
    }


    public void init(){ // link the entities and initialize the parameters.

    }

    @Override
    public String toString() {
        return "NativeContainer{" +
                "uid=" + getUid() +
                ", id=" + getId() +
                ", userId=" + getUserId() +
                ", mips=" + getMips() +
                ", numberOfPes=" + getNumberOfPes() +
                ", ram=" + getRam() +
                ", bw=" + getBw() +
                ", size=" + getSize() +
////                ", cloudletScheduler=" + super.cloudletScheduler() +
                ", pod=" + getPod().getId() + //这里不能get pod 否则会循环递归
                "} ";
    }


    public double updateContainerProcessing(double currentTime, List<Double> mipsShare) {
        if (mipsShare != null) {
            return getCloudletScheduler().updateEntityProcessing(currentTime, mipsShare);
        }
        return 0.0;
    }

    public double getCurrentRequestedTotalMips() {
        double totalRequestedMips = 0;
        for (double mips : getCurrentRequestedMips()) {
            totalRequestedMips += mips;
        }
        return totalRequestedMips;
    }

    public double getCurrentRequestedMaxMips() {
        double maxMips = 0;
        for (double mips : getCurrentRequestedMips()) {
            if (mips > maxMips) {
                maxMips = mips;
            }
        }
        return maxMips;
    }

    public long getCurrentRequestedBw() {
        if (isBeingInstantiated()) {
            return getBw();
        }
        return (long) (getCloudletScheduler().getCurrentRequestedUtilizationOfBw() * getBw());
    }

    public float getCurrentRequestedRam() {
        if (isBeingInstantiated()) {
            return getRam();
        }
        return (float) (getCloudletScheduler().getCurrentRequestedUtilizationOfRam() * getRam());
    }

    public double getTotalUtilizationOfCpu(double time) {
        //Log.printLine("Container: get Current getTotalUtilizationOfCpu"+ getNativeCloudletScheduler().getTotalUtilizationOfCpu(time));
        return getCloudletScheduler().getTotalUtilizationOfCpu(time);
    }

    public double getTotalUtilizationOfCpuMips(double time) {
        //Log.printLine("Container: get Current getTotalUtilizationOfCpuMips"+getTotalUtilizationOfCpu(time) * getMips());
        return getTotalUtilizationOfCpu(time) * getMips();
    }

    public boolean isBeingInstantiated() {
        return beingInstantiated;
    }

    public void addStateHistoryEntry(
            double time,
            double allocatedMips,
            double requestedMips,
            boolean isInMigration) {
        VmStateHistoryEntry newState = new VmStateHistoryEntry(
                time,
                allocatedMips,
                requestedMips,
                isInMigration);
        if (!getStateHistory().isEmpty()) {
            VmStateHistoryEntry previousState = getStateHistory().get(getStateHistory().size() - 1);
            if (previousState.getTime() == time) {
                getStateHistory().set(getStateHistory().size() - 1, newState);
                return;
            }
        }
        getStateHistory().add(newState);
    }

    public double getUtilizationMad() {
        double mad = 0;
        if (!getUtilizationHistory().isEmpty()) {
            int n = HISTORY_LENGTH;
            if (HISTORY_LENGTH > getUtilizationHistory().size()) {
                n = getUtilizationHistory().size();
            }
            double median = MathUtil.median(getUtilizationHistory());
            double[] deviationSum = new double[n];
            for (int i = 0; i < n; i++) {
                deviationSum[i] = Math.abs(median - getUtilizationHistory().get(i));
            }
            mad = MathUtil.median(deviationSum);
        }
        return mad;
    }

    public double getUtilizationMean() {
        double mean = 0;
        if (!getUtilizationHistory().isEmpty()) {
            int n = HISTORY_LENGTH;
            if (HISTORY_LENGTH > getUtilizationHistory().size()) {
                n = getUtilizationHistory().size();
            }
            for (int i = 0; i < n; i++) {
                mean += getUtilizationHistory().get(i);
            }
            mean /= n;
        }
        return mean * getMips();
    }

    public double getUtilizationVariance() {
        double mean = getUtilizationMean();
        double variance = 0;
        if (!getUtilizationHistory().isEmpty()) {
            int n = HISTORY_LENGTH;
            if (HISTORY_LENGTH > getUtilizationHistory().size()) {
                n = getUtilizationHistory().size();
            }
            for (int i = 0; i < n; i++) {
                double tmp = getUtilizationHistory().get(i) * getMips() - mean;
                variance += tmp * tmp;
            }
            variance /= n;
        }
        return variance;
    }

    public void addUtilizationHistoryValue(final double utilization) {
        getUtilizationHistory().add(0, utilization);
        if (getUtilizationHistory().size() > HISTORY_LENGTH) {
            getUtilizationHistory().remove(HISTORY_LENGTH);
        }
    }

    protected List<Double> getUtilizationHistory() {
        return utilizationHistory;
    }

    public double getPreviousTime() {
        return previousTime;
    }

    public void setPreviousTime(final double previousTime) {
        this.previousTime = previousTime;
    }

    public double getSchedulingInterval() {
        return schedulingInterval;
    }

    protected void setSchedulingInterval(double schedulingInterval) {
        this.schedulingInterval = schedulingInterval;
    }

//

    public List<Double> getCurrentRequestedMips() {
        if (isBeingInstantiated()) {
            List<Double> currentRequestedMips = new ArrayList<>();

            for (int i = 0; i < getNumberOfPes(); i++) {
                currentRequestedMips.add(getMips());

            }

            return currentRequestedMips;
        }

        return getCloudletScheduler().getCurrentRequestedMips();
    }

    public double getWorkloadTotalMips() {

        //Log.printLine("Container: get Current totalRequestedMips"+ totalRequestedMips);
        return getWorkloadMips() * getNumberOfPes();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Container container = (Container) o;
        return Objects.equals(getUid(), container.getUid()) && Objects.equals(pod, container.pod);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUid(), pod);
    }

}