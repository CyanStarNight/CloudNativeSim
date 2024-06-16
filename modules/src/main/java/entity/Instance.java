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

@Getter
@Setter
public class Instance implements Cloneable{
    private String uid;
    // replica index
    private int rid;
    // prefix
    private String prefix;
    // type of instance
    public final String type = "Instance";
    // app id
    private int appId;

    // status of instance
    public Status status = Status.Ready;
    // one service n pods
    private List<String> labels;
    // services which mapped
    private List<String> serviceList= new ArrayList<>();
    // vm which located
    private NativeVm vm;
    // size of instance
    private long size;
    // num of cloudlets
    public List<RpcCloudlet> processingCloudlets = new ArrayList<>();
    public List<RpcCloudlet> completionCloudlets = new ArrayList<>();
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


    // uid -> instance
    public static Map<String, Instance> instanceUidMap = new HashMap<>();
    // replicas
    public ReplicaSet replicaSet;


    public void order(int appId) {
        // app id
        this.appId = appId;
        // uid
        uid = UUID.randomUUID().toString();
        instanceUidMap.put(uid,this);
        // rid
        this.rid = replicaSet.getReplicaCount();;
        replicaSet.add(this);
    }


    public Instance(int appId,  String prefix) {
        this.prefix = prefix;
        // 0.初始化replicaSet
        initReplicaSet(prefix);
        // 1.编号，设置各个id
        order(appId);
        // 2.设置默认值
        setRequests_share(100);
        setRequests_ram(200);
        setLimits_share(1024);
        setLimits_ram(1000);
        setInMigration(false);

    }

    public Instance(int appId, String prefix, List<String> labels) {
        this.prefix = prefix;
        // 0.初始化replicaSet
        initReplicaSet(prefix);
        // 1.编号，设置各个id
        order(appId);
        // 2.设置默认值
        setRequests_share(100);
        setRequests_ram(200);
        setLimits_share(1024);
        setLimits_ram(1000);
        setInMigration(false);
        this.labels = labels;
    }

    public void initReplicaSet(String prefix){

        if (ReplicaSet.replicaSetMap.containsKey(prefix))
            replicaSet = ReplicaSet.replicaSetMap.get(prefix);
        else{
            replicaSet = new ReplicaSet(prefix);
            ReplicaSet.replicaSetMap.put(prefix, replicaSet);
        }
    }


    // 添加或增加 RAM 使用量的方法
    public void addUsedRam(double ram) {
        this.usedRam += (int) Math.ceil(ram);
    }

    public void subUsedRam(double ram){
        this.usedRam -= (int) Math.ceil(ram);
    }

    // 添加或增加 share 使用量的方法
    public void addUsedShare(double share) {
        setUsedShare(usedShare += (int) Math.ceil(share));
    }

    public void subUsedShare(double share) {
        setUsedShare(usedShare -= (int) Math.ceil(share));
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

    public void clearUsage(){
        this.usedRam = 0;
        this.usedShare = 0;
        this.usedMips = 0;
        this.usedReceiveBw = 0;
        this.usedTransmitBw = 0;
    }

    public String getName() {
        return prefix+'-'+rid;
    }

    public String toString() {
        return "type:"+getType()+", "
                +"name: "+ getName()
                +", uid: "+getUid();
    }

    @Override
    public Instance clone() {
        try {
            // 浅拷贝
            Instance clone = (Instance) super.clone();
            clone.order(appId);
            clone.clearUsage();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }


    public void incrementTotalCloudlets() {
        setTotalCloudlets(totalCloudlets+1);
    }

    public double getUtilizationOfCpu() {
        return (double) getUsedShare() / getCurrentAllocatedCpuShare();
    }

    public double getFreeShare(){
        return getCurrentAllocatedCpuShare() - getUsedShare();
    }

    public static Instance getInstance(String uid){
        return instanceUidMap.get(uid);
    }

    public static List<Instance> getAllInstances(){
        return (List<Instance>) instanceUidMap.values();
    }

    public void releaseCloudlet(RpcCloudlet cl) {
        subUsedShare(cl.getShare());
        subUsedRam(cl.getSize());
        //TODO: 如何释放带宽？
        getProcessingCloudlets().remove(cl);
        getCompletionCloudlets().add(cl);
    }

    public static void deleteInstance(Instance instance) {
        instanceUidMap.remove(instance.getUid());
        instance.getReplicaSet().remove(instance);
        instance.serviceList.forEach(s -> Service.getService(s).removeInstance(instance));
    }
}
