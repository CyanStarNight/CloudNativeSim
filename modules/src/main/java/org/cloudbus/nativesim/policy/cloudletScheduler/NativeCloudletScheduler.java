/*
 * Copyright ©2024. Jingfeng Wu.
 */

package org.cloudbus.nativesim.policy.cloudletScheduler;

import lombok.Getter;
import lombok.Setter;
import org.cloudbus.cloudsim.ResCloudlet;
import org.cloudbus.nativesim.extend.NativeCloudlet;
import org.cloudbus.nativesim.service.Instance;
import org.cloudbus.nativesim.service.Service;

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

    /** The cloudlet waiting list. */
    private List<NativeCloudlet> cloudletWaitingList = new ArrayList<>();

    /** The cloudlet exec list. */
    private List<NativeCloudlet> cloudletExecList = new ArrayList<>();

    /** The cloudlet paused list. */
    private List<NativeCloudlet> cloudletPausedList = new ArrayList<>();

    /** The cloudlet finished list. */
    private List<NativeCloudlet> cloudletFinishedList = new ArrayList<>();

    public NativeCloudletScheduler(double mips, int totalShare) {
        setPreviousTime(0.0);
        setMips(mips);
        setFreeShare(totalShare);
    }

    public abstract double getTotalUtilizationOfCpu(double time);

    public abstract double getTotalUtilizationOfRam(double time);

    public abstract double getTotalUtilizationOfBw(double time);

    public void receiveCloudlet(NativeCloudlet cloudlet){
        cloudletWaitingList.add(cloudlet);
    }

    public abstract void processCloudlets();

    public abstract void pauseCloudlets();

    public abstract void resumeCloudlets();

}


