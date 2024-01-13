package org.cloudbus.nativesim.entity;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.cloudbus.nativesim.Controller;
import org.cloudbus.nativesim.network.EndPoint;
import org.cloudbus.nativesim.scheduler.NativeCloudletScheduler;
import org.cloudbus.nativesim.util.NativeStateHistoryEntry;
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
@EqualsAndHashCode()
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pod{

    private String uid; // the global id
    private int id;
    private int userId;
    public String name; // really need ?

    private ArrayList<String> labels; // one service n pods
    private Service service;
    List<EndPoint> endPoints;
    private NativeVm vm;

    private String prefix; // the prefix identifying the replicas
    private List<Pod> replicas;
    private int num_replicas;

    private List<Container> containerList;
    private int num_containers;

    private long size;
    private double mips;
    private int numberOfPes;
    private int ram;
    private long bw;
    private long currentAllocatedSize;
    private int currentAllocatedRam;
    private long currentAllocatedBw;
    private List<Double> currentAllocatedMips;

    private NativeCloudletScheduler cloudletScheduler;
    public Status status = Status.Ready;

    private boolean inMigration;
    private boolean beingInstantiated;

    private final List<NativeStateHistoryEntry> stateHistory = new LinkedList<NativeStateHistoryEntry>();

    public void setUid() {
        uid = userId + "-" + id;
    }

    public static String getUid(int userId, int id) {
        return userId + "-" + id;
    }

    public Pod(int userId) {
        setUid();
        setUserId(userId);
    }
    public Pod(int userId,String name) {
        setUid();
        setUserId(userId);
        setName(name);
    }


    public double getTotalUtilizationOfCpuMips(double time) {
        return getTotalUtilizationOfCpu(time) * getMips();
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
//    @AssertTrue
//    public boolean matchServices(Controller controller){
//        List<Service> serviceList = new ArrayList<>();
//        for (String label : labels){
//            serviceList.addAll(controller.selectServicesByLabel(label));
//        }
//        if (!serviceList.isEmpty()) {
//            setServiceList(serviceList);
//            return true;
//        }
//        return false;
//    }

    @AssertTrue
    public boolean matchReplicas(Controller controller){
        List<Pod> replicas = controller.selectPodsByPrefix(prefix);
        if (!replicas.isEmpty()) {
            setReplicas(replicas);
            return true;
        }
        return false;
    }

    public int getCurrentRequestedRam() {
        if (isBeingInstantiated()) {
            return getRam();
        }
        return (int) (getCloudletScheduler().getCurrentRequestedUtilizationOfRam() * getRam());
    }

    public double getTotalUtilizationOfCpu(double time) {
        return getCloudletScheduler().getTotalUtilizationOfCpu(time);
    }

    public long getCurrentRequestedBw() {
        if (isBeingInstantiated()) {
            return getBw();
        }
        return (long) (getCloudletScheduler().getCurrentRequestedUtilizationOfBw() * getBw());
    }

    @Override
    public String toString() {
        return "\n"+this.getClass().getSimpleName()+":\n"
                +"name: "+name
                +"\nid: "+id;
    }
}
