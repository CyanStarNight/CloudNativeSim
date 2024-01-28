/*
 * Copyright ©2024. Jingfeng Wu.
 */

package org.cloudbus.nativesim.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.cloudbus.cloudsim.util.MathUtil;
import org.cloudbus.nativesim.network.EndPoint;
import org.cloudbus.nativesim.scheduler.NativeCloudletScheduler;
import org.cloudbus.nativesim.util.NativeStateHistoryEntry;
import org.cloudbus.nativesim.util.Status;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class Instance {
    private String uid; // the global id
    private int id;
    private int userId;
    private String name;
    public Status status = Status.Ready;

    private ArrayList<String> labels; // one service n pods
    private Service service;
    List<EndPoint> endPoints;
    private NativeVm vm;

    private long size;
    private int ram;
    private long bw;
    private double mips;
    private int numberOfPes;

    private long currentAllocatedSize;
    private int currentAllocatedRam;
    private long currentAllocatedBw;
    private List<Double> currentAllocatedMips;
    private int currentAllocatedPes;
    private double currentAllocatedTotalMips;

    private NativeCloudletScheduler cloudletScheduler;
    private double previousTime;
    private double schedulingInterval; //TODO: 2023/12/17 interval暂时没用上，待定

    private List<String> MigratingIn;
    private List<String> MigratingOut;
    private boolean inMigration;

    private boolean beingInstantiated;
    public static final int HISTORY_LENGTH = 30;
    private final List<Double> utilizationHistory = new LinkedList<Double>();
    private final List<NativeStateHistoryEntry> stateHistory = new LinkedList<NativeStateHistoryEntry>();

    public double updateContainerProcessing(double currentTime, List<Double> mipsShare) {
        if (mipsShare != null) {
            return getCloudletScheduler().updateContainerProcessing(currentTime, mipsShare);
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

    public int getCurrentRequestedRam() {
        if (isBeingInstantiated()) {
            return getRam();
        }
        return (int) (getCloudletScheduler().getCurrentRequestedUtilizationOfRam() * getRam());
    }

    public double getTotalUtilizationOfCpu(double time) {
        //Log.printLine("Container: get Current getTotalUtilizationOfCpu"+ getNativeCloudletScheduler().getTotalUtilizationOfCpu(time));
        return getCloudletScheduler().getTotalUtilizationOfCpu(time);
    }

    public double getTotalUtilizationOfCpuMips(double time) {
        //Log.printLine("Container: get Current getTotalUtilizationOfCpuMips"+getTotalUtilizationOfCpu(time) * getMips());
        return getTotalUtilizationOfCpu(time) * getCurrentAllocatedTotalMips();
    }

    public void addStateHistoryEntry(
            double time,
            double allocatedMips,
            double requestedMips,
            boolean isInMigration) {
        NativeStateHistoryEntry newState = new NativeStateHistoryEntry(
                time,
                allocatedMips,
                requestedMips,
                isInMigration);
        if (!getStateHistory().isEmpty()) {
            NativeStateHistoryEntry previousState = getStateHistory().get(getStateHistory().size() - 1);
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
        return mean * getCurrentAllocatedTotalMips();
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
                double tmp = getUtilizationHistory().get(i) * getCurrentAllocatedTotalMips() - mean;
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
}
