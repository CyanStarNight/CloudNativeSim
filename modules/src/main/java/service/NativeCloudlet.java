/*
 * Copyright ©2024. Jingfeng Wu.
 */

package service;

import core.Status;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.cloudbus.cloudsim.UtilizationModel;
import request.Request;

import java.util.Random;

@Getter @Setter @ToString
public class NativeCloudlet {

    public int id;
    // Cloudlet本质上是request处理的一部分
    private Request request;
    // 目前部署cloudlets的instance
    protected String instanceUid;
    // 目前部署cloudlets的service
    protected String serviceName;
    // cloudlet的长度，单位是B
    private int len;
    // cloudlet的状态分为Processing, Waiting和Finished
    public Status status;
    // cloudlet的处理时间和等待时间
    private double execTime;
    private double waitTime;
    // Cloudlet的全局参数定义，单位为KB
    public static int meanLength;
    public static int stdDev;

    // The cost per bw
    protected double costPerBw;

    // The accumulated bw cost
    protected double accumulatedBwCost;

    // The utilization of cpu model.
    private UtilizationModel utilizationModelCpu;

    //The utilization of memory model.
    private UtilizationModel utilizationModelRam;

    // The utilization of bw model.
    private UtilizationModel utilizationModelBw;


    public NativeCloudlet(int id, Request request, String serviceName,
                          UtilizationModel utilizationModelCpu,
                          UtilizationModel utilizationModelRam,
                          UtilizationModel utilizationModelBw) {
        this.id = id;
        this.request = request;
        this.serviceName = serviceName;
        this.len = generateCloudletLength();
        this.status = Status.Ready;
        this.utilizationModelCpu = utilizationModelCpu;
        this.utilizationModelRam = utilizationModelRam;
        this.utilizationModelBw = utilizationModelBw;

        accumulatedBwCost = 0.0;
        costPerBw = 0.0;
    }

    public NativeCloudlet() {
        this.len = generateCloudletLength();
        this.status = Status.Ready;
    }

    public static int generateCloudletLength() {
        // 创建 Random 实例
        Random random = new Random();
        // 生成正态分布的 Cloudlet 长度
        return (int) (meanLength + random.nextGaussian() * stdDev);
    }

    public Instance getInstance(){
        return Instance.getInstance(getInstanceUid());
    }
    public Service getService(){
        return Service.getService(getServiceName());
    }
    public String getAPI(){
        return getRequest().getAPI();
    }


    public double getTotalTime(){
        return execTime+waitTime;
    }

    public double getUtilizationOfCpu(final double time) {
        return getUtilizationModelCpu().getUtilization(time);
    }


    public double getUtilizationOfRam(final double time) {
        return getUtilizationModelRam().getUtilization(time);
    }


    public double getUtilizationOfBw(final double time) {
        return getUtilizationModelBw().getUtilization(time);
    }
}
