/*
 * Copyright ©2024. Jingfeng Wu.
 */

package org.cloudbus.nativesim.policy.cloudletScheduler;

import org.cloudbus.nativesim.core.Status;
import org.cloudbus.nativesim.extend.NativeCloudlet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sareh on 10/07/15.
 */
public class NativeCloudletSchedulerTimeShared extends NativeCloudletScheduler {

    public NativeCloudletSchedulerTimeShared(double mips, int totalShare) {
        super(mips, totalShare);
    }

    @Override
    public double getTotalUtilizationOfCpu(double time) {
        return 0;
    }

    @Override
    public double getTotalUtilizationOfRam(double time) {
        return 0;
    }

    @Override
    public double getTotalUtilizationOfBw(double time) {
        return 0;
    }

    /* 顺序处理cloudlets*/
    //TODO: 目前假设：instance同时只能处理一个cloudlet
    //TODO: cloudlet的等待时间怎么计算
    @Override
    public void processCloudlets() {
        // 收集所有待处理的cloudlets
        List<NativeCloudlet> toProcess = new ArrayList<>(getCloudletWaitingList());

        // 现在可以安全地处理这些cloudlets而不会导致ConcurrentModificationException
        for (NativeCloudlet cl : toProcess) {
            // 从等待列表中移除
            getCloudletWaitingList().remove(cl);

            // 添加到执行列表
            getCloudletExecList().add(cl);

            if (cl.status == Status.Ready) {
                int len = cl.getLen();
                // 假设getMips()方法返回每秒的处理能力，计算执行时间
                cl.setExecTime((double)len / getMips());
            }

            // 从执行列表中移除，并添加到完成列表
            getCloudletExecList().remove(cl);
            getCloudletFinishedList().add(cl);
            cl.status = Status.Success;
            //TODO: 更新响应时间的逻辑有问题
//            cl.getRequest().addDelay(cl.getTotalTime());
        }
    }


    @Override
    public void pauseCloudlets() {

    }

    @Override
    public void resumeCloudlets() {

    }
}





