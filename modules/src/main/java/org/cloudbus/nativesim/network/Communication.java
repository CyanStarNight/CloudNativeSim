/*
 * Copyright ©2024. Jingfeng Wu.
 */

package org.cloudbus.nativesim.network;

import lombok.*;
import org.cloudbus.nativesim.entity.Service;

import java.util.Random;


@Getter
@Setter
public class Communication{

    private String uid; // the global id
    private int userId; // the user id
    private int id;

    private String tag; // tags may identify the definition of commu.

    String originName, destName;

    Service origin; // tailVec
    Service dest; // headVec

    private long bw;
    private long data; // cloudlets used to be the data.
    private double cost;

//    private long requestTime;
//    private long arrivalTime;
//    private long trans; // tans = arrivalTime - requestTime
//
//    private long responseTime;

    Communication hLink; // the edge which has the same head vertex.
    Communication tLink; // the edge which has the same tail vertex.
    double ete, lte; // ete: earliest time of edge, lte: latest time of edge.

    public Communication(int userId) {
        this.userId = userId;
        tag = "abstract";
    }

    public Communication(int userId,String tag){ // create an abstract commu;
        this.userId = userId;
        this.tag = tag;
    }

    public Communication(int userId, String originName, String destName) {
        this.userId = userId;
        this.originName = originName;
        this.destName = destName;
    }
//TODO: 2023/12/17 calculate_cost方法计算逻辑不对，具体值 = 关键任务的执行时间。
// 应用的执行时间 = 模拟结束的事件 = 启动时间+关键任务执行时间+实体通信时间
    public double calculate_cost(){
        double cost = 0.0 ;
//        for (NativeCloudlet d : data){
//            cost += d.generateCloudletLength();
//        }
        cost = new Random().nextDouble();
        this.cost = cost;
        return cost;
    }

    public String toString() {
        return originName +
                " -> " + destName +
                " (" + cost + ")";
    }
}
