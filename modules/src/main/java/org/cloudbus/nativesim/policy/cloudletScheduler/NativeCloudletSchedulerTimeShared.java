/*
 * Copyright ©2023. Jingfeng Wu.
 */

package org.cloudbus.nativesim.policy.cloudletScheduler;

import lombok.Getter;
import lombok.Setter;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.ResCloudlet;

import java.util.List;

@Getter
@Setter
public class NativeCloudletSchedulerTimeShared extends NativeCloudletScheduler{
    protected int currentCPUs;

    public NativeCloudletSchedulerTimeShared() {
        super();
        currentCPUs = 0;
    }

    @Override
    public double updateEntityProcessing(double currentTime, List<Double> mipsShare) {
        return 0;
    }

    @Override
    public double cloudletSubmit(Cloudlet gl, double fileTransferTime) {
        return 0;
    }

    @Override
    public double cloudletSubmit(Cloudlet gl) {
        return 0;
    }

    @Override
    public Cloudlet cloudletCancel(int clId) {
        return null;
    }

    @Override
    public boolean cloudletPause(int clId) {
        return false;
    }

    @Override
    public double cloudletResume(int clId) {
        return 0;
    }

    @Override
    public void cloudletFinish(ResCloudlet rcl) {

    }

    @Override
    public int getCloudletStatus(int clId) {
        return 0;
    }

    @Override
    public boolean isFinishedCloudlets() {
        return false;
    }

    @Override
    public Cloudlet getNextFinishedCloudlet() {
        return null;
    }

    @Override
    public int runningCloudlets() {
        return 0;
    }

    @Override
    public Cloudlet migrateCloudlet() {
        return null;
    }

    @Override
    public double getTotalUtilizationOfCpu(double time) {
        return 0;
    }

    @Override
    public List<Double> getCurrentRequestedMips() {
        return null;
    }

    @Override
    public double getTotalCurrentAllocatedMipsForCloudlet(ResCloudlet rcl, double time) {
        return 0;
    }

    @Override
    public double getCurrentRequestedUtilizationOfRam() {
        return 0;
    }

    @Override
    public double getCurrentRequestedUtilizationOfBw() {
        return 0;
    }
}
