/*
 * Copyright ©2024. Jingfeng Wu.
 */

package org.cloudbus.nativesim.service;

import lombok.Getter;
import lombok.Setter;
import org.cloudbus.cloudsim.util.MathUtil;
import org.cloudbus.nativesim.extend.NativePe;
import org.cloudbus.nativesim.extend.NativeVm;
import org.cloudbus.nativesim.policy.cloudletScheduler.NativeCloudletScheduler;
import org.cloudbus.nativesim.core.Status;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
public class Instance{

    private String uid;
    private int id;
    private int appId;
    private String name;

    public final String type = "Instance";

    public Status status = Status.Ready;
    private List<String> labels; // one service n pods
    private List<Service> serviceList;

    private NativeVm vm;

    private int requests_ram;
    private int requests_share;
    private double requests_mips;

    private int limits_ram;
    private int limits_share;
    private double limits_mips;

    private double receive_bw;
    private double transmit_bw;

    private long size;

    private int currentAllocatedRam;
    private double currentAllocatedReceiveBw;
    private double currentAllocatedTransmitBw;
    private NativePe currentAllocatedPe;
    private double currentAllocatedMips;
    private int currentAllocatedCpuShare;

    private NativeCloudletScheduler cloudletScheduler;
    private int previousTime;

    private double schedulingInterval = 1.0; //TODO: 待定

    private List<String> MigratingIn;
    private List<String> MigratingOut;
    private boolean inMigration;

    public static final int HISTORY_LENGTH = 30;
    private final List<Double> utilizationHistory = new LinkedList<Double>();

    // uid -> instance
    public static Map<String, Instance> InstanceUidMap = new HashMap<>();
    public static String getInstanceUid(int userId, int id) {
        return userId + "-Instance-" + id;
    }
    public static Instance getInstance(String uid){
        return InstanceUidMap.get(uid);
    }
    public static List<Instance> getAllInstances(){
        return (List<Instance>) InstanceUidMap.values();
    }
    public static List<Instance> matchInstancesWithLabels(Service service){
        List<String> labels = service.getLabels();
        return InstanceUidMap.values().stream().
                filter(instance -> instance.getLabels().stream().
                        anyMatch(labels::contains)).collect(Collectors.toList());
    }

    public Instance(int appId) {
        this.appId = appId;
        this.id = InstanceUidMap.size();
        setUid();
        setInMigration(false);
        setCurrentAllocatedCpuShare(0);

        //为没有字段的instance设置默认值
        setRequests_share(100);
        setRequests_ram(200);
        setLimits_share(1024);
        setLimits_ram(1000);
    }

    public Instance(int appId, List<String> labels) {
        new Instance(appId);
        this.labels = labels;
    }

    public Instance(int appId, String name, List<String> labels) {
        new Instance(appId,labels);
        this.name = name;
    }

    public void setUid() {
        InstanceUidMap.remove(uid);
        uid = appId + "-Instance-" + id;
        InstanceUidMap.put(uid,this);
    }


    public double getTotalUtilizationOfCpu(double time) {
        //Log.printLine("Container: get Current getTotalUtilizationOfCpu"+ getNativeCloudletScheduler().getTotalUtilizationOfCpu(time));
        return getCloudletScheduler().getTotalUtilizationOfCpu(time);
    }


    public double getUtilizationMean() {
        double mean = 0;
        if (!getUtilizationHistory().isEmpty()) {
            int n = Math.min(HISTORY_LENGTH, getUtilizationHistory().size());
            for (int i = 0; i < n; i++) {
                mean += getUtilizationHistory().get(i);
            }
            mean /= n;
        }
        return mean * getCurrentAllocatedMips();
    }


    public double getUtilizationVariance() {
        double mean = getUtilizationMean();
        double variance = 0;
        if (!getUtilizationHistory().isEmpty()) {
            int n = Math.min(HISTORY_LENGTH, getUtilizationHistory().size());
            for (int i = 0; i < n; i++) {
                double tmp = getUtilizationHistory().get(i) * getCurrentAllocatedMips() - mean;
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

}
