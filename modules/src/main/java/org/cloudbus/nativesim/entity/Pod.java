package org.cloudbus.nativesim.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.cloudbus.nativesim.NativeController;
import org.cloudbus.nativesim.scheduler.PodCloudletScheduler;
import org.cloudbus.nativesim.util.Status;

import javax.validation.constraints.AssertTrue;

/**
 * @author JingFeng Wu
 */
/** Attention: pods对模拟的意义似乎更体现在空间资源和架构解释上
 * 1.All the containers share the same namespace、storage、lifetime and process action.
 * 2.Pod will connect the services and containers with double linkages.
 * 3.Pods are the basic units of scheduling for users.
 * 4.Use replicaSet to implements horizontal scaling.
 * */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pod extends NativeEntity {

    private NativeVm Vm;
    private long size;
    private ArrayList<String> labels; // one service n pods
    private List<Service> serviceList;
    private List<Container> containerList;

    private String prefix; // the prefix identifying the replicas
    private List<Pod> replicas;

    private int num_containers;
    private int num_replicas;

    private int pod_cpu,pod_ram,pod_bw;
    private int storage;
    private long lifeTime; // controlled by simulation
    private PodCloudletScheduler cloudletScheduler;

    public Status status = Status.Ready;

    public Pod(int userId, String name){
        super(userId,name);
    }
    /**Unit: Combine*/
    @AssertTrue
    public boolean matchServices(NativeController controller){
        List<Service> serviceList = new ArrayList<>();
        for (String label : labels){
            serviceList.addAll(controller.selectServicesByLabel(label));
        }
        if (!serviceList.isEmpty()) {
            setServiceList(serviceList);
            return true;
        }
        return false;
    }
    @AssertTrue
    public boolean matchReplicas(NativeController controller){
        List<Pod> replicas = controller.selectPodsByPrefix(prefix);
        if (!replicas.isEmpty()) {
            setReplicas(replicas);
            return true;
        }
        return false;
    }

    public void init(){ // link the entities and initialize the parameters.

    }

    public double updateEntityProcessing(double currentTime, List<Double> mipsShare) {
        if (mipsShare != null) {
            return getCloudletScheduler().updatePodProcessing(currentTime, mipsShare);
        }
        return 0.0;
    }

    public double getTotalUtilizationOfCpu(double time) {
        return getCloudletScheduler().getTotalUtilizationOfCpu(time);
    }

    public double getTotalUtilizationOfCpuMips(double time) {
        return getTotalUtilizationOfCpu(time) * getMips();
    }

}
