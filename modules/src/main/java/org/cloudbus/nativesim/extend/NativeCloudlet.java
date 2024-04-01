/*
 * Copyright ©2024. Jingfeng Wu.
 */

package org.cloudbus.nativesim.extend;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.cloudbus.nativesim.core.Status;
import org.cloudbus.nativesim.request.Request;
import org.cloudbus.nativesim.service.Instance;

import java.util.Random;

@Getter @Setter @ToString @NoArgsConstructor
public class NativeCloudlet {

    public String API;

    public int id;

    private Request request;

    protected String instanceUid; // 目前部署cloudlets的instance

    protected String serviceName; // 目前部署cloudlets的service

    private int len; // cloudlet的长度

    private double execTime;

    private double waitTime;

    public Status status;

    public static int meanLength;
    public static int stdDev;

    private int usedShare;
    private int usedRam;
    private int usedBw;

    public NativeCloudlet(int id, String API, String serviceName) {
        this.id = id;
        this.serviceName = serviceName;
        this.API = API;
        this.len = generateCloudletLength();
        this.status = Status.Ready;
    }

    public NativeCloudlet(int id, String api) {
        this.id = id;
        this.API = api;
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
        return Instance.getInstance(instanceUid);
    }

    public double getTotalTime(){
        return execTime+waitTime;
    }
}
