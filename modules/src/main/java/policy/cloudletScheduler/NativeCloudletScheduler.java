/*
 * Copyright ©2024. Jingfeng Wu.
 */

package policy.cloudletScheduler;

import core.Status;
import entity.Instance;
import entity.Service;
import lombok.Getter;
import lombok.Setter;
import entity.RpcCloudlet;

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
    protected Queue<RpcCloudlet> waitingQueue = new LinkedList<>();

    /** The cloudlet exec list. */
    protected Queue<RpcCloudlet> execQueue = new LinkedList<>();

    /** The cloudlet paused list. */
    protected List<RpcCloudlet> failedQueue = new LinkedList<>();

    /** The cloudlet finished list. */
    protected List<RpcCloudlet> finishedList = new ArrayList<>();


    public NativeCloudletScheduler() {
        setPreviousTime(0.0);
    }



    public void addToWaitingQueue(RpcCloudlet rpcCloudlet){
        waitingQueue.add(rpcCloudlet);
        rpcCloudlet.setStatus(Status.Waiting);
    }

    public void addToWaitingQueue(List<RpcCloudlet> cloudlets){
        waitingQueue.addAll(cloudlets);
        cloudlets.forEach(c -> c.setStatus(Status.Waiting));
    }

    public abstract void schedule();
    public abstract boolean distributeCloudlet(RpcCloudlet rpcCloudlet, List<Instance> instanceList);


    public abstract void addToProcessingQueue(RpcCloudlet cloudlet);

    private void addToProcessingQueue(List<RpcCloudlet> cloudlets){
        cloudlets.forEach(this::addToProcessingQueue);
    }

    public abstract void processCloudlets();

}


