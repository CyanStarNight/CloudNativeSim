/*
 * Copyright ©2024. Jingfeng Wu.
 */

package policy.cloudletScheduler;

import core.Status;
import entity.Instance;
import entity.Service;
import lombok.Getter;
import lombok.Setter;
import entity.NativeCloudlet;

import java.util.*;

@Getter
@Setter
public abstract class NativeCloudletScheduler{

    /** The previous time. */
    protected double previousTime;

    private Service service;
    // instance uid -> cloudlets
    protected Map<String, Integer> usedShareNumMap = new HashMap<>();

    private int schedulingInterval = 10;

    /** The cloudlet waiting list. */
    protected Queue<NativeCloudlet> waitingQueue = new LinkedList<>();

    /** The cloudlet exec list. */
    protected Queue<NativeCloudlet> execQueue = new LinkedList<>();

    /** The cloudlet paused list. */
    protected List<NativeCloudlet> failedQueue = new LinkedList<>();

    /** The cloudlet finished list. */
    protected List<NativeCloudlet> finishedList = new ArrayList<>();


    public NativeCloudletScheduler() {
        setPreviousTime(0.0);
    }


    public abstract void receiveCloudlets(List<NativeCloudlet> cloudlets);

    public void addToWaitingQueue(NativeCloudlet nativeCloudlet){
        waitingQueue.add(nativeCloudlet);
        nativeCloudlet.setStatus(Status.Waiting);
    }

    public void addToWaitingQueue(List<NativeCloudlet> cloudlets){
        waitingQueue.addAll(cloudlets);
        cloudlets.forEach(c -> c.setStatus(Status.Waiting));
    }

    public abstract void schedule();
    public abstract boolean distributeCloudlet(NativeCloudlet nativeCloudlet, List<Instance> instanceList);


    public abstract void addToProcessingQueue(NativeCloudlet cloudlet);

    private void addToProcessingQueue(List<NativeCloudlet> cloudlets){
        cloudlets.forEach(this::addToProcessingQueue);
    }

    public abstract void processCloudlets();

}


