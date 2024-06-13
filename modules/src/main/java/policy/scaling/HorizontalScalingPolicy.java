/*
 * Copyright ©2024. Jingfeng Wu.
 */

package policy.scaling;

import entity.Instance;
import entity.ReplicaSet;
import entity.Service;
import extend.NativeVm;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import static core.Reporter.printEvent;

@Getter
@Setter
public class HorizontalScalingPolicy extends ServiceScalingPolicy{

    double cpuThreshold = 0.95;

    private List<ReplicaSet> scalingList = new ArrayList<>();
    private List<ReplicaSet> failedList = new ArrayList<>();
    private List<ReplicaSet> finishedList = new ArrayList<>();


    public HorizontalScalingPolicy() {
        super();
    }

    public List<ReplicaSet> getReplicaSets(List<Instance> instanceList) {
        List<ReplicaSet> replicaSets = new ArrayList<>();
        for (Instance instance : instanceList) {
            if(!replicaSets.contains(instance.getReplicaSet())){
                replicaSets.add(instance.getReplicaSet());
            }
        }
        return replicaSets;
    }

    public double getMinUtilization(ReplicaSet replicaSet) {
        return replicaSet.getReplicas().stream().
                mapToDouble(Instance::getUtilizationOfCpu).min().
                orElse(0.0);
    }

    @Override
    public boolean needScaling(Service service) {
        boolean flag = false;
        for (ReplicaSet replicaSet : getReplicaSets(service.getInstanceList())) {
            if (getMinUtilization(replicaSet) > cpuThreshold){
                // 放入scaling
                getScalingList().add(replicaSet);
                flag =  true;
            }
        }
        return flag;
    }

    @Override
    public void scaling(Service service) {//TODO: 缩容处理似乎有问题

        for (ReplicaSet replicaSet : getScalingList()) {

            // 水平扩展
            Instance newReplica = replicaSet.replicate(); // 自动加入replica中
            // 判断能否扩展
            if (getServiceAllocationPolicy().allocateVmForInstance(newReplica)){
                getReplications().add(newReplica);
                service.getInstanceList().add(newReplica);
                printEvent(newReplica.getPrefix() + " has been horizontally scaled.");
            }
            // 否则删除副本
            else {
                getFailedList().add(replicaSet);
                Instance.deleteInstance(newReplica);
            }
            getFinishedList().add(replicaSet);
        }
        getScalingList().removeAll(getFinishedList());
        getFinishedList().clear();
    }
}
