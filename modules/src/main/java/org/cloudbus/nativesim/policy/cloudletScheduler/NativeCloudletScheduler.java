/*
 * Copyright ©2023. Jingfeng Wu.
 */

package org.cloudbus.nativesim.policy.cloudletScheduler;

import lombok.Getter;
import lombok.Setter;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.ResCloudlet;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public abstract class NativeCloudletScheduler{
    /** The previous time. */
    private double previousTime;

    /** The current mips share. */
    private List<Double> currentMipsShare;

    /** The cloudlet waiting list. */
    protected List<? extends ResCloudlet> cloudletWaitingList;

    /** The cloudlet exec list. */
    protected List<? extends ResCloudlet> cloudletExecList;

    /** The cloudlet paused list. */
    protected List<? extends ResCloudlet> cloudletPausedList;

    /** The cloudlet finished list. */
    protected List<? extends ResCloudlet> cloudletFinishedList;

    /** The cloudlet failed list. */
    protected List<? extends ResCloudlet> cloudletFailedList;


    public NativeCloudletScheduler() {
        setPreviousTime(0.0);
        cloudletWaitingList = new ArrayList<ResCloudlet>();
        cloudletExecList = new ArrayList<ResCloudlet>();
        cloudletPausedList = new ArrayList<ResCloudlet>();
        cloudletFinishedList = new ArrayList<ResCloudlet>();
        cloudletFailedList = new ArrayList<ResCloudlet>();
    }

    public abstract double updateEntityProcessing(double currentTime, List<Double> mipsShare);

    public abstract double cloudletSubmit(Cloudlet gl, double fileTransferTime);

    public abstract double cloudletSubmit(Cloudlet gl);

    public abstract Cloudlet cloudletCancel(int clId);

    public abstract boolean cloudletPause(int clId);

    public abstract double cloudletResume(int clId);

    public abstract void cloudletFinish(ResCloudlet rcl);

    public abstract int getCloudletStatus(int clId);

    public abstract boolean isFinishedCloudlets();

    public abstract Cloudlet getNextFinishedCloudlet();

    public abstract int runningCloudlets();

    public abstract Cloudlet migrateCloudlet();

    public abstract double getTotalUtilizationOfCpu(double time);

    public abstract List<Double> getCurrentRequestedMips();

    public abstract double getTotalCurrentAllocatedMipsForCloudlet(ResCloudlet rcl, double time);

    public abstract double getCurrentRequestedUtilizationOfRam();

    public abstract double getCurrentRequestedUtilizationOfBw();
}
