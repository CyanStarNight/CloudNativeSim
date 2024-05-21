/*
 * Copyright ©2024. Jingfeng Wu.
 */

package entity;

import lombok.Getter;
import lombok.Setter;
import extend.NativePe;
import extend.NativeVm;
import core.Status;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
public class Instance implements Cloneable{
    // uid = prefix + type + id
    private String uid;
    // id
    private int id;
    // prefix
    private String prefix;
    // replicas
    private List<Instance> replicas;
    private int num_replicas;
    // app id
    private int appId;
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
    // num of cloudlets
    public List<NativeCloudlet> processingCloudlets = new ArrayList<>();
    public int totalCloudlets;

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

    // ram allocated
    private int currentAllocatedRam;
    // receive bw allocated
    private double currentAllocatedReceiveBw;
    // transmit bw allocated
    private double currentAllocatedTransmitBw;
    // pe allocated
    private NativePe currentAllocatedPe;
    // mips allocated
    private double currentAllocatedMips;
    // cpu share allocated
    private int currentAllocatedCpuShare;

    // ram used
    private int usedRam;
    // share used
    private int usedShare;
    // mips used
    private double usedMips;
    // receive bw used
    private double usedReceiveBw;
    // transmit bw used
    private double usedTransmitBw;

    // migration status
    private boolean inMigration;

    // previous  used to update utilizationHistory
    private int previousTime;
    // uid -> instance
    public static Map<String, Instance> InstanceUidMap = new HashMap<>();

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

    public Instance(int appId,  String prefix) {
        this.appId = appId;
        this.id = InstanceUidMap.size();
        setPrefix(prefix);
        setUid();
        setInMigration(false);
        setCurrentAllocatedCpuShare(0);
        //为没有字段的instance设置默认值
        setRequests_share(100);
        setRequests_ram(200);
        setLimits_share(1024);
        setLimits_ram(1000);
    }

    public Instance(int appId, String prefix, List<String> labels) {
        this.appId = appId;
        this.id = InstanceUidMap.size();
        setPrefix(prefix);
        setUid();
        setInMigration(false);
        setCurrentAllocatedCpuShare(0);

        //为没有字段的instance设置默认值
        setRequests_share(100);
        setRequests_ram(200);
        setLimits_share(1024);
        setLimits_ram(1000);
        this.labels = labels;
    }

    public void setUid() {
        // 删除之前的uid记录
        InstanceUidMap.remove(uid);
        // 添加新的
        uid = getPrefix() +"-"+getType()+"-"+ getId();
        InstanceUidMap.put(uid,this);
    }


    public String getName() {
        return getUid();
    }

    // 添加或增加 RAM 使用量的方法
    public void addUsedRam(int ram) {
        this.usedRam += ram;
    }

    // 添加或增加 share 使用量的方法
    public void addUsedShare(int share) {
        this.usedShare += share;
    }

    public void subUsedShare(int share) {
        this.usedShare -= share;
    }


    // 添加或增加 MIPS 使用量的方法
    public void addUsedMips(double mips) {
        this.usedMips += mips;
    }

    // 添加或增加接收带宽使用量的方法
    public void addUsedReceiveBw(double bw) {
        this.usedReceiveBw += bw;
    }

    // 添加或增加发送带宽使用量的方法
    public void addUsedTransmitBw(double bw) {
        this.usedTransmitBw += bw;
    }

    public String toString() {
        return "type:"+getType()+", "
                +"name: "+ getPrefix()
                +", uid: "+getUid();
    }

    @Override
    public Instance clone() {
        try {
            Instance clone = (Instance) super.clone();
            clone.id = InstanceUidMap.size();
            clone.setUid();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public void incrementTotalCloudlets() {
        setTotalCloudlets(totalCloudlets+1);
    }

}
