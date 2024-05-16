/*
 * Copyright ©2024. Jingfeng Wu.
 */

package policy.cloudletScheduler;

import lombok.Getter;
import lombok.Setter;
import entity.NativeCloudlet;

import java.util.*;

@Getter
@Setter
public abstract class NativeCloudletScheduler{

    /** The mips of instance. */
    private double mips;
    /** The free share of instance. */
    private int freeShare;
    /** The previous time. */
    private double previousTime;
    /** The previous time. */
    private double totalExecTime;

    // TODO: 提交的cloudlets先进入waitingQueue，FIFO式进入execQueue，瓶颈是实例的处理上限

    /** The cloudlet waiting list. */
    private Queue<NativeCloudlet> waitingQueue = new LinkedList<>();

    /** The cloudlet exec list. */
    private Queue<NativeCloudlet> execQueue = new LinkedList<>();

    /** The cloudlet paused list. */
    private Queue<NativeCloudlet> pausedQueue = new LinkedList<>();

    /** The cloudlet finished list. */
    private Queue<NativeCloudlet> finishedQueue = new LinkedList<>();

    public NativeCloudletScheduler(double mips, int totalShare) {
        setPreviousTime(0.0);
        setMips(mips);
        setFreeShare(totalShare);
    }


    public abstract double getTotalUtilizationOfCpu(double time);

    public abstract double getTotalUtilizationOfRam(double time);

    public abstract double getTotalUtilizationOfBw(double time);

    public void receiveCloudlet(NativeCloudlet nativeCloudlet){
        waitingQueue.add(nativeCloudlet);
    }

    public abstract void processCloudlets();

    public abstract void pauseCloudlets();

    public abstract void resumeCloudlets();

}


