/*
 * Copyright ©2024. Jingfeng Wu.
 */

package policy.scaling;

import core.Exporter;
import entity.Instance;
import entity.ReplicaSet;
import entity.Service;
import extend.UsageData;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static core.Reporter.printEvent;

@Getter
@Setter
public class HorizontalScalingPolicy extends ServiceScalingPolicy{

    // 范围，小于下界缩容，大于上界扩容
    private double[] cpuThreshold = {0.4, 0.85};

    private Map<String, List<UsageData>> usageOfCpuHistory = Exporter.usageOfCpuHistory;
    // 最近10条记录
    private int recentRange = 10;

    private List<ReplicaSet> scalingUpList = new ArrayList<>();
    private List<ReplicaSet> scalingDownList = new ArrayList<>();
    private List<ReplicaSet> failedList = new ArrayList<>();
    private List<ReplicaSet> finishedList = new ArrayList<>();

    public HorizontalScalingPolicy() {
        super();
    }

    // 获取当前服务的所有副本集合
    public List<ReplicaSet> getReplicaSets(Service service) {
        List<ReplicaSet> replicaSets = new ArrayList<>();
        for (Instance instance : service.getInstanceList()) {
            if(!replicaSets.contains(instance.getReplicaSet())){
                replicaSets.add(instance.getReplicaSet());
            }
        }
        return replicaSets;
    }



    @Override
    public boolean needScaling(Service service) {
        boolean flag = false;
        for (ReplicaSet replicaSet : getReplicaSets(service)) {
            List<Double> recentUsage = new ArrayList<>();
            // 读取用量历史
            for (Instance instance: replicaSet.getReplicas()){
                List<UsageData> recentData = usageOfCpuHistory.get(instance.getUid());
                if (recentData == null) continue;
                int size = recentData.size();
                // 近期平均值
                recentUsage.add(recentData.subList(Math.max(0,size-recentRange), size).stream()
                        .mapToDouble(UsageData::getUsage).average().orElse(0.0));
            }
            double avgRecentUsage = recentUsage.stream().mapToDouble(Double::doubleValue)
                    .average().orElse(-1);
            double avgRecentUtilization = avgRecentUsage / replicaSet.getReplicas().get(0).getCurrentAllocatedCpuShare();

            if (avgRecentUtilization > cpuThreshold[1]){
                // 放入扩容队列
                getScalingUpList().add(replicaSet);
                flag =  true;
            }
            if (avgRecentUtilization > 0 && avgRecentUtilization < cpuThreshold[0]){
                // 放入缩容队列
                getScalingDownList().add(replicaSet);
                flag =  true;
            }
        }
        return flag;
    }



    @Override
    public void scaling(Service service) {

        for (ReplicaSet replicaSet : getScalingUpList()) {
            // 水平扩展
            Instance newReplica = replicaSet.replicate(); // 自动加入replica中
            // 判断能否扩容
            if (getServiceAllocationPolicy().allocateVmForInstance(newReplica)){
                getNewInstances().add(newReplica);
                printEvent(newReplica.getPrefix() + " has been horizontally scaled.");
                service.getInstanceList().add(newReplica);
            }
            // 否则删除副本
            else {
                getFailedList().add(replicaSet);
            }
            getFinishedList().add(replicaSet);
        }

        //TODO: 缩容处理似乎有问题
        for (ReplicaSet replicaSet : getScalingDownList()) {
            // 选择谁缩容
            Random random = new Random();
            List<Instance> replicas = replicaSet.getReplicas();
            Instance selectedInstance = replicas.get(random.nextInt(replicas.size()));
            // 缩容
            getServiceAllocationPolicy().deallocateVmForInstance(selectedInstance);
            service.removeInstance(selectedInstance);
            getFinishedList().add(replicaSet);
        }


        getScalingUpList().clear();
        getScalingDownList().clear();
        getFinishedList().clear();
        getNewInstances().clear();
    }
}
