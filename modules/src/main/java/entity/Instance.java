/*
 * Copyright ©2024. Jingfeng Wu.
 */

package entity;

import lombok.Getter;
import lombok.Setter;
import extend.NativePe;
import extend.NativeVm;
import policy.cloudletScheduler.NativeCloudletScheduler;
import core.Status;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
public class Instance{
    // id & uid
    private String uid;
    private int id;
    private int appId;
    private String name;
    // type of instance
    public final String type = "Instance";
    // status of instance
    public Status status = Status.Ready;
    // one service n pods
    private List<String> labels;
    // services which mapped
    private List<Service> serviceList;
    // vm which located
    private NativeVm vm;
    // size of instance
    private long size;
    // num of requests
    public int num_requests;
    // num of cloudlets
    public int num_cloudlets;
    // ram needs
    private int requests_ram;
    // share needs
    private int requests_share;
    // mips needs
    private double requests_mips;
    // receive bw needs
    private double receive_bw;
    // transmit bw needs
    private double transmit_bw;
    // ram limits
    private int limits_ram;
    // share limits
    private int limits_share;
    // mips limits
    private double limits_mips;
    // ram usages
    private int currentAllocatedRam;
    // receive bw usages
    private double currentAllocatedReceiveBw;
    // transmit bw usages
    private double currentAllocatedTransmitBw;
    // pe usages
    private NativePe currentAllocatedPe;
    // mips usages
    private double currentAllocatedMips;
    // cpu share usages
    private int currentAllocatedCpuShare;
    // migration status
    private List<String> MigratingIn;
    private List<String> MigratingOut;
    private boolean inMigration;
    // utilization history
    public static final int HISTORY_LENGTH = 30;
    private final List<Double> utilizationHistory = new LinkedList<Double>();
    // previous time used to update utilizationHistory
    private int previousTime;
    // scheduling Interval 5s
    private double schedulingInterval = 5.0;
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
