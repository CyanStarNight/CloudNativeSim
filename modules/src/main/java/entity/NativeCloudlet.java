/*
 * Copyright ©2024. Jingfeng Wu.
 */

package entity;

import core.Status;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.cloudbus.cloudsim.UtilizationModel;

import java.util.Random;

import static core.Generator.generateCloudletLength;

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


    public Instance getInstance(){
        return Instance.getInstance(getInstanceUid());
    }
    public Service getService(){
        return Service.getService(getServiceName());
    }
    public String getAPI(){
        return getRequest().getMethod();
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
