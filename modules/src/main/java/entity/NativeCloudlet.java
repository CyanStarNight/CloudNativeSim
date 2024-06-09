/*
 * Copyright ©2024. Jingfeng Wu.
 */

package entity;

import core.Status;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter @Setter @ToString
public class NativeCloudlet {
    // ID
    public int id;
    // 计数
    private static int count = 0;
    // 绑定request
    private Request request;
    // 目前部署cloudlets的instance
    protected Instance instance;
    // 目前部署cloudlets的service
    protected String serviceName;
    // cloudlet的长度，单位是B
    private int len;
    // cloudlet的大小
    private float size;

    private double share;
    // cloudlet的状态分为Processing, Waiting和Finished
    public Status status;

    private double startExecTime;
    // cloudlet的处理时间和
    private double execTime;
    // 等待时间
    private double waitTime;

    // The cost per bw
    protected double costPerBw;


    public NativeCloudlet(Request request, String serviceName, int len) {
        this.id = count++;
        this.request = request;
        this.serviceName = serviceName;
        this.len = len;
        this.status = Status.Ready;
        this.size = (float) len*4;
    }

    // 游走cloudlet
    public NativeCloudlet(int len) {
        this.len = len;
        this.status = Status.Ready;
        this.size = (float) len*4;
    }

    public void addWitTime(double waitTime) {
        setWaitTime(getWaitTime() + waitTime);
    }

    public Service getService(){
        return Service.getService(getServiceName());
    }
    public String getApiName(){
        return getRequest().getApiName();
    }

    public void bindToInstance(Instance instance){
        setInstance(instance);
        instance.totalCloudlets ++;
    }
}
