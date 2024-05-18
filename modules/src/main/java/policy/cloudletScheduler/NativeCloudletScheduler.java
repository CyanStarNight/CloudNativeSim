/*
 * Copyright ©2024. Jingfeng Wu.
 */

package policy.cloudletScheduler;

import entity.Instance;
import lombok.Getter;
import lombok.Setter;
import entity.NativeCloudlet;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
public abstract class NativeCloudletScheduler{
    // instance uid -> cloudlet id list
    private Map<String ,Set<Integer>> cloudletsMap = new HashMap<>();
    private List<Instance> instanceList;
    /** The free share of instance. */
    private List<Integer> freeShareList;
    /** The previous time. */
    private double previousTime;
    /** The previous time. */
    private double totalExecTime;

    /** The cloudlet waiting list. */
    private List<NativeCloudlet> waitingQueue = new LinkedList<>();

    /** The cloudlet exec list. */
    private List<NativeCloudlet> execQueue = new LinkedList<>();

    /** The cloudlet paused list. */
    private List<NativeCloudlet> pausedQueue = new LinkedList<>();

    /** The cloudlet finished list. */
    private List<NativeCloudlet> finishedList = new ArrayList<>();


    public NativeCloudletScheduler(List<Instance> instanceList) {
        setInstanceList(instanceList);
        this.cloudletsMap = new HashMap<>();
        for (Instance instance : instanceList) {
            cloudletsMap.put(instance.getUid(), new HashSet<>());
        }
        setPreviousTime(0.0);
        setFreeShareList(instanceList.stream().map(Instance::getCurrentAllocatedCpuShare).collect(Collectors.toList()));
    }


    public abstract double getTotalUtilizationOfCpu(double time);

    public abstract double getTotalUtilizationOfRam(double time);

    public abstract double getTotalUtilizationOfBw(double time);

    public void  distributeCloudlets(List<NativeCloudlet> cloudlets){
        cloudlets.forEach(c -> distributeCloudlet(c,getInstanceList()));
    }

    public abstract void distributeCloudlet(NativeCloudlet nativeCloudlet, List<Instance> instanceList);

    public void receiveCloudlet(NativeCloudlet nativeCloudlet){
        waitingQueue.add(nativeCloudlet);
    }

    public void receiveCloudlets(List<NativeCloudlet> cloudlets){
        waitingQueue.addAll(cloudlets);
    }

    public abstract double processCloudlets();

    public abstract void pauseCloudlets();

    public abstract void resumeCloudlets();

}


